package com.ibohdan.nest.entity;

import android.support.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.ibohdan.nest.network.logansquare.StateConverter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@JsonObject
public class Structure {

    public static final String STRUCTURES = "structures";

    public static final String FIELD_STRUCTURE_ID = "structure_id";
    public static final String FIELD_AWAY = "away";

    @JsonField(name = FIELD_STRUCTURE_ID)
    public String structureID;

    @JsonField(name = FIELD_AWAY, typeConverter = StateConverter.class)
    public State state;

    public Structure() {
    }

    @StringDef({FIELD_AWAY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FieldName {
    }

    public String getPath(@FieldName String field) {
        return "/" + STRUCTURES + "/" + structureID + "/" + field;
    }
}
