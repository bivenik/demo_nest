package com.ibohdan.nest.network.logansquare;

import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;
import com.ibohdan.nest.entity.HvacMode;

public class HvacModeConverter extends StringBasedTypeConverter<HvacMode> {

    @Override
    public HvacMode getFromString(String string) {
        return HvacMode.getMode(string);
    }

    @Override
    public String convertToString(HvacMode mode) {
        return mode.getValue();
    }
}
