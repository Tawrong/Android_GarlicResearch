package com.example.garlicapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<Scheduler_viewHolder> {
    private Context context;
    private List<Scheduler_items> list;
    private OnItemClickListener listener;
    public ScheduleAdapter(Context context, List<Scheduler_items> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }
    @NonNull
    @Override

    public Scheduler_viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scheduler_recyclerview_layout, parent, false);
        return new Scheduler_viewHolder(view, this, listener); // Pass the adapter instance here
    }


    @Override
    public void onBindViewHolder(@NonNull Scheduler_viewHolder holder, int position) {
        holder.date.setText(list.get(position).getDate());
        holder.timestart.setText(list.get(position).getTimeStart());
        holder.timeend.setText(list.get(position).getTimeEnd());

    }
    @Override
    public int getItemCount() {
        return list.size();
    }
    public void updateData(List<Scheduler_items> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }
    public interface OnItemClickListener {
        void onItemClick(int position);
    }



}
