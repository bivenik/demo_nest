package com.ibohdan.nest.network.logansquare;

import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;
import com.ibohdan.nest.entity.State;

public class StateConverter extends StringBasedTypeConverter<State> {

    @Override
    public State getFromString(String string) {
        return State.getState(string);
    }

    @Override
    public String convertToString(State state) {
        return state.getValue();
    }
}
