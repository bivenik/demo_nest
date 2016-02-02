package com.ibohdan.nest.entity;

import android.support.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.ibohdan.nest.network.logansquare.HvacModeConverter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@JsonObject
public class Thermostat extends Device {

    public static final String THERMOSTATS = "thermostats";

    public static final String FIELD_CAN_COOL = "can_cool";
    public static final String FIELD_CAN_HEAT = "can_heat";
    public static final String FIELD_HAS_FAN = "has_fan";
    public static final String FIELD_HUMIDITY = "humidity";
    public static final String FIELD_HAS_LEAF = "has_leaf";
    public static final String FIELD_AMBIENT_TEMP_C = "ambient_temperature_c";
    public static final String FIELD_FAN_TIMER_ACTIVE = "fan_timer_active";
    public static final String FIELD_TARGET_TEMP_C = "target_temperature_c";
    public static final String FIELD_TARGET_TEMP_HIGH_C = "target_temperature_high_c";
    public static final String FIELD_TARGET_TEMP_LOW_C = "target_temperature_low_c";
    public static final String FIELD_HVAC_MODE = "hvac_mode";

    @JsonField(name = FIELD_CAN_COOL)
    public boolean canCool;

    @JsonField(name = FIELD_CAN_HEAT)
    public boolean canHeat;

    @JsonField(name = FIELD_HAS_FAN)
    public boolean hasFan;

    @JsonField(name = FIELD_HUMIDITY)
    public int humidity;

    @JsonField(name = FIELD_HAS_LEAF)
    public boolean hasLeaf;

    @JsonField(name = FIELD_AMBIENT_TEMP_C)
    public double ambientTemperatureC;

    @JsonField(name = FIELD_FAN_TIMER_ACTIVE)
    public boolean fanTimerActive;

    @JsonField(name = FIELD_TARGET_TEMP_C)
    public double targetTemperatureC;

    @JsonField(name = FIELD_TARGET_TEMP_HIGH_C)
    public double targetTemperatureHighC;

    @JsonField(name = FIELD_TARGET_TEMP_LOW_C)
    public double targetTemperatureLowC;

    @JsonField(name = FIELD_HVAC_MODE, typeConverter = HvacModeConverter.class)
    public HvacMode hvacMode;

    public Thermostat() {
    }

    @StringDef({FIELD_FAN_TIMER_ACTIVE, FIELD_TARGET_TEMP_C, FIELD_TARGET_TEMP_HIGH_C, FIELD_TARGET_TEMP_LOW_C, FIELD_HVAC_MODE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FieldName {
    }

    @Override
    public String getPath(@FieldName String field) {
        return super.getPath(field);
    }

    @Override
    protected String getDeviceName() {
        return THERMOSTATS;
    }
}
