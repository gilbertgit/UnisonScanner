package com.cphandheld.unisonscanner;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Button;
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

public class OrganizationActivity extends HeaderActivity
{
    public static final String PREFS_FILE = "SharedPrefs";
    ListView listOrganizations;
    ArrayList orgs;
    TextView textUrl;
    Button buttonChangeUrl;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization);
        setHeader(R.color.colorOrgHeader, getResources().getString(R.string.hello_admin), "", R.string.org_header);

        SharedPreferences settings = getSharedPreferences(PREFS_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();

        textUrl = (TextView) findViewById(R.id.textUrl);
        // Set the default app url
        String appUrl = settings.getString("appUrl", "");
        if(appUrl.equals("")) {
            appUrl = "http://unison-dev.cphandheld.com/";
            textUrl.setText("UNISON 2.0");
            Utilities.SetAppUrl(appUrl);
            editor.putString("appUrl", appUrl);
            editor.commit();
        }
        else if(appUrl.equals("http://unison-dev.cphandheld.com/"))
            textUrl.setText("UNISON 2.0");
        else
            textUrl.setText("UNISON 1.0");



        buttonChangeUrl = (Button) findViewById(R.id.buttonChangeUrl);
        buttonChangeUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences settings = getSharedPreferences(PREFS_FILE, 0);
                SharedPreferences.Editor editor = settings.edit();

                String appUrl = settings.getString("appUrl", "");
                if(appUrl.equals("http://unison-dev.cphandheld.com/")) {
                    // change it to Unison 1.0 database
                    textUrl.setText("UNISON 1.0");
                    appUrl = "http://unison.cphandheld.com/";
                }
                else {
                    // change it to Unison 2.0 database
                    textUrl.setText("UNISON 2.0");
                    appUrl = "http://unison-dev.cphandheld.com/";
                }

                // commit the changes
                Utilities.SetAppUrl(appUrl);
                editor.putString("appUrl", appUrl);
                editor.commit();

                Toast.makeText(getApplicationContext(), "Please select an organization", Toast.LENGTH_LONG).show();

                // Refresh the list of organizations
                new loadOrganizations().execute();
            }
        });

        listOrganizations = (ListView) findViewById(R.id.listOrganizations);
        listOrganizations.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                TextView textView = (TextView) view.findViewById(R.id.rowTextView);
                textView.setTextColor(getResources().getColor(R.color.colorOrgTextSelected));
                view.setBackgroundColor(getResources().getColor(R.color.colorOrgBgSelected));

                Organization org = (Organization)orgs.get(position);
                int orgId = org.organizationId;
                String name = org.name;

                SharedPreferences settings = getSharedPreferences(PREFS_FILE, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("orgId", orgId);
                editor.putString("orgName", name);
                editor.commit();

                Intent i = new Intent(OrganizationActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

        new loadOrganizations().execute();
    }

    public void onBackPressed()
    {
        Toast toast = Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 75);
        toast.show();

        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

    private class loadOrganizations extends AsyncTask<String, Void, Void>
    {

        @Override
        protected void onPreExecute() {
            //Toast.makeText(getApplicationContext(), "Loading organizations...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... params) {
            getOrganizations();
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            if (orgs != null && orgs.size() > 0) {
                ArrayAdapter<Organization> adapter = new ArrayAdapter<Organization>(OrganizationActivity.this, R.layout.generic_list, orgs);
                listOrganizations.setAdapter(adapter);
            }
        }

        private void getOrganizations() {
            HttpURLConnection connection;
            InputStreamReader isr;
            URL url;
            String result;
            JSONArray responseData;

            try {
                String address = Utilities.AppURL + Utilities.OrganizationsURL + Utilities.AppURLSuffix;
                url = new URL(address);
                connection = (HttpURLConnection) url.openConnection();
                isr = new InputStreamReader(connection.getInputStream());

                if(connection.getResponseCode() == 200) {
                    result = Utilities.StreamToString(isr);
                    responseData = new JSONArray(result);

                    orgs = new ArrayList(responseData.length());

                    for (int i = 0; i < responseData.length(); i++) {
                        Organization org = new Organization();
                        JSONObject temp = responseData.getJSONObject(i);
                        org.name = temp.getString("Name");
                        org.organizationId = temp.getInt("OrganizationId");
                        orgs.add(org);
                    }
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                Log.i("getOrganizations()","error...");
            }
        }
    }

    private class Organization
    {
        private String name;
        private int organizationId;

        public Organization()
        {

        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
