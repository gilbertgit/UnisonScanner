package com.cphandheld.unisonscanner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class VehicleActivity extends HeaderActivity {
    TextView textVIN;
    EditText editTextYear;
    EditText editTextMake;
    EditText editTextModel;
    EditText editTextColor;
    Button buttonSubmitInfo;

    private ProgressDialog mProgressDialog;

    String errorMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle);
        setHeader(R.color.colorVehicleHeader, Utilities.currentUser.name, Utilities.currentContext.locationName, R.string.vehicle_header);

        textVIN = (TextView) findViewById(R.id.textVIN);
        editTextYear = (EditText) findViewById(R.id.textEditYear);
        editTextMake = (EditText) findViewById(R.id.textEditMake);
        editTextModel = (EditText) findViewById(R.id.textEditModel);
        editTextColor = (EditText) findViewById(R.id.textEditColor);
        buttonSubmitInfo = (Button) findViewById(R.id.buttonSubmitInfo);
        textVIN.setText(Utilities.currentContext.vehicle.VIN);



        mProgressDialog = new ProgressDialog(VehicleActivity.this);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Uploading vehicle info...");
        mProgressDialog.setMessage("Just hold on a sec...");

        buttonSubmitInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!editTextYear.getText().equals("") && !editTextMake.getText().equals("")
                        && !editTextModel.getText().equals("") && !editTextColor.getText().equals("")) {
                    try {
                        Utilities.currentContext.vehicle.Year = Integer.parseInt(editTextYear.getText().toString());
                    } catch (NumberFormatException nfe) {
                        Toast.makeText(getApplicationContext(), "Invalid year..", Toast.LENGTH_SHORT).show();
                    }
                    Utilities.currentContext.vehicle.Make = editTextMake.getText().toString();
                    Utilities.currentContext.vehicle.Model = editTextModel.getText().toString();
                    Utilities.currentContext.vehicle.Color = editTextColor.getText().toString();

                    Intent i = new Intent(VehicleActivity.this, BinActivity.class);
                    i.putExtra("origin", "vehicle_activity");
                    startActivity(i);
                } else
                    Toast.makeText(getApplicationContext(), "Please fill and fields.", Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        int year = Utilities.currentContext.vehicle.Year;
        if(year != 0)
        {
            editTextYear.setText(String.valueOf(year));
            editTextMake.setText(Utilities.currentContext.vehicle.Make);
            editTextModel.setText(Utilities.currentContext.vehicle.Model);
            editTextColor.setText(Utilities.currentContext.vehicle.Color);
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
        Intent i = new Intent(VehicleActivity.this, ScanActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtras(b);

        startActivityForResult(i, RESULT_OK);
    }
}


