package com.example.garlicapp;

import org.bson.types.ObjectId;

public class ScheduleItem {
    private final ObjectId objectId;
    private final String expireDate;
    private final String timeStart;
    private final String timeEnd;
    private final String Racknum;
    private final String temperature;
    private final String growlights;

    // Constructor
    public ScheduleItem(ObjectId objectId, String expireDate, String timeStart, String timeEnd, String racknum, String temperature, String growlights) {
        this.objectId = objectId;
        this.expireDate = expireDate;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.Racknum = racknum;
        this.temperature = temperature;
        this.growlights = growlights;

    }

    // Getter methods
    public ObjectId getObjectId() {
        return objectId;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public String getTimeStart() {
        return timeStart;
    }

    public String getTimeEnd() {
        return timeEnd;
    }
    public String getRacknum() {
        return Racknum;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getGrowlights() {
        return growlights;
    }
}

