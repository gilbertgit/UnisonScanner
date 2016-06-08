package com.cphandheld.unisonscanner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    CheckBox checkStock;
    private ProgressDialog mProgressDialog;

    private DBHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization);
        setHeader(R.color.colorOrgHeader, getResources().getString(R.string.hello_admin), "", R.string.org_header);

        dbHelper = new DBHelper(OrganizationActivity.this);
        dbHelper.getWritableDatabase();

        mProgressDialog = new ProgressDialog(OrganizationActivity.this);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Fetching organizations...");
        mProgressDialog.setMessage("Just hold on a sec...");

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
                    appUrl = "http://unison-stage.cphandheld.com/";
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

        checkStock = (CheckBox)findViewById(R.id.checkboxStock);
        checkStock.setChecked(settings.getBoolean("usesStock", false));
        checkStock.setOnCheckedChangeListener(new myCheckBoxChangeClicker());

        new checkConnectionAndGetOrgTask().execute();
    }

    private class checkConnectionAndGetOrgTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            return ConnectUtilities.hasInternetAccess(OrganizationActivity.this);
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(result)
            {
                new loadOrganizations().execute();
            }
            else
            {
                GetOrganizationsDB();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If we've received a touch notification that the user has touched
        // outside the app, finish the activity.
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            finish();
            return true;
        }

        // Delegate everything else to Activity.
        return super.onTouchEvent(event);
    }

    class myCheckBoxChangeClicker implements CheckBox.OnCheckedChangeListener
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SharedPreferences settings = getSharedPreferences(PREFS_FILE, 0);
            SharedPreferences.Editor editor = settings.edit();
            if(isChecked) {
                editor.putBoolean("usesStock", true);
            }
            else
            {
                editor.putBoolean("usesStock", false);
            }
            editor.commit();
        }
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

    public void GetOrganizationsDB()
    {
        Cursor c = dbHelper.getOrganizations();
        orgs = new ArrayList(c.getCount());

        if (c.moveToFirst()) {
            do {

                Organization org = new Organization();

                int nameIndex = c.getColumnIndex("name");
                org.name = c.getString(nameIndex);

                int organizationIdIndex = c.getColumnIndex("organizationId");
                org.organizationId = c.getInt(organizationIdIndex);

                orgs.add(org);
            } while (c.moveToNext());
        }
        c.close();

        if (orgs != null && orgs.size() > 0) {
            ArrayAdapter<Organization> adapter = new ArrayAdapter<Organization>(OrganizationActivity.this, R.layout.generic_list, orgs);
            listOrganizations.setAdapter(adapter);
        }
        mProgressDialog.dismiss();
    }

    private class loadOrganizations extends AsyncTask<String, Void, Void>
    {

        @Override
        protected void onPreExecute() {
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            getOrganizations();
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            GetOrganizationsDB();
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
                    dbHelper.clearOrganizationTable();
                    result = Utilities.StreamToString(isr);
                    responseData = new JSONArray(result);

                    orgs = new ArrayList(responseData.length());

                    for (int i = 0; i < responseData.length(); i++) {
                        JSONObject temp = responseData.getJSONObject(i);

                        dbHelper.insertOrganization(temp.getInt("OrganizationId"), temp.getString("Name"));
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
