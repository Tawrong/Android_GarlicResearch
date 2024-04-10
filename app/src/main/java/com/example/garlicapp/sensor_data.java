package com.example.garlicapp;

import org.bson.types.ObjectId;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class sensor_data extends RealmObject {
    @PrimaryKey
    @Required
    private ObjectId _id;

    private String date;

    private Double humidity;

    private Double lumens1;

    private Double lumens2;

    private Double lumens3;

    private Double lumens4;

    private Double temperature;

    private String time;

    // Standard getters & setters
    public ObjectId getId() { return _id; }
    public void setId(ObjectId _id) { this._id = _id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }

    public Double getLumens1() { return lumens1; }
    public void setLumens1(Double lumens1) { this.lumens1 = lumens1; }

    public Double getLumens2() { return lumens2; }
    public void setLumens2(Double lumens2) { this.lumens2 = lumens2; }

    public Double getLumens3() { return lumens3; }
    public void setLumens3(Double lumens3) { this.lumens3 = lumens3; }

    public Double getLumens4() { return lumens4; }
    public void setLumens4(Double lumens4) { this.lumens4 = lumens4; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
