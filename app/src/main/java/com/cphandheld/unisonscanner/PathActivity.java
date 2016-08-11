package com.cphandheld.unisonscanner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class PathActivity extends HeaderActivity {
    TextView textVIN;
    TextView textBin;
    ListView listPaths;
    ArrayList paths;
    Button buttonCustomPath;
    Button buttonNoPath;

    String errorMessage;
    private ProgressDialog mProgressDialog;

    private DBHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path);
        setHeader(R.color.colorPathHeader, Utilities.currentUser.name, Utilities.currentContext.locationName, R.string.path_header);

        dbHelper = new DBHelper(PathActivity.this);
        dbHelper.getWritableDatabase();

        mProgressDialog = new ProgressDialog(PathActivity.this);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Checking in vehicle...");
        mProgressDialog.setMessage("Hold on a sec...");

        textVIN = (TextView) findViewById(R.id.textVIN);
        textVIN.setText(Utilities.currentContext.vehicle.VIN);

        textBin = (TextView) findViewById(R.id.textBin);
        textBin.setText(Utilities.currentContext.binName.toUpperCase());

        listPaths = (ListView) findViewById(R.id.listPaths);
        listPaths.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view.findViewById(R.id.rowTextView);
                textView.setTextColor(getResources().getColor(R.color.colorPathTextSelected));
                view.setBackgroundColor(getResources().getColor(R.color.colorPathBgSelected));

                Path path = (Path) paths.get(position);
                int pathId = path.pathId;
                String name = path.name;
                boolean startPath = path.startPath;

                Intent intent = new Intent(PathActivity.this, NotesActivity.class);
                Utilities.currentContext.pathId = pathId;
                Utilities.currentContext.pathName = name;
                Utilities.currentContext.startPath = startPath;
                startActivityForResult(intent, 1);
            }
        });

        buttonCustomPath = (Button) findViewById(R.id.buttonCustomPath);
        buttonCustomPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PathActivity.this, NotesActivity.class);
                Utilities.currentContext.pathId = -1;
                Utilities.currentContext.pathName = getResources().getString(R.string.custom_path_item);
                Utilities.currentContext.startPath = true;
                startActivityForResult(intent, 1);
            }
        });

        buttonNoPath = (Button) findViewById(R.id.buttonNoPath);
        buttonNoPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utilities.currentContext.pathId = -1;
                Utilities.currentContext.pathName = getResources().getString(R.string.no_path_item);
                Utilities.currentContext.startPath = false;

                new checkConnectionAndCheckInTask().execute();
            }
        });

        new checkConnectionAndGetPathTask().execute();

    }

    private class checkConnectionAndCheckInTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            return ConnectUtilities.hasInternetAccess(PathActivity.this);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                new CheckIn().execute();
            } else {
                StoreVehicleCheckIn();
            }
        }
    }

    private class checkConnectionAndGetPathTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            return ConnectUtilities.hasInternetAccess(PathActivity.this);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                new loadPaths().execute(Integer.toString(Utilities.currentContext.locationId));
            } else {
                GetPathsDB();
            }
        }
    }

    private void StoreVehicleCheckIn() {
        CheckInPost cip = new CheckInPost();
        cip.Action = "MOVEBIN";
        cip.LocationId = Utilities.currentContext.locationId;
        cip.BinId = Utilities.currentContext.binId;
        cip.PathId = Utilities.currentContext.pathId;
        cip.Notes = Utilities.currentContext.notes;
        cip.UserId = Utilities.currentUser.userId;
        cip.StartPath = Utilities.currentContext.startPath;
        cip.Vehicle = Utilities.currentContext.vehicle;
        cip.ScannedDate = Utilities.currentContext.scannedDate;

        Gson gson = new Gson();
        String json = gson.toJson(cip);

        dbHelper.insertVehicleEntry(json);

        Intent i = new Intent(PathActivity.this, ScanActivity.class);
        // We want to finish() the activities after ScanActivity
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.header_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                Toast toast = Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM, 0, 75);
                toast.show();

                Intent i = new Intent(this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onBackPressed() {
        Intent i = new Intent(PathActivity.this, BinActivity.class);
        i.putExtra("back", true);
        setResult(RESULT_OK, i);
        finish();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Boolean backPress = data.getBooleanExtra("back", false);

                if (backPress) {
                    listPaths.clearChoices();

                    for (int i = 0; i < listPaths.getCount(); i++) {
                        View view = listPaths.getChildAt(i);
                        TextView textView = (TextView) view.findViewById(R.id.rowTextView);
                        textView.setTextColor(getResources().getColor(R.color.colorListTextUnselected));
                        view.setBackgroundColor(getResources().getColor(R.color.colorListBgUnselected));
                    }
                }
            }
        }
    }

    public void GetPathsDB() {
        Cursor c = dbHelper.getPaths(Utilities.currentContext.locationId);
        paths = new ArrayList(c.getCount());

        if (c.moveToFirst()) {
            do {

                Path path = new Path();

                int nameIndex = c.getColumnIndex("name");
                path.name = c.getString(nameIndex);

                int pathIdIndex = c.getColumnIndex("pathId");
                path.pathId = c.getInt(pathIdIndex);

                int startPathIndex = c.getColumnIndex("startPath");
                int startPath = c.getInt(startPathIndex);
                if (startPath == 1)
                    path.startPath = true;
                else
                    path.startPath = false;

                paths.add(path);
            } while (c.moveToNext());
        }
        c.close();

        if (paths != null && paths.size() > 0) {
            ArrayAdapter<Path> adapter = new ArrayAdapter<Path>(PathActivity.this, R.layout.generic_list, paths);
            listPaths.setAdapter(adapter);
        }
    }

    private class loadPaths extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            getPaths(Integer.parseInt(params[0]));
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            GetPathsDB();
            mProgressDialog.dismiss();
        }

        private void getPaths(int locationId) {
            HttpURLConnection connection;
            InputStreamReader isr;
            URL url;
            String result;
            JSONArray responseData;

            try {
                String address = Utilities.AppURL + Utilities.PathURL + Integer.toString(locationId) + Utilities.AppURLSuffix;
                url = new URL(address);
                connection = (HttpURLConnection) url.openConnection();
                isr = new InputStreamReader(connection.getInputStream());

                if (connection.getResponseCode() == 200) {
                    dbHelper.clearPathTable(Utilities.currentContext.locationId);
                    result = Utilities.StreamToString(isr);
                    responseData = new JSONArray(result);

                    paths = new ArrayList(responseData.length());

                    for (int i = 0; i < responseData.length(); i++) {
                        JSONObject temp = responseData.getJSONObject(i);
                        dbHelper.insertPath(temp.getInt("PathId"), temp.getString("PathName"), 1, Utilities.currentContext.locationId);
                    }
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                Log.i("getPaths()", "error...");
            }
        }
    }

    private class Path {
        private String name;
        private int pathId;
        private boolean startPath;

        public Path() {

        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    private class CheckIn extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            errorMessage = "";

            if (CheckInPost()) {
                Intent i = new Intent(PathActivity.this, ScanActivity.class);

                // We want to finish() the activities after ScanActivity
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

                return null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            mProgressDialog.dismiss();
            if (!errorMessage.equals(""))
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
        }

        private boolean CheckInPost() {
            URL url;
            HttpURLConnection connection;
            OutputStreamWriter request;
            InputStreamReader isr;
            JSONObject postData;
            JSONObject responseData;
            String result;

            try {
                CheckInPost cip = new CheckInPost();
                cip.ScannerSerialNumber = Utilities.scannerSN;
                cip.Action = "MOVEBIN";
                cip.LocationId = Utilities.currentContext.locationId;
                cip.BinId = Utilities.currentContext.binId;
                cip.PathId = Utilities.currentContext.pathId;
                cip.Notes = Utilities.currentContext.notes;
                cip.UserId = Utilities.currentUser.userId;
                cip.StartPath = Utilities.currentContext.startPath;
                cip.Vehicle = Utilities.currentContext.vehicle;
                cip.Vehicle.Stock = Utilities.currentContext.Stock;
                cip.ScannedDate = Utilities.currentContext.scannedDate;

                Gson gson = new Gson();
                String json = gson.toJson(cip);

                url = new URL(Utilities.AppURL + Utilities.VehicleCheckInURL);

                connection = (HttpURLConnection) url.openConnection();
                connection.setFixedLengthStreamingMode(json.length());
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Content-type", "application/json");
                connection.setRequestMethod("POST");

                request = new OutputStreamWriter(connection.getOutputStream());
                request.write(json);
                request.flush();
                request.close();

                if (connection.getResponseCode() == 200)
                    return true;
                else {
                    //isr = new InputStreamReader(connection.getInputStream());
                    isr = new InputStreamReader(connection.getErrorStream());
                    result = Utilities.StreamToString(isr);
                    responseData = new JSONObject(result);

                    errorMessage = responseData.getString("Message");
                    Log.i("vehicle check in error", errorMessage);
                    return false;

                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }

            return false;
        }
    }

    public class CheckInPost implements Serializable {
        String ScannerSerialNumber;
        String Action;
        int LocationId;
        int BinId;
        int PathId;
        String Notes;
        int UserId;
        boolean StartPath;
        Vehicle Vehicle;
        String ScannedDate;


        CheckInPost() {
        }
    }
}
