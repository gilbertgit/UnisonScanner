package com.cphandheld.unisonscanner;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Supernova on 2/16/2016.
 */
public class Utilities {

    // static values
    public static final String PREFS_FILE = "SharedPrefs";
    public static String AppURL = "http://unison-dev.cphandheld.com/";
    public static final String LoginURL = "api/Users/ScannerLogin/";
    public static final String LocationsURL = "api/Locations/Organization/";
    public static final String OrganizationsURL = "api/Organizations";
    public static final String AppURLSuffix = "/CheckInApp";
    public static final String BinsURL = "api/Bins/Location/";
    public static final String PathURL = "api/PathTemplate/Location/";
    public static final String VehicleInfoURL = "api/TicketHeader/DecodeVin/";
    public static final String VehicleCheckInURL = "api/Inventory/CheckIn";

    public static User currentUser = new User();
    public static CurrentContext currentContext = new CurrentContext();

    public static String StreamToString(InputStreamReader isr) throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader( isr);
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        bufferedReader.close();
        return result;

    }

    // This is temporary function for NADA
    public static void SetAppUrl(String url)
    {
        AppURL = url;
    }

}
