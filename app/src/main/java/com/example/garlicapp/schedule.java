package com.example.garlicapp;

import org.bson.types.ObjectId;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class schedule extends RealmObject {
    @PrimaryKey
    @Required
    private ObjectId _id;

    private Integer day;

    private String grow_light_status;

    private Integer month;

    private String monthYearSelected;

    private String rack;

    private Integer temperature;

    private String time_end;

    private String time_start;

    private Integer year;

    // Standard getters & setters
    public ObjectId getId() { return _id; }
    public void setId(ObjectId _id) { this._id = _id; }

    public Integer getDay() { return day; }
    public void setDay(Integer day) { this.day = day; }

    public String getGrowLightStatus() { return grow_light_status; }
    public void setGrowLightStatus(String grow_light_status) { this.grow_light_status = grow_light_status; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public String getMonthYearSelected() { return monthYearSelected; }
    public void setMonthYearSelected(String monthYearSelected) { this.monthYearSelected = monthYearSelected; }

    public String getRack() { return rack; }
    public void setRack(String rack) { this.rack = rack; }

    public Integer getTemperature() { return temperature; }
    public void setTemperature(Integer temperature) { this.temperature = temperature; }

    public String getTimeEnd() { return time_end; }
    public void setTimeEnd(String time_end) { this.time_end = time_end; }

    public String getTimeStart() { return time_start; }
    public void setTimeStart(String time_start) { this.time_start = time_start; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
}
