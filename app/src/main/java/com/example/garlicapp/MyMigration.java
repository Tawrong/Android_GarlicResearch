package com.example.garlicapp;

import org.bson.types.ObjectId;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;

public class MyMigration implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        // DynamicRealm exposes a mutable schema
        RealmObjectSchema sensorDataSchema = realm.getSchema().get("sensor_data");

        // Check if the class "sensor_data" exists in the schema
        if (sensorDataSchema == null) {
            // If not, create it
            sensorDataSchema = realm.getSchema().create("sensor_data")
                    .addField("_id", ObjectId.class, FieldAttribute.PRIMARY_KEY, FieldAttribute.REQUIRED)
                    .addField("humidity", Double.class)
                    .addField("lumens1", Double.class)
                    .addField("lumens2", Double.class)
                    .addField("lumens3", Double.class)
                    .addField("lumens4", Double.class)
                    .addField("temperature", Double.class)
                    .addField("date", String.class)
                    .addField("time", String.class);

            // Perform migration operations based on oldVersion and newVersion
            // ...

            // If there are more changes, you can add more conditions and perform corresponding migrations
        }
    }
}



