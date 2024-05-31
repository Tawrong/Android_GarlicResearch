package com.example.garlicapp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

import org.bson.types.ObjectId;

public class schedule extends RealmObject {
    @PrimaryKey
    @Required
    private ObjectId _id;

    private String end_date;

    private String password;

    private String start_date;

    private String temperature_off;

    private String temperature_on;

    private String time_off;

    private String time_off_end;

    private String time_off_start;

    private String time_on;

    private String time_on_end;

    private String time_on_start;

    // Standard getters & setters
    public ObjectId getId() { return _id; }
    public void setId(ObjectId _id) { this._id = _id; }

    public String getEndDate() { return end_date; }
    public void setEndDate(String end_date) { this.end_date = end_date; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getStartDate() { return start_date; }
    public void setStartDate(String start_date) { this.start_date = start_date; }

    public String getTemperatureOff() { return temperature_off; }
    public void setTemperatureOff(String temperature_off) { this.temperature_off = temperature_off; }

    public String getTemperatureOn() { return temperature_on; }
    public void setTemperatureOn(String temperature_on) { this.temperature_on = temperature_on; }

    public String getTimeOff() { return time_off; }
    public void setTimeOff(String time_off) { this.time_off = time_off; }

    public String getTimeOffEnd() { return time_off_end; }
    public void setTimeOffEnd(String time_off_end) { this.time_off_end = time_off_end; }

    public String getTimeOffStart() { return time_off_start; }
    public void setTimeOffStart(String time_off_start) { this.time_off_start = time_off_start; }

    public String getTimeOn() { return time_on; }
    public void setTimeOn(String time_on) { this.time_on = time_on; }

    public String getTimeOnEnd() { return time_on_end; }
    public void setTimeOnEnd(String time_on_end) { this.time_on_end = time_on_end; }

    public String getTimeOnStart() { return time_on_start; }
    public void setTimeOnStart(String time_on_start) { this.time_on_start = time_on_start; }
}
