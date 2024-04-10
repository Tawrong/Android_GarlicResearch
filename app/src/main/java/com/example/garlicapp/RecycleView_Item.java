package com.example.garlicapp;

public class RecycleView_Item {
    String fieldName;
    String values;
    String units;

    public RecycleView_Item(String fieldName, String values, String units) {
        this.fieldName = fieldName;
        this.values = values;
        this.units = units;
    }
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }
}
