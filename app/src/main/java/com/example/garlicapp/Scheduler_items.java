package com.example.garlicapp;

import org.bson.types.ObjectId;

public class Scheduler_items {
    String date;
    String timeStart;
    ObjectId objectId;
    String password;

    public void setDate(String date) {
        this.date = date;
    }

    public void setTimeStart(String timeStart) {
        this.timeStart = timeStart;
    }

    public void setTimeEnd(String timeEnd) {
        this.timeEnd = timeEnd;
    }

    public String getDate() {
        return date;
    }

    public String getTimeStart() {
        return timeStart;
    }

    public String getTimeEnd() {
        return timeEnd;
    }

    String timeEnd;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public Scheduler_items(ObjectId objectId, String date, String timeStart, String timeEnd, String password){
        this.objectId = objectId;
        this.date = date;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.password = password;
    }

}
