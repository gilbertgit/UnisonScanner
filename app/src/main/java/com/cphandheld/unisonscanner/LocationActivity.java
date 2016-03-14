package com.cphandheld.unisonscanner;

import android.content.Intent;
import android.os.AsyncTask;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        setHeader(R.color.colorLocHeader, Utilities.currentUser.name, "", R.string.loc_header);

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

        new loadLocations().execute(Integer.toString(Utilities.currentUser.organizationId));
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
                        TextView textView = (TextView) view.findViewById(R.id.rowTextView);
                        textView.setTextColor(getResources().getColor(R.color.colorListTextUnselected));
                        view.setBackgroundColor(getResources().getColor(R.color.colorListBgUnselected));
                    }
                }
            }
        }
    }

    private class loadLocations extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            //Toast.makeText(getApplicationContext(), "Loading locations...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... params) {
            getLocations(Integer.parseInt(params[0]));
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
              if (locs != null && locs.size() > 0) {
                  ArrayAdapter<Locations> adapter = new ArrayAdapter<Locations>(LocationActivity.this, R.layout.generic_list, locs);
                  listLocations.setAdapter(adapter);
              }
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
                    result = Utilities.StreamToString(isr);
                    responseData = new JSONArray(result);

                    locs = new ArrayList(responseData.length());

                    for (int i = 0; i < responseData.length(); i++) {
                        Locations loc = new Locations();
                        JSONObject temp = responseData.getJSONObject(i);
                        loc.name = temp.getString("Title");
                        loc.locationId = temp.getInt("LocationId");
                        locs.add(loc);
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
