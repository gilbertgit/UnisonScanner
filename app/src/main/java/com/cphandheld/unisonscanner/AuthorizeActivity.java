package com.cphandheld.unisonscanner;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by titan on 3/22/16.
 */
public class AuthorizeActivity extends ActionBarActivity {

    public static final String PREFS_FILE = "SharedPrefs";
    ImageView imageButton1;
    ImageView imageButton2;
    ImageView imageButton3;
    ImageView imageButton4;
    ImageView imageButton5;
    ImageView imageButton6;
    ImageView imageButton7;
    ImageView imageButton8;
    ImageView imageButton9;
    ImageView imageButton0;
    ImageView imageEntry1;
    ImageView imageEntry2;
    ImageView imageEntry3;
    ImageView imageEntry4;
    ImageView imageBack;

    ProgressDialog mProgressDialog;

    int organizationId = -1;
    int ticketId = 0;
    int statusId = 0;
    int userId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorize);

        Intent intent = getIntent();
        String action = intent.getStringExtra("action");


        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_FILE, 0);
        organizationId = settings.getInt("orgId", -1);

        ticketId = Utilities.currentContext.vehicleTicket.ticketId;
        if(action.equals("Start"))
            statusId = 1;
        else if(action.equals("Complete"))
            statusId = 3;

        mProgressDialog = new ProgressDialog(AuthorizeActivity.this);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Hold on a sec...");

        imageEntry1 = (ImageView) findViewById(R.id.entry1);
        imageEntry2 = (ImageView) findViewById(R.id.entry2);
        imageEntry3 = (ImageView) findViewById(R.id.entry3);
        imageEntry4 = (ImageView) findViewById(R.id.entry4);

        setClickEvents();
    }


    protected void setClickEvents()
    {
        imageButton1 = (ImageView) findViewById(R.id.button1);
        imageButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEntry((String) view.getTag());
            }
        });

        imageButton2 = (ImageView) findViewById(R.id.button2);
        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEntry((String) view.getTag());
            }
        });

        imageButton3 = (ImageView) findViewById(R.id.button3);
        imageButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEntry((String) view.getTag());
            }
        });

        imageButton4 = (ImageView) findViewById(R.id.button4);
        imageButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEntry((String) view.getTag());
            }
        });

        imageButton5 = (ImageView) findViewById(R.id.button5);
        imageButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEntry((String) view.getTag());
            }
        });

        imageButton6 = (ImageView) findViewById(R.id.button6);
        imageButton6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEntry((String) view.getTag());
            }
        });

        imageButton7 = (ImageView) findViewById(R.id.button7);
        imageButton7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEntry((String) view.getTag());
            }
        });

        imageButton8 = (ImageView) findViewById(R.id.button8);
        imageButton8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEntry((String) view.getTag());
            }
        });

        imageButton9 = (ImageView) findViewById(R.id.button9);
        imageButton9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEntry((String) view.getTag());
            }
        });

        imageButton0 = (ImageView) findViewById(R.id.button0);
        imageButton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEntry((String) view.getTag());
            }
        });

        imageBack = (ImageView) findViewById(R.id.buttonBack);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteEntry();
            }
        });
    }
    protected void setEntry(String tag) {
        if (imageEntry1.getTag() == null) {
            imageEntry1.setTag(tag);
            imageEntry1.setImageResource(R.drawable.yes_pin);
        } else if (imageEntry2.getTag() == null) {
            imageEntry2.setTag(tag);
            imageEntry2.setImageResource(R.drawable.yes_pin);
        } else if (imageEntry3.getTag() == null) {
            imageEntry3.setTag(tag);
            imageEntry3.setImageResource(R.drawable.yes_pin);
        } else if (imageEntry4.getTag() == null) {
            imageEntry4.setTag(tag);
            imageEntry4.setImageResource(R.drawable.yes_pin);
            String pin = (String)imageEntry1.getTag() + (String)imageEntry2.getTag() + (String)imageEntry3.getTag() + tag;
            new LoginTask().execute(Integer.toString(organizationId), pin);
        }
    }

    protected void deleteEntry() {
        if (imageEntry4.getTag() != null) {
            imageEntry4.setTag(null);
            imageEntry4.setImageResource(R.drawable.btn_no_pin);
        } else if (imageEntry3.getTag() != null) {
            imageEntry3.setTag(null);
            imageEntry3.setImageResource(R.drawable.btn_no_pin);
        } else if (imageEntry2.getTag() != null) {
            imageEntry2.setTag(null);
            imageEntry2.setImageResource(R.drawable.btn_no_pin);
        } else if (imageEntry1.getTag() != null) {
            imageEntry1.setTag(null);
            imageEntry1.setImageResource(R.drawable.btn_no_pin);
        }
    }

    private class StartStopTask extends AsyncTask<String, Void, Void>
    {
        @Override
        protected void onPreExecute() {
            mProgressDialog.setTitle("Posting data...");
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            if (StartStopPost())
            {
                finishAffinity();
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            mProgressDialog.dismiss();
        }

        private boolean StartStopPost()
        {
            URL url;
            HttpURLConnection connection;
            OutputStreamWriter request;
            JSONObject postData;

            try
            {
                postData = new JSONObject();
                postData.accumulate("TicketId", ticketId);
                postData.accumulate("StatusId", statusId);
                postData.accumulate("UserId", userId);

                url = new URL(Utilities.AppURL + Utilities.StartStopURL);

                connection = (HttpURLConnection) url.openConnection();
                connection.setFixedLengthStreamingMode(postData.toString().length());
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Content-type", "application/json");
                connection.setRequestMethod("POST");

                request = new OutputStreamWriter(connection.getOutputStream());
                request.write(postData.toString());
                request.flush();
                request.close();

                int code = connection.getResponseCode();

                if (connection.getResponseCode() == 204)
                {
                    return true;
                } else
                    return false;
            } catch (JSONException | IOException e)
            {
                e.printStackTrace();
            }

            return false;
        }
    }

    private class LoginTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            mProgressDialog.setTitle("Verifying your credentials...");
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            Utilities.currentUser = null;
            if (LoginPost(Integer.parseInt(params[0]), params[1]))
            {
                    return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            if (Utilities.currentUser == null) {
                Toast toast = Toast.makeText(getApplicationContext(), "Invalid PIN", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM, 0, 75);
                toast.show();

                imageEntry1.setImageResource(R.drawable.pin_x);
                imageEntry2.setImageResource(R.drawable.pin_x);
                imageEntry3.setImageResource(R.drawable.pin_x);
                imageEntry4.setImageResource(R.drawable.pin_x);

                final Vibrator vibe = (Vibrator) AuthorizeActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(200);


                YoYo.with(Techniques.Shake)
                        .duration(1000)
                        .playOn(imageEntry1);

                YoYo.with(Techniques.Shake)
                        .duration(1000)
                        .playOn(imageEntry2);

                YoYo.with(Techniques.Shake)
                        .duration(1000)
                        .playOn(imageEntry3);

                YoYo.with(Techniques.Shake)
                        .duration(1000)
                        .playOn(imageEntry4);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        imageEntry1.setImageResource(R.drawable.btn_no_pin);
                        imageEntry2.setImageResource(R.drawable.btn_no_pin);
                        imageEntry3.setImageResource(R.drawable.btn_no_pin);
                        imageEntry4.setImageResource(R.drawable.btn_no_pin);
                        imageEntry1.setTag(null);
                        imageEntry2.setTag(null);
                        imageEntry3.setTag(null);
                        imageEntry4.setTag(null);
                    }
                }, 1500);
            }
            else
                new StartStopTask().execute();


            mProgressDialog.dismiss();
        }

        private boolean LoginPost(int organizationId, String pin) {

                URL url;
                HttpURLConnection connection;
                OutputStreamWriter request;
                JSONObject responseData;
                JSONObject postData;
                InputStreamReader isr;
                String result;

                try
                {
                    postData = new JSONObject();
                    postData.accumulate("OrganizationId", organizationId);
                    postData.accumulate("Pin", pin);

                    url = new URL(Utilities.AppURL + Utilities.LoginURL);

                    connection = (HttpURLConnection) url.openConnection();
                    connection.setFixedLengthStreamingMode(postData.toString().length());
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Content-type", "application/json");
                    connection.setRequestMethod("POST");

                    request = new OutputStreamWriter(connection.getOutputStream());
                    request.write(postData.toString());
                    request.flush();
                    request.close();

                    if (connection.getResponseCode() == 200)
                    {
                        isr = new InputStreamReader(connection.getInputStream());
                        result = Utilities.StreamToString(isr);
                        responseData = new JSONObject(result);

                        Utilities.currentUser = new User();
                        Utilities.currentUser.organizationId = organizationId;
                        Utilities.currentUser.userId = responseData.getInt("UserId");
                        Utilities.currentUser.name = responseData.getString("Name");

                        Utilities.currentContext = new CurrentContext();
                        Utilities.currentContext.organizationId = organizationId;

                        userId = Utilities.currentUser.userId;

                        return true;
                    } else
                        return false;
                } catch (JSONException | IOException e)
                {
                    e.printStackTrace();
                }

            return false;
        }
    }
}
