package com.cphandheld.unisonscanner;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.TextView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import android.widget.Toast;

import com.symbol.emdk.*;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class ScanActivity extends HeaderActivity implements EMDKListener
{

    TextView textVIN;
    TextView textStatus;
    ImageView imageStatus;

    private ProgressDialog mProgressDialog;

    private String TAG = "ScanActivity";

    //Assign the profile name used in EMDKConfig.xml
    private String profileName = "DataCaptureProfile";

    //Declare a variable to store ProfileManager object
    private ProfileManager mProfileManager = null;
    private EMDKManager emdkManager = null;

    private BroadcastReceiver EMDKReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        setHeader(R.color.colorScanHeader, Utilities.currentUser.name, Utilities.currentContext.locationName, R.string.scan_header);

        textVIN = (TextView) findViewById(R.id.textVIN);
        textStatus = (TextView) findViewById(R.id.textStatus);
        textStatus.setText("Scan a VIN");
        imageStatus = (ImageView) findViewById(R.id.imageStatus);
        imageStatus.setVisibility(View.INVISIBLE);

        mProgressDialog = new ProgressDialog(ScanActivity.this);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Fetching vehicle info...");
        mProgressDialog.setMessage("Just hold on a sec...");


        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), this);
        // Check the return status of getEMDKManager and update the status Text
        // View accordingly
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            textStatus.setText("EMDKManager Request Failed");
        }
    }

    @Override
    protected void onResume() {
// TODO Auto-generated method stub
        super.onResume();

        textStatus.setText("Scan a VIN");
        IntentFilter intentFilter = new IntentFilter("com.cphandheld.unisonscanner.RECVRBI");
        //Create a our Broadcast Receiver.
        EMDKReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Get the source of the data
                String source = intent.getStringExtra("com.motorolasolutions.emdk.datawedge.source");

                //Check if the data has come from the barcode scanner
                if(source.equalsIgnoreCase("scanner")){
                    //Get the data from the intent
                    String data = intent.getStringExtra("com.motorolasolutions.emdk.datawedge.data_string");

                    //Check that we have received data
                    if(data != null && data.length() > 0){
                        String barcode = CheckVinSpecialCases(data);

                        if (barcode.length() != 17)
                        {
                            Toast.makeText(ScanActivity.this, "Scanned VIN is not 17 characters", Toast.LENGTH_SHORT).show();

                            textVIN.setText("- - - - - - - - - - - - - - - - -"); //reset to blank VIN, just in case
                            imageStatus.setImageResource(R.drawable.x);
                            imageStatus.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            textVIN.setText(data);
                            imageStatus.setImageResource(R.drawable.check);
                            imageStatus.setVisibility(View.VISIBLE);
                            textStatus.setText("");

                            Utilities.currentContext.vehicle = new Vehicle();
                            Utilities.currentContext.vehicle.vin = data;
                            new VerifyVehicleTask().execute(data);
                        }
                    }
                }
            }
        };
        //Register our receiver.
        this.registerReceiver(EMDKReceiver, intentFilter);
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.header_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                onClosed();

                Toast toast = Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM, 0, 75);
                toast.show();

                Intent i = new Intent(ScanActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onBackPressed()
    {
        onClosed();

        Intent i = new Intent(ScanActivity.this, LocationActivity.class);
        i.putExtra("back", true);
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.i(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                Boolean backPress = data.getBooleanExtra("back", false);

                if(backPress)
                {
                    textVIN.setText(R.string.empty_vin);
                    imageStatus.setVisibility(View.INVISIBLE);
                    onOpened(emdkManager);
                }
            }
        }
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
       this.emdkManager = emdkManager;


        mProfileManager = (ProfileManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.PROFILE);

        if(mProfileManager != null) {
            try {

                String[] modifyData = new String[1];
                //Call processProfile with profile name and SET flag to create the profile. The modifyData can be null.

                EMDKResults results = mProfileManager.processProfile(profileName, ProfileManager.PROFILE_FLAG.SET, modifyData);
                if (results.statusCode == EMDKResults.STATUS_CODE.FAILURE) {
                    //Failed to set profile
                }
            } catch (Exception ex) {
                // Handle any exception
            }
        }
    }

    @Override
    public void onClosed() {

    }

    @Override
    protected void onPause() {
        super.onPause();

        //Register our receiver.
        this.unregisterReceiver(this.EMDKReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    private String CheckVinSpecialCases(String vin)
    {
        String formattedVIN = vin;

        if (vin.length() > 17)
        {
            if (vin.substring(0, 1).toUpperCase().equals("I") || vin.substring(0, 1).toUpperCase().equals("A") || vin.substring(0,1).equals(" ")) // Ford, Mazda, Honda Issues
                formattedVIN = vin.substring(1, 17);
            else if (vin.length() == 18)
                formattedVIN = vin.substring(0, 17); // Lexus Issue
        }

        return formattedVIN;
    }

    private class VerifyVehicleTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            mProgressDialog.show();
            //Toast.makeText(getApplicationContext(), "Verifying vehicle information...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... params) {

            if (VerifyVehicle((params[0])))
            {
                onClosed();
                Intent i = new Intent(ScanActivity.this, BinActivity.class);
                startActivity(i);
                return null;
            }
            else
            {
                onClosed();
                Intent i = new Intent(ScanActivity.this, VehicleActivity.class);
                startActivity(i);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Void unused) {
            mProgressDialog.dismiss();
        }

        private boolean VerifyVehicle(String vin) {
                URL url;
                HttpURLConnection connection;
                JSONObject responseData;
                InputStreamReader isr;
                String result;

                try
                {
                    String address = Utilities.AppURL + Utilities.VehicleInfoURL + vin;
                    url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    isr = new InputStreamReader(connection.getInputStream());

                    if (connection.getResponseCode() == 200)
                    {
                        result = Utilities.StreamToString(isr);
                        responseData = new JSONObject(result);

                        Boolean success = responseData.getBoolean("Success");

                        if (success)
                        {
                            JSONObject veh = responseData.getJSONObject("Vehicle");
                            Utilities.currentContext.vehicle.year = veh.getInt("Year");
                            Utilities.currentContext.vehicle.make = veh.getString("Make");
                            Utilities.currentContext.vehicle.model = veh.getString("Model");
                            Utilities.currentContext.vehicle.color = veh.getString("Color");

                            Utilities.currentContext.binId = responseData.getInt("BinId");
                            return true;
                        }
                        else
                            return false;
                    }
                    else
                        return false;
                } catch (JSONException | IOException e)
                {
                    e.printStackTrace();
                }

            return false;
        }
    }
}
