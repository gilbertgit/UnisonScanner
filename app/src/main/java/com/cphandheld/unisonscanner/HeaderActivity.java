package com.cphandheld.unisonscanner;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Supernova on 2/25/2016.
 */
abstract class HeaderActivity extends Activity
{
    TextView textUser;
    TextView textDealership;
    TextView textHeader;
    RelativeLayout headerLayout;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setHeader(int colorId, String user, String dealership, int headerId)
    {
        setHeader(colorId, user, dealership, headerId, "");
    }

    protected void setHeader(int colorId, String user, String dealership, int headerId, String headerText)
    {
        if(headerLayout == null)
            headerLayout = (RelativeLayout)findViewById(R.id.headerLayout);

        headerLayout.setBackgroundResource(colorId);

        if(textUser == null)
            textUser = (TextView)findViewById(R.id.textUser);

        String userHeader = getResources().getString(R.string.greeting) + " " + user.toUpperCase() + "!";
        textUser.setText(userHeader);

        if(textDealership == null)
            textDealership = (TextView)findViewById(R.id.textDealership);

        if (dealership.equals(""))
        {
            textDealership.setVisibility(View.INVISIBLE);
        }
        else
        {
            textDealership.setVisibility(View.VISIBLE);
            textDealership.setText(dealership.toUpperCase());
        }

        if(textHeader == null)
            textHeader = (TextView)findViewById(R.id.textHeader);

        if (!headerText.equals(""))
            textHeader.setText(headerText);
        else
            textHeader.setText(getResources().getString(headerId));
    }
}
