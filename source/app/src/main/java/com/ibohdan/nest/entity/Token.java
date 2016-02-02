package com.ibohdan.nest.entity;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.io.Serializable;

@JsonObject
public class Token implements Serializable{

    @JsonField(name = "access_token")
    public String token;

    public Token() {
    }

    public Token(String token) {
        this.token = token;
    }
}
