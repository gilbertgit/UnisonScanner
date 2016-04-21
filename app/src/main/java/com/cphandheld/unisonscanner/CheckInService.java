package com.cphandheld.unisonscanner;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * Created by titan on 4/6/16.
 */
public class CheckInService extends Service {

    public Runnable mRunnable = null;
    DBHelper dbHelper;
    String errorMessage;
    String checkInData;

    private static final int NOTIFICATION = 1009;

    @Nullable
    private NotificationManager mNotificationManager = null;
    private final NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this);

    private void setupNotifications() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, LoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                0);
        mNotificationBuilder
                .setSmallIcon(R.drawable.service_logo)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(getText(R.string.app_name))
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setOngoing(true);
    }

    private void showNotification() {
        mNotificationBuilder
                .setTicker("Unison Scanner")
                .setContentText("Unison Scanner service is running!");
        if (mNotificationManager != null) {
            mNotificationManager.notify(NOTIFICATION, mNotificationBuilder.build());
        }
    }

    public CheckInService() {

    }

    private boolean mRunning;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mRunning = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        final Handler mHandler = new Handler();

        if (!mRunning) {
            mRunning = true;
            dbHelper = new DBHelper(getApplicationContext());
            dbHelper.getWritableDatabase();
            setupNotifications();
            showNotification();
            //showNotification();
            mRunnable = new Runnable() {
                @Override
                public void run() {

                    if(Utilities.isNetworkAvailable(getApplicationContext()))
                        new CheckIn().execute();

                    mHandler.postDelayed(mRunnable, 3000);
                }
            };
            mHandler.postDelayed(mRunnable, 3000);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private String[] GetCheckIns()
    {
        Cursor c = dbHelper.getVehicleEntries();
        ArrayList<String> checkIns = new ArrayList<String>();

        if (c.moveToFirst()) {
            do {
                checkIns.add(c.getString(c.getColumnIndex("data")));

            } while (c.moveToNext());
        }
        c.close();

        return checkIns.toArray(new String[checkIns.size()]);

    }

    private class CheckIn extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
             checkInData = "";
            String[] data = GetCheckIns();
            if (!data.equals(null) && data.length != 0) {
                checkInData += "{CheckIns:[";
                for (int i = 0; i < data.length; i++)
                {
                    checkInData += data[i];

                    if(i != data.length-1)
                        checkInData += ",";
                }
                checkInData += "]}";
            }

        }

        @Override
        protected Void doInBackground(String... params) {
            errorMessage = "";

            CheckInPost();

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
//
//            if (!errorMessage.equals(""))
//                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
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

                //Gson gson = new Gson();
                //String[] data = GetCheckIns();
                if (!checkInData.equals(null) && checkInData != "") {
                    //String json = gson.toJson(data);
                    //String json = data[0].toString();
                    String json = checkInData;
                    url = new URL(Utilities.AppURL + Utilities.VehicleCheckInListURL);

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

                    int code = connection.getResponseCode();

                    if (code == 204) {
                        dbHelper.clearVehicleEntryTable();
                        return true;
                    }
                    else {
                        //isr = new InputStreamReader(connection.getInputStream());
                        isr = new InputStreamReader(connection.getErrorStream());
                        result = Utilities.StreamToString(isr);
                        responseData = new JSONObject(result);
                        //dbHelper.clearVehicleEntryTable();
                        errorMessage = responseData.getString("Message");
                        Log.i("vehicle check in error", errorMessage);
                        return false;

                    }
                }
            } catch (JSONException | IOException e) {
                errorMessage = e.getMessage();
                e.printStackTrace();
            }

            return false;
        }
    }
}
