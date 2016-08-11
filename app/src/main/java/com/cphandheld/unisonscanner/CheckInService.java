package com.cphandheld.unisonscanner;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by titan on 4/6/16.
 */
public class CheckInService extends Service {

    public Runnable mRunnable = null;
    DBHelper dbHelper;
    String errorMessage;
    String checkInData;
    String[] checkins;
    List<Integer> checkinsToRemove;

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

//            if(ConnectUtilities.isNetworkAvailable(getApplicationContext())) {
//                Handler h = new Handler() {
//                    @Override
//                    public void handleMessage(Message msg) {
//
//                        if (msg.what != 1) { // code if not connected
//
//                        } else { // code if connected
//                            new CheckIn().execute();
//                        }
//                    }
//                };
//                ConnectUtilities.isNetworkAvailable(h, 3000);
//            }

            mRunnable = new Runnable() {
                @Override
                public void run() {

                    new checkConnectionTask().execute();

                    mHandler.postDelayed(mRunnable, 3000);
                }
            };
            mHandler.postDelayed(mRunnable, 3000);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private class checkConnectionTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            return ConnectUtilities.hasInternetAccess(getApplicationContext());
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                checkins = GetCheckIns();
                if (!checkins.equals(null) && checkins.length != 0) {
                    new CheckIn().execute();
                }
            }
        }
    }


    private String[] GetCheckIns()
    {
        Cursor c = dbHelper.getVehicleEntries();
        ArrayList<String> checkIns = new ArrayList<String>();
        checkinsToRemove = new ArrayList<Integer>();

        if (c.moveToFirst()) {
            do {
                checkIns.add(c.getString(c.getColumnIndex("data")));
                checkinsToRemove.add(c.getInt(0));

            } while (c.moveToNext());
        }
        c.close();

        return checkIns.toArray(new String[checkIns.size()]);

    }

    private class CheckIn extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
             checkInData = "";

            if (!checkins.equals(null) && checkins.length != 0) {
                checkInData += "{\"ScannerSerialNumber\":\"" + Utilities.scannerSN + "\",\"CheckIns\":[";
                for (int i = 0; i < checkins.length; i++)
                {
                    checkInData += checkins[i];

                    if(i != checkins.length-1)
                        checkInData += ",";
                }
                checkInData += "]}";
            }

        }

        @Override
        protected Boolean doInBackground(String... params) {
            errorMessage = "";

            return CheckInPost();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result)
            {
                StringBuilder strbul  = new StringBuilder();
                Iterator<Integer> iter = checkinsToRemove.iterator();
                while(iter.hasNext())
                {
                    strbul.append(iter.next());
                    if(iter.hasNext()){
                        strbul.append(",");
                    }
                }
                strbul.toString();
                dbHelper.clearVehicleEntryTable(strbul.toString());
            }
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
