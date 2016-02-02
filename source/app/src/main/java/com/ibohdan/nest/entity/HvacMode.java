package com.ibohdan.nest.entity;

public enum HvacMode {

    HEAT("heat"),
    COOL("cool"),
    HEAT_AND_COOL("heat-cool"),
    OFF("off");

    public static HvacMode getMode(String key) {
        final HvacMode[] values = values();
        for (int i = 0; i < values.length; i++) {
            if (values[i].value.equalsIgnoreCase(key)) {
                return values[i];
            }
        }
        throw new IllegalArgumentException("Can't find mode for key: " + key);
    }

    private final String value;

    HvacMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
