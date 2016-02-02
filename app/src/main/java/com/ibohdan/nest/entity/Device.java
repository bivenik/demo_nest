package com.ibohdan.nest.entity;

import android.support.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@JsonObject
public class Device {

    public static final String DEVICES = "devices";

    public static final String FIELD_DEVICE_ID = "device_id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_IS_ONLINE = "is_online";

    @JsonField(name = FIELD_DEVICE_ID)
    public String deviceID;

    @JsonField(name = FIELD_NAME)
    public String name;

    @JsonField(name = FIELD_IS_ONLINE)
    public boolean isOnline;

    public Device() {
    }

    @StringDef({})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DeviceFieldName {
    }

    public String getPath(@DeviceFieldName String field) {
        return "/" + DEVICES + "/" + getDeviceName() + "/" + deviceID + "/" + field;
    }

    protected String getDeviceName() {
        return null;
    }
}
