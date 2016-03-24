package com.cphandheld.unisonscanner;


import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by titan on 3/11/16.
 */
public class AdapterBin extends ArrayAdapter<Bin> {

    ArrayList<Bin> mBins = new ArrayList<Bin>();
    private LayoutInflater mInflater;
    Context mContext;

    public AdapterBin(Context context, int resource, ArrayList<Bin> bins) {
        super(context, resource, bins);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mBins = bins;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Inflate view for each element in list.
        convertView = mInflater.inflate(R.layout.generic_list, null);

        //Get details for bin
        Bin bin = mBins.get(position);

        ((TextView) convertView.findViewById(R.id.rowTextView)).setText(bin.getName());

        TextView status = (TextView) convertView.findViewById(R.id.rowTextView);

        if(bin.isSelected()){
            status.setBackgroundColor(mContext.getResources().getColor(R.color.colorBinBgSelected));
        }else{

            status.setBackgroundColor(Color.WHITE);
        }

        return convertView;
    }

}

