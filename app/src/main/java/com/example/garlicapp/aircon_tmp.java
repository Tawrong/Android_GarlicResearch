package com.example.garlicapp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

import org.bson.types.ObjectId;

public class aircon_tmp extends RealmObject {
    @PrimaryKey
    @Required
    private ObjectId _id;

    private Long c_temp;

    private Boolean status;

    // Standard getters & setters
    public ObjectId getId() { return _id; }
    public void setId(ObjectId _id) { this._id = _id; }

    public Long getCTemp() { return c_temp; }
    public void setCTemp(Long c_temp) { this.c_temp = c_temp; }

    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
}

