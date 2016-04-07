package com.cphandheld.unisonscanner;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.ProfileManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by titan on 3/21/16.
 */
public class StartStopWorkflowActivity extends HeaderActivity implements EMDKManager.EMDKListener {

    TextView textVIN;
    TextView textYear;
    TextView textMake;
    TextView textModel;
    TextView textColor;
    TextView textModule;
    TextView textBinName;
    ListView listServices;
    Button buttonStartStop;
    boolean hasTicket;
    private String profileName = "StartStopWorkflow";
    private ProgressDialog mProgressDialog;

    //Declare a variable to store ProfileManager object
    private ProfileManager mProfileManager = null;
    private EMDKManager emdkManager = null;
    private BroadcastReceiver EMDKReceiver;

    private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";
    private static String ourIntentAction = "com.cphandheld.unisonscanner.RECVRBISS";
    private static String SOURCE_TAG = "com.motorolasolutions.emdk.datawedge.source";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_stop_workflow);
        setHeader(R.color.colorOrgHeader, "User", "", R.string.vehicle_info_header);

        mProgressDialog = new ProgressDialog(StartStopWorkflowActivity.this);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Fetching vehicle info...");
        mProgressDialog.setMessage("Hold on a sec...");

        textVIN = (TextView) findViewById(R.id.textVIN2);
        textYear = (TextView) findViewById(R.id.textYear2);
        textMake = (TextView) findViewById(R.id.textMake2);
        textModel = (TextView) findViewById(R.id.textModel2);
        textColor = (TextView) findViewById(R.id.textColor2);
        textBinName = (TextView) findViewById(R.id.textBin);
        textModule = (TextView) findViewById(R.id.textModule);

        buttonStartStop = (Button)findViewById(R.id.buttonStartStop);
        buttonStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String action = buttonStartStop.getText().toString();
                Intent intent = new Intent(StartStopWorkflowActivity.this, AuthorizeActivity.class);
                intent.putExtra("action", action);
                startActivity(intent);
            }
        });

        listServices = (ListView) findViewById(R.id.listServices);
        listServices.setBackgroundColor(getResources().getColor(R.color.colorDefaultBg));

        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), this);
        // Check the return status of getEMDKManager and update the status Text
        // View accordingly
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            //textStatus.setText("EMDKManager Request Failed");
        }

        Intent i = getIntent();
        handleDecodeData(i);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    public void onNewIntent(Intent i) {
       handleDecodeData(i);
    }

    private void handleDecodeData(Intent i) {
        if (i.getAction().contentEquals(ourIntentAction)) {
            //Get the source of the data
            String source = i.getStringExtra(SOURCE_TAG);

            //Check if the data has come from the barcode scanner
            if(source.equalsIgnoreCase("scanner")){
                //Get the data from the intent
                String data = i.getStringExtra(DATA_STRING_TAG);

                //Check that we have received data
                if(data != null && data.length() > 0){
                    String barcode = Utilities.CheckVinSpecialCases(data);

                    if (barcode.length() != 17)
                    {
                        Toast.makeText(StartStopWorkflowActivity.this, "Scanned VIN is not 17 characters", Toast.LENGTH_SHORT).show();
                        textVIN.setText("- - - - - - - - - - - - - - - - -"); //reset to blank VIN, just in case
                    }
                    else
                    {
                        textVIN.setText(data);
                        Utilities.currentContext.vehicle = new Vehicle();
                        Utilities.currentContext.vehicle.VIN = data;
                        new GetVehicleInfoTask().execute(data);
                    }
                }
            }
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        this.emdkManager = emdkManager;
    }

    @Override
    public void onClosed() {

    }

    private class GetVehicleInfoTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            GetVehicleInfo(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {

            textYear.setText(Integer.toString(Utilities.currentContext.vehicle.Year));
            textMake.setText(Utilities.currentContext.vehicle.Make);
            textModel.setText(Utilities.currentContext.vehicle.Model);
            String color = Utilities.currentContext.vehicle.Color;
            if(color == null || color.equals("null"))
                textColor.setText("Color Not Set");
            else
                textColor.setText(color);
            textBinName.setText(Utilities.currentContext.binName);
            if(hasTicket) {
                textModule.setText(Utilities.currentContext.vehicleTicket.moduleName);

                ArrayAdapter<VehicleTicket> adapter = new ArrayAdapter<VehicleTicket>(StartStopWorkflowActivity.this,
                        R.layout.default_textview, Utilities.currentContext.vehicleTicket.ticketServices);
                listServices.setAdapter(adapter);

                if(Utilities.currentContext.vehicleTicket.allServicesStarted) {
                    buttonStartStop.setBackgroundColor(getResources().getColor(R.color.colorStop));
                    buttonStartStop.setText("Complete");
                    buttonStartStop.setEnabled(true);
                    if(Utilities.currentContext.vehicleTicket.servicesCompleted)
                    {
                        buttonStartStop.setBackgroundColor(getResources().getColor(R.color.colorSubHeaderText));
                        buttonStartStop.setEnabled(false);
                    }
                }
                else if(!Utilities.currentContext.vehicleTicket.allServicesStarted && !Utilities.currentContext.vehicleTicket.servicesCompleted) {
                    buttonStartStop.setBackgroundColor(getResources().getColor(R.color.colorCheckInButton));
                    buttonStartStop.setText("Start");
                    buttonStartStop.setEnabled(true);
                }
                else
                {
                    buttonStartStop.setBackgroundColor(getResources().getColor(R.color.colorCheckInButton));
                    buttonStartStop.setText("Start");
                    buttonStartStop.setEnabled(true);
                }

            }
            else
            {
                textModule.setText("No Current Tickets");
                listServices.setAdapter(null);
                buttonStartStop.setBackgroundColor(getResources().getColor(R.color.colorSubHeaderText));
                buttonStartStop.setEnabled(false);
            }

            mProgressDialog.dismiss();
        }

        private boolean GetVehicleInfo(String vin) {
            URL url;
            HttpURLConnection connection;
            JSONObject responseData;
            InputStreamReader isr;
            String result;

            try
            {
                String address = Utilities.AppURL + Utilities.VehicleInfoURL + vin + Utilities.VehicleTicketSuffix;
                //String address = "http://unison-alt.cphandheld.com/" + "/api/TicketHeader/DecodeVin/" + "JTEGD21A350132131" + "/Ticket";
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
                        Utilities.currentContext.vehicle.Year = veh.getInt("Year");
                        Utilities.currentContext.vehicle.Make = veh.getString("Make");
                        Utilities.currentContext.vehicle.Model = veh.getString("Model");
                        Utilities.currentContext.vehicle.Color = veh.getString("Color");
                        Utilities.currentContext.binId = responseData.getInt("BinId");
                        Utilities.currentContext.binName = responseData.getString("BinName");
                        //Object t = responseData.getJSONObject("Ticket");
                        if(!responseData.isNull("Ticket")) {
                            JSONObject ticket = responseData.getJSONObject("Ticket");
                            JSONArray services = ticket.getJSONArray("TicketServices");
                            Utilities.currentContext.vehicleTicket = new VehicleTicket();
                            Utilities.currentContext.vehicleTicket.servicesCompleted = ticket.getBoolean("AllServicesCompleted");
                            Utilities.currentContext.vehicleTicket.allServicesStarted = ticket.getBoolean("AllServicesStarted");
                            Utilities.currentContext.vehicleTicket.moduleName = ticket.getString("ModuleName");
                            Utilities.currentContext.vehicleTicket.ticketId = ticket.getInt("TicketId");
                            Utilities.currentContext.vehicleTicket.ticketServices = new ArrayList(services.length());
                            if (services != null) {
                                for (int i = 0; i < services.length(); i++) {
                                    Utilities.currentContext.vehicleTicket.ticketServices.add(services.get(i).toString());
                                }
                            }
                            hasTicket = true;
                        }
                        else
                        {
                            hasTicket = false;
                        }
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
