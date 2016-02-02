package com.ibohdan.nest.service;

public interface ThermostatListener {

    void onAuthStatusChanged(boolean isAuth);

    void onLoggedOut();
}
