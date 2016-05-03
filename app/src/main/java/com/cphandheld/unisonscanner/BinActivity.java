package com.cphandheld.unisonscanner;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceActivity;
import android.support.annotation.BinderThread;
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
import android.widget.BaseAdapter;
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

public class BinActivity extends HeaderActivity {
    TextView textVIN;
    ListView listBins;
    ArrayList bins;
    String origin;
    private DBHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bin);
        setHeader(R.color.colorBinHeader, Utilities.currentUser.name, Utilities.currentContext.locationName, R.string.bin_header);

        dbHelper = new DBHelper(BinActivity.this);
        dbHelper.getWritableDatabase();

        Intent intent = getIntent();
        if (intent.hasExtra("origin"))
            origin = intent.getStringExtra("origin");

        textVIN = (TextView) findViewById(R.id.textVIN);
        textVIN.setText(Utilities.currentContext.vehicle.VIN);
        ArrayAdapter<Bin> aa = new ArrayAdapter<Bin>(this, R.layout.generic_list);

        listBins = (ListView) findViewById(R.id.listBins);
        listBins.setAdapter(aa);
        listBins.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view.findViewById(R.id.rowTextView);
                textView.setTextColor(getResources().getColor(R.color.colorBinTextSelected));
                view.setBackgroundColor(getResources().getColor(R.color.colorBinBgSelected));

                Bin bin = (Bin) bins.get(position);
                int binId = bin.getBinId();
                String name = bin.getName();

                Intent intent = new Intent(BinActivity.this, PathActivity.class);
                Utilities.currentContext.binId = binId;
                Utilities.currentContext.binName = name;
                startActivityForResult(intent, 1);
            }
        });

//        Handler h = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//
//                if (msg.what != 1) { // code if not connected
//                    GetBinsDB();
//                } else { // code if connected
//                    new loadBins(BinActivity.this).execute(Integer.toString(Utilities.currentContext.locationId));
//                }
//            }
//        };
//        ConnectUtilities.isNetworkAvailable(h,2000);

//        if (ConnectUtilities.isNetworkAvailable(BinActivity.this)) {
//            new loadBins(this).execute(Integer.toString(Utilities.currentContext.locationId));
//        }
//        else
//        {
//            GetBinsDB();
//        }
        new checkConnectionTask().execute();
    }

    private class checkConnectionTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            return ConnectUtilities.hasInternetAccess(BinActivity.this);
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(result)
            {
                new loadBins(BinActivity.this).execute(Integer.toString(Utilities.currentContext.locationId));
            }
            else
            {
                GetBinsDB();
            }
        }
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

    @Override
    public void onBackPressed() {
        Bundle b = new Bundle();
        b.putString("back", "yes");
        Intent i;

        if (origin != null) {
            if (origin.equals("vehicle_activity"))
                i = new Intent(BinActivity.this, VehicleActivity.class);
            else
                i = new Intent(BinActivity.this, ScanActivity.class);
        } else
            i = new Intent(BinActivity.this, ScanActivity.class);

        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtras(b);

        //setResult(RESULT_OK, i);
        // super.onBackPressed();
        startActivityForResult(i, RESULT_OK);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Boolean backPress = data.getBooleanExtra("back", false);

                if (backPress) {
                    AdapterBin ab = new AdapterBin(this, 0, bins);
                    listBins.setAdapter(ab);
                }
            }
        }
    }

    public void GetBinsDB()
    {
        Cursor c = dbHelper.getBins();
        bins = new ArrayList(c.getCount());

        if (c.moveToFirst()) {
            do {
                int nameIndex = c.getColumnIndex("name");
                String name = c.getString(nameIndex);

                int binIdIndex = c.getColumnIndex("binId");
                int binId = c.getInt(binIdIndex);
                boolean selected = false;
                if (Utilities.currentContext.binId != 0) {
                    if (binId == Utilities.currentContext.binId) {
                        selected = true;
                    }
                }
                Bin bin = new Bin(name, binId, selected);
                bins.add(bin);
            } while (c.moveToNext());
        }
        c.close();

        if (bins != null && bins.size() > 0) {
            AdapterBin ab = new AdapterBin(BinActivity.this, 0, bins);
            listBins.setAdapter(ab);

            if (Utilities.currentContext.binId != 0)
                Toast.makeText(getApplicationContext(), "Vehicle is currently in a bin.", Toast.LENGTH_LONG).show();
        }
    }

    private class loadBins extends AsyncTask<String, Void, Void> {

        private ListView listView;
        private Activity activity;

        public loadBins(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            //Toast.makeText(getApplicationContext(), "Loading bins...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... params) {

            getBins(Integer.parseInt(params[0]));
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {

            GetBinsDB();
        }

        private Void getBins(int locationId) {
            HttpURLConnection connection;
            InputStreamReader isr;
            URL url;
            String result;
            JSONArray responseData;

            try {
                String address = Utilities.AppURL + Utilities.BinsURL + Integer.toString(locationId) + Utilities.AppURLSuffix;
                url = new URL(address);
                connection = (HttpURLConnection) url.openConnection();
                isr = new InputStreamReader(connection.getInputStream());

                if (connection.getResponseCode() == 200) {
                    dbHelper.clearBinTable();
                    result = Utilities.StreamToString(isr);
                    responseData = new JSONArray(result);

                    bins = new ArrayList(responseData.length());

                    for (int i = 0; i < responseData.length(); i++) {
                        JSONObject temp = responseData.getJSONObject(i);
                        String name = temp.getString("BinName");
                        int binId = temp.getInt("BinId");

                        // insert into database table Bin
                        dbHelper.insertBin(binId, name);
                    }
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                Log.i("getBins()", "error...");
                return null;
            }
            return null;
        }

    }

}
