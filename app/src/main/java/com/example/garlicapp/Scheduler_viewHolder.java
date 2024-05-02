package com.example.garlicapp;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.bson.types.ObjectId;

public class Scheduler_viewHolder extends RecyclerView.ViewHolder {
    ImageView delete_button;
    TextView date, timestart, timeend;
    private ScheduleAdapter scheduleAdapter;
    private ScheduleAdapter.OnItemClickListener listener;
    private ObjectId objectId;
    public Scheduler_viewHolder(@NonNull View itemView, ScheduleAdapter adapter, ScheduleAdapter.OnItemClickListener listener) {
        super(itemView);
        delete_button = itemView.findViewById(R.id.delete_button);
        date = itemView.findViewById(R.id.date);
        timestart = itemView.findViewById(R.id.timeStart);
        timeend = itemView.findViewById(R.id.timeEnd);
        this.scheduleAdapter = adapter;
        this.listener = listener;

        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(getAdapterPosition());
            }
        });
    }
}
