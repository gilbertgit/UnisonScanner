package com.cphandheld.unisonscanner;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.TextView;
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
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class ScanActivity extends HeaderActivity implements EMDKListener {

    public static final String PREFS_FILE = "SharedPrefs";
    TextView textVIN;
    TextView textStatus;
    ImageView imageStatus;
    String errorMessage;
    boolean validVin = true;
    boolean locationMatch = true;
    boolean procError = false;
    boolean usesStock;

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

        SharedPreferences prefs = getSharedPreferences(PREFS_FILE, 0);
        usesStock = prefs.getBoolean("usesStock", false);

        textVIN = (TextView) findViewById(R.id.textVIN);
        textStatus = (TextView) findViewById(R.id.textStatus);
        textStatus.setText("Scan a VIN");
        imageStatus = (ImageView) findViewById(R.id.imageStatus);
        imageStatus.setVisibility(View.INVISIBLE);

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

        mProgressDialog = new ProgressDialog(ScanActivity.this);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Fetching vehicle info...");
        mProgressDialog.setMessage("Hold on a sec...");

        textStatus.setText("Scan a VIN");
        IntentFilter intentFilter = new IntentFilter(getString(R.string.scan_intent));
        //Create a our Broadcast Receiver.
        EMDKReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Get the source of the data

                String source = intent.getStringExtra(getString(R.string.datawedge_source));

                //Check if the data has come from the barcode scanner
                if (source.equalsIgnoreCase("scanner")) {
                    //Get the data from the intent
                    String data = intent.getStringExtra(getString(R.string.datawedge_data_string));

                    //Check that we have received data
                    if (data != null && data.length() > 0) {
                        String barcode = Utilities.CheckVinSpecialCases(data);

                        if (barcode.length() != 17) {
                            Toast.makeText(ScanActivity.this, "Scanned VIN is not 17 characters", Toast.LENGTH_SHORT).show();

                            textVIN.setText("- - - - - - - - - - - - - - - - -"); //reset to blank VIN, just in case
                            imageStatus.setImageResource(R.drawable.x);
                            imageStatus.setVisibility(View.VISIBLE);
                        } else {
                            textVIN.setText(barcode);
                            imageStatus.setImageResource(R.drawable.check);
                            imageStatus.setVisibility(View.VISIBLE);
                            textStatus.setText("");

                            Utilities.currentContext.vehicle = new Vehicle();
                            Utilities.currentContext.vehicle.VIN = barcode;
                            if (Utilities.isNetworkAvailable(ScanActivity.this)) {
                                mProgressDialog.show();
                                new VerifyVehicleTask().execute(barcode);
                            } else {
                                Utilities.currentContext.vehicle.Make = "";
                                Utilities.currentContext.vehicle.Model = "";
                                Utilities.currentContext.vehicle.Color = "";
                                Utilities.currentContext.binId = 0;
                                //Toast.makeText(ScanActivity.this, "Please check your internet connection.", Toast.LENGTH_LONG).show();
                                Intent i = new Intent(ScanActivity.this, BinActivity.class);
                                startActivity(i);
                            }
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

    public void onBackPressed() {
        onClosed();

        Intent i = new Intent(ScanActivity.this, LocationActivity.class);
        i.putExtra("back", true);
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Boolean backPress = data.getBooleanExtra("back", false);

                if (backPress) {
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

        if (mProfileManager != null) {
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

    private class VerifyVehicleTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {

            if (VerifyVehicle((params[0]))) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
                if (locationMatch) {
                    onClosed();
                    Intent i;
                    if(usesStock)
                        i=  new Intent(ScanActivity.this, StockActivity.class);
                    else
                        i = new Intent(ScanActivity.this, BinActivity.class);

                    startActivity(i);
                }
                return null;
            } else {
                mProgressDialog.dismiss();
                mProgressDialog = null;
                if (validVin && !procError) {
                    onClosed();
                    Intent i = new Intent(ScanActivity.this, VehicleActivity.class);
                    startActivity(i);
                }

                return null;
            }
        }

        @Override
        protected void onPostExecute(Void unused) {

            if (!procError) {
                if (!validVin) {
                    Toast.makeText(ScanActivity.this, "This is not a valid VIN.", Toast.LENGTH_LONG).show();
                    imageStatus.setImageResource(R.drawable.x);
                    imageStatus.setVisibility(View.VISIBLE);
                }
                if (!locationMatch) {
                    Toast.makeText(ScanActivity.this, "Vehicle is not in this location.", Toast.LENGTH_LONG).show();
                    imageStatus.setImageResource(R.drawable.x);
                    imageStatus.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(ScanActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                imageStatus.setImageResource(R.drawable.x);
                imageStatus.setVisibility(View.VISIBLE);
            }
        }

        private boolean VerifyVehicle(String vin) {
            URL url;
            HttpURLConnection connection;
            JSONObject responseData;
            InputStreamReader isr;
            String result;
            validVin = true;
            procError = false;

            try {
                String address = Utilities.AppURL + Utilities.VehicleInfoURL + vin;
                url = new URL(address);


                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(address);
                HttpResponse response = client.execute(request);
                int code = response.getStatusLine().getStatusCode();


//                    connection = (HttpURLConnection) url.openConnection();
//                    int status = connection.getResponseCode();
                if (code == 500) {
                    errorMessage = response.getStatusLine().getReasonPhrase();
                    isr = new InputStreamReader(response.getEntity().getContent());
                    result = Utilities.StreamToString(isr);
                    responseData = new JSONObject(result);

                    errorMessage = responseData.getString("Message");
                    procError = true;
                    return false;
                } else {
                    isr = new InputStreamReader(response.getEntity().getContent());
                }
                if (code == 200) {
                    result = Utilities.StreamToString(isr);
                    responseData = new JSONObject(result);

                    Boolean success = responseData.getBoolean("Success");

                    if (success) {
                        JSONObject veh = responseData.getJSONObject("Vehicle");
                        Utilities.currentContext.vehicle.Year = veh.getInt("Year");
                        Utilities.currentContext.vehicle.Make = veh.getString("Make");
                        Utilities.currentContext.vehicle.Model = veh.getString("Model");

                        if(veh.getString("Color") == "" || veh.getString("Color") == "null")
                            Utilities.currentContext.vehicle.Color = null;
                        else
                            Utilities.currentContext.vehicle.Color = veh.getString("Color");
                        Utilities.currentContext.binId = responseData.getInt("BinId");

                        if(veh.getString("Stock") == "null")
                            Utilities.currentContext.Stock = "";
                        else
                            Utilities.currentContext.Stock = veh.getString("Stock");

                        int loc = responseData.getInt("LocationId");

                        if (loc != 0 && Utilities.currentContext.locationId != loc)
                            locationMatch = false;
                        else
                            locationMatch = true;

                        return true;
                    } else {
                        if (responseData.getString("Message").equals("Invalid VIN"))
                            validVin = false;
                        return false;
                    }
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }

            return false;
        }
    }
}
