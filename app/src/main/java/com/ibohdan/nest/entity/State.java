package com.ibohdan.nest.entity;

public enum State {

    AWAY("away"),
    AUTO_AWAY("auto-away"),
    HOME("home");

    public static State getState(String key) {
        final State[] values = values();
        for (int i = 0; i < values.length; i++) {
            if (values[i].value.equalsIgnoreCase(key)) {
                return values[i];
            }
        }
        throw new IllegalArgumentException("Can't find state for key: " + key);
    }

    private final String value;

    State(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
