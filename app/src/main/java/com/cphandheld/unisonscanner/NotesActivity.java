package com.cphandheld.unisonscanner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

public class NotesActivity extends HeaderActivity {
    TextView textVIN;
    TextView textBin;
    TextView textNotesHeader;
    EditText textNotes;
    Button buttonCheckIn;

    String errorMessage;

    private ProgressDialog mProgressDialog;
    private DBHelper dbHelper;
    private CheckInPost cip;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        setHeader(R.color.colorNotesHeader, Utilities.currentUser.name, Utilities.currentContext.locationName, -1, Utilities.currentContext.pathName.toUpperCase());

        dbHelper = new DBHelper(NotesActivity.this);
        dbHelper.getWritableDatabase();

        textVIN = (TextView) findViewById(R.id.textVIN);
        textVIN.setText(Utilities.currentContext.vehicle.VIN);

        textBin = (TextView) findViewById(R.id.textBin);
        textBin.setText(Utilities.currentContext.binName.toUpperCase());

        textNotesHeader = (TextView) findViewById(R.id.textNotesHeader);
        String notesHeader = getResources().getString(R.string.notes_text_header);
        textNotesHeader.setText(notesHeader);

        textNotes = (EditText) findViewById(R.id.textNotes);

        buttonCheckIn = (Button) findViewById(R.id.buttonCheckIn);
        buttonCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utilities.currentContext.notes = textNotes.getText().toString();

                cip = new CheckInPost();
                cip.ScannerSerialNumber = Utilities.scannerSN;
                cip.Action = "CHECKIN";
                cip.LocationId = Utilities.currentContext.locationId;
                cip.BinId = Utilities.currentContext.binId;
                cip.PathId = Utilities.currentContext.pathId;
                cip.Notes = Utilities.currentContext.notes;
                cip.UserId = Utilities.currentUser.userId;
                cip.StartPath = Utilities.currentContext.startPath;
                cip.Vehicle = Utilities.currentContext.vehicle;
                cip.Vehicle.Stock = Utilities.currentContext.Stock;
                cip.ScannedDate = Utilities.currentContext.scannedDate;
                cip.Latitude = Utilities.currentContext.latitude;
                cip.Longitude = Utilities.currentContext.longitude;

                StoreVehicleCheckIn();

                Intent i = new Intent(NotesActivity.this, ScanActivity.class);
                // We want to finish() the activities after ScanActivity
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
               // new CheckIn().execute();
            }
        });

        mProgressDialog = new ProgressDialog(NotesActivity.this);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Checking in vehicle...");
        mProgressDialog.setMessage("Hold on a sec...");
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

    private void StoreVehicleCheckIn()
    {
        Gson gson = new Gson();
        String json = gson.toJson(cip);
        dbHelper.insertVehicleEntry(json);
    }

    public void onBackPressed() {
        Intent i = new Intent(NotesActivity.this, PathActivity.class);
        i.putExtra("back", true);
        setResult(RESULT_OK, i);
        finish();
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
                Intent i = new Intent(NotesActivity.this, ScanActivity.class);

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
}
