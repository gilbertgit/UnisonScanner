package com.cphandheld.unisonscanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by titan on 4/14/16.
 */
public class StockActivity extends HeaderActivity {

    TextView textViewVin;
    EditText editTextStock;
    Button  buttonNext;
    String origin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);
        setHeader(R.color.colorScanHeader, Utilities.currentUser.name, Utilities.currentContext.locationName, R.string.scan_header);

        Intent intent = getIntent();
        if (intent.hasExtra("origin"))
            origin = intent.getStringExtra("origin");

        textViewVin = (TextView) findViewById(R.id.textVIN);
        textViewVin.setText(Utilities.currentContext.vehicle.VIN);
        editTextStock = (EditText) findViewById(R.id.textStock);

        editTextStock.setText(Utilities.currentContext.Stock);

        buttonNext = (Button) findViewById(R.id.buttonNext);

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StockActivity.this, BinActivity.class);
                Utilities.currentContext.Stock = editTextStock.getText().toString();
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Bundle b = new Bundle();
        b.putString("back", "yes");
        Intent i;

        if (origin != null) {
            if (origin.equals("vehicle_activity"))
                i = new Intent(StockActivity.this, VehicleActivity.class);
            else
                i = new Intent(StockActivity.this, ScanActivity.class);
        } else
            i = new Intent(StockActivity.this, ScanActivity.class);

        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtras(b);

        //setResult(RESULT_OK, i);
        // super.onBackPressed();
        startActivityForResult(i, RESULT_OK);
    }
}
