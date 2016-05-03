package com.cphandheld.unisonscanner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class LocationActivity extends HeaderActivity
{
    ListView listLocations;
    ArrayList locs;

    private DBHelper dbHelper;
    ProgressDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        setHeader(R.color.colorLocHeader, Utilities.currentUser.name, "", R.string.loc_header);

        dbHelper = new DBHelper(LocationActivity.this);
        dbHelper.getWritableDatabase();

        mProgressDialog = new ProgressDialog(LocationActivity.this);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Fetching Locations...");
        mProgressDialog.setMessage("Just hold on a sec...");

        listLocations = (ListView) findViewById(R.id.listLocations);
        listLocations.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                TextView textView = (TextView) view.findViewById(R.id.rowTextView);
                textView.setTextColor(getResources().getColor(R.color.colorLocTextSelected));
                view.setBackgroundColor(getResources().getColor(R.color.colorLocBgSelected));

                Locations loc = (Locations)locs.get(position);
                int locId = loc.locationId;
                String name = loc.name;

                Intent intent = new Intent(LocationActivity.this, ScanActivity.class);
                Utilities.currentContext.locationId = locId;
                Utilities.currentContext.locationName = name;
                startActivityForResult(intent, 1);
            }
        });

//        Handler h = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//
//                if (msg.what != 1) { // code if not connected
//                    GetLocationsDB();
//                } else { // code if connected
//                    new loadLocations().execute(Integer.toString(Utilities.currentUser.organizationId));
//                }
//            }
//        };
//        ConnectUtilities.isNetworkAvailable(h,2000);

//        if (ConnectUtilities.isNetworkAvailable(LocationActivity.this)) {
//            new loadLocations().execute(Integer.toString(Utilities.currentUser.organizationId));
//        }
//        else
//        {
//            GetLocationsDB();
//        }
        new checkConnectionTask().execute();
    }

    private class checkConnectionTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            return ConnectUtilities.hasInternetAccess(LocationActivity.this);
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(result)
            {
                new loadLocations().execute(Integer.toString(Utilities.currentUser.organizationId));
            }
            else
            {
                GetLocationsDB();
            }
        }
    }

    public void onBackPressed() {
        logout();
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.header_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout()
    {
        Toast toast = Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 70);
        toast.show();

        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                Boolean backPress = data.getBooleanExtra("back", false);

                if(backPress)
                {
                    listLocations.clearChoices();

                    for (int i=0; i<listLocations.getCount(); i++)
                    {
                        View view = listLocations.getChildAt(i);
                        if(view != null) {
                            TextView textView = (TextView) view.findViewById(R.id.rowTextView);
                            textView.setTextColor(getResources().getColor(R.color.colorListTextUnselected));
                            view.setBackgroundColor(getResources().getColor(R.color.colorListBgUnselected));
                        }
                    }
                }
            }
        }
    }

    public void GetLocationsDB()
    {
        Cursor c = dbHelper.getLocations();
        locs = new ArrayList(c.getCount());

        if (c.moveToFirst()) {
            do {
                Locations loc = new Locations();
                int nameIndex = c.getColumnIndex("name");
                loc.name = c.getString(nameIndex);

                int locationIdIndex = c.getColumnIndex("locationId");
                loc.locationId = c.getInt(locationIdIndex);

                locs.add(loc);
            } while (c.moveToNext());
        }
        c.close();

        if (locs != null && locs.size() > 0) {
            ArrayAdapter<Locations> adapter = new ArrayAdapter<Locations>(LocationActivity.this, R.layout.generic_list, locs);
            listLocations.setAdapter(adapter);
        }
        mProgressDialog.dismiss();
    }

    private class loadLocations extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            mProgressDialog.show();
            //Toast.makeText(getApplicationContext(), "Loading locations...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... params) {
            getLocations(Integer.parseInt(params[0]));
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            GetLocationsDB();
        }

        private void getLocations(int organizationId) {
            HttpURLConnection connection;
            InputStreamReader isr;
            URL url;
            String result;
            JSONArray responseData;

            try {
                String address = Utilities.AppURL + Utilities.LocationsURL + Integer.toString(organizationId) + Utilities.AppURLSuffix;
                url = new URL(address);
                connection = (HttpURLConnection) url.openConnection();
                isr = new InputStreamReader(connection.getInputStream());

                if(connection.getResponseCode() == 200) {
                    dbHelper.clearLocationTable();
                    result = Utilities.StreamToString(isr);
                    responseData = new JSONArray(result);

                    locs = new ArrayList(responseData.length());

                    for (int i = 0; i < responseData.length(); i++) {

                        JSONObject temp = responseData.getJSONObject(i);

                        dbHelper.insertLocation(temp.getInt("LocationId"), temp.getString("Title"));
                    }
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                Log.i("getLocations()","error...");
            }
        }
    }

    private class Locations
    {
        private String name;
        private int locationId;

        public Locations()
        {

        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
