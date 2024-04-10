package com.example.garlicapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private static List<ScheduleItem> scheduleItemList;

    public ScheduleAdapter(List<ScheduleItem> scheduleItemList) {
        ScheduleAdapter.scheduleItemList = scheduleItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleItem scheduleItem = scheduleItemList.get(position);
        holder.racknum.setText(scheduleItem.getRacknum());
        holder.expireDateTextView.setText(scheduleItem.getExpireDate());
        holder.timeStartTextView.setText(scheduleItem.getTimeStart());
        holder.timeEndTextView.setText(scheduleItem.getTimeEnd());
        holder.temperatureTextView.setText(scheduleItem.getTemperature());
        holder.glsTextView.setText(scheduleItem.getGrowlights());
    }

    @Override
    public int getItemCount() {
        return scheduleItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView expireDateTextView;
        public TextView timeStartTextView;
        public TextView timeEndTextView, racknum, glsTextView, temperatureTextView;
        public ImageView deleterowButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deleterowButton = itemView.findViewById(R.id.DeleteRowButton);
            expireDateTextView = itemView.findViewById(R.id.expireDateTextView);
            timeStartTextView = itemView.findViewById(R.id.timeStartTextView);
            timeEndTextView = itemView.findViewById(R.id.timeEndTextView);
            racknum = itemView.findViewById(R.id.RackNum);
            glsTextView = itemView.findViewById(R.id.glsTextView);
            temperatureTextView = itemView.findViewById(R.id.temperatureTextView);


            deleterowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        ObjectId clickedObjectId = scheduleItemList.get(position).getObjectId();
                        scheduleItemList.remove(position);
                        notifyItemRemoved(position);

                        deleteDocumentFromDatabase(clickedObjectId);
                    }
                } // Missing closing brace for onClick method
            }); // Closing brace for setOnClickListener
        }
    }

    private void deleteDocumentFromDatabase(ObjectId objectId) {
        AppConfiguration appConfiguration = new AppConfiguration.Builder("devicesync-ehvrh").build();
        App app = new App(appConfiguration);
        app.loginAsync(Credentials.anonymous(), it -> {
            if (it.isSuccess()) {
                User user = app.currentUser();
                MongoCollection<Document> collection = user.getMongoClient("garlicgreenhouse")
                        .getDatabase("GarlicGreenhouse")
                        .getCollection("schedule");
                Document filter = new Document("_id", objectId);
                collection.deleteOne(filter).getAsync(result -> {
                    if (result.isSuccess()) {
                        Log.d("Delete Sched", "Deleted Successfully");
                    } else {
                        Log.d("Delete Sched", "Deletion Failed");
                    }
                });
            }
        });
    }

}
