package com.example.garlicapp;

import org.bson.types.ObjectId;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class light_state extends RealmObject {
    @PrimaryKey
    @Required
    private ObjectId _id;

    private Integer relay_num;

    private Boolean state;

    // Standard getters & setters
    public ObjectId getId() { return _id; }
    public void setId(ObjectId _id) { this._id = _id; }

    public Integer getRelayNum() { return relay_num; }
    public void setRelayNum(Integer relay_num) { this.relay_num = relay_num; }

    public Boolean getState() { return state; }
    public void setState(Boolean state) { this.state = state; }
}
