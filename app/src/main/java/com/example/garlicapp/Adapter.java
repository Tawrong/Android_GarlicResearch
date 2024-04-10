package com.example.garlicapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<viewholder> {
    private Context context;
    private List<RecycleView_Item> list;

    public Adapter(Context context, List<RecycleView_Item> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new viewholder(LayoutInflater.from(context).inflate(R.layout.recycler_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        holder.fieldname.setText(list.get(position).getFieldName());
        holder.datavalues.setText(list.get(position).getValues());
        holder.units.setText(list.get(position).getUnits());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // Method to update the dataset
    public void updateData(List<RecycleView_Item> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }
}
