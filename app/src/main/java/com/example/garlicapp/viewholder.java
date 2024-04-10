package com.example.garlicapp;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class viewholder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView fieldname, datavalues, units;
    public viewholder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageview);
        fieldname = itemView.findViewById(R.id.fieldname);
        datavalues = itemView.findViewById(R.id.datavalues);
        units = itemView.findViewById(R.id.dataUnits);
    }
}