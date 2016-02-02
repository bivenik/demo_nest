package com.ibohdan.nest.ui;

import android.content.res.Resources;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ibohdan.nest.App;
import com.ibohdan.nest.R;
import com.ibohdan.nest.entity.HvacMode;
import com.ibohdan.nest.entity.State;
import com.ibohdan.nest.entity.Structure;
import com.ibohdan.nest.entity.Thermostat;
import com.ibohdan.nest.service.FirebaseService;

import java.util.ArrayList;

import javax.inject.Inject;

public class ThermostatViewHolder implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    enum SelectedTemperature {
        NONE, HIGH, LOW
    }

    @Inject
    FirebaseService firebaseService;

    private final Resources resources;

    private final View rootView;

    private final ThermostatView thermostatView;

    private final View fanView;
    private final View leafView;

    private final TextView currentTempTextView;
    private final TextView targetTempTextView;
    private final TextView humidityTextView;
    private final TextView temperatureLowTextView;
    private final TextView temperatureHighTextView;

    private final AppCompatButton statusButton;
    private final AppCompatSpinner hvacModeSpinner;

    private ArrayList<HvacMode> thermostatModes;
    private Thermostat thermostat;
    private Structure structure;
    private State currentState;
    private SelectedTemperature selectedTemperature = SelectedTemperature.NONE;

    public ThermostatViewHolder(View view) {
        App.getGraph().inject(this);

        rootView = view;
        resources = view.getResources();

        thermostatView = (ThermostatView) view.findViewById(R.id.thermostat);
        targetTempTextView = (TextView) view.findViewById(R.id.target_temperature);
        humidityTextView = (TextView) view.findViewById(R.id.humidity);

        currentTempTextView = (TextView) view.findViewById(R.id.current_temperature);

        temperatureLowTextView = (TextView) view.findViewById(R.id.temperature_low);
        temperatureHighTextView = (TextView) view.findViewById(R.id.temperature_high);

        hvacModeSpinner = (AppCompatSpinner) view.findViewById(R.id.hvac_mode_spinner);

        statusButton = (AppCompatButton) view.findViewById(R.id.status_button);
        statusButton.setOnClickListener(this);

        fanView = view.findViewById(R.id.fan);
        leafView = view.findViewById(R.id.leaf);

        View tempUp = view.findViewById(R.id.increase_temp);
        tempUp.setOnClickListener(this);
        View tempDown = view.findViewById(R.id.decrease_temp);
        tempDown.setOnClickListener(this);

        temperatureLowTextView.setOnClickListener(this);
        temperatureHighTextView.setOnClickListener(this);

        resetSpinner();
    }

    public void updateThermostat(Thermostat thermostat) {
        this.thermostat = thermostat;

        if (!isOnline() || thermostat.hvacMode == HvacMode.OFF) {
            targetTempTextView.setText(resources.getString(R.string.off));
            setEnabled(false);
        } else {
            switch (thermostat.hvacMode) {
                case HEAT:
                case COOL:
                    selectedTemperature = SelectedTemperature.NONE;
                    targetTempTextView.setText(resources.getString(R.string.temperature, thermostat.targetTemperatureC));
                    setEnabled(true);
                    break;
                case HEAT_AND_COOL:
                    if (selectedTemperature == SelectedTemperature.NONE) {
                        selectedTemperature = SelectedTemperature.HIGH;
                    }
                    temperatureLowTextView.setText(resources.getString(R.string.temperature, thermostat.targetTemperatureLowC));
                    temperatureHighTextView.setText(resources.getString(R.string.temperature, thermostat.targetTemperatureHighC));
                    setEnabled(true);
                    break;
                case OFF:
                    break;
            }
        }
        updateTemperatureViews();

        if (isOnline() && thermostat.humidity > 0) {
            humidityTextView.setText(resources.getString(R.string.humidity, thermostat.humidity));
        } else {
            humidityTextView.setText(null);
        }

        if (!isOnline()) {
            return;
        }

        switch (thermostat.hvacMode) {
            case COOL:
            case HEAT:
            case HEAT_AND_COOL:
                update();
                break;
        }

        updateModes();
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
        if (currentState != structure.state) {
            currentState = structure.state;
            setEnabled(isOnline() && thermostat.hvacMode != HvacMode.OFF);
            if (thermostat != null) {
                updateThermostat(thermostat);
            }
            statusButton.setText(structure.state == State.AWAY || structure.state == State.AUTO_AWAY
                    ? R.string.away
                    : R.string.home);
        }
    }

    public void setAway() {
        structure.state = State.AWAY;
        statusButton.setText(R.string.away);
        firebaseService.sendRequest(structure.getPath(Structure.FIELD_AWAY), State.AWAY.getValue());
    }

    public void setHome() {
        structure.state = State.HOME;
        statusButton.setText(R.string.home);
        firebaseService.sendRequest(structure.getPath(Structure.FIELD_AWAY), State.HOME.getValue());
    }

    public void switchStatus() {
        if (structure != null) {
            switch (structure.state) {
                case AUTO_AWAY:
                case AWAY:
                    setHome();
                    break;
                case HOME:
                default:
                    setAway();
                    break;
            }
        }
    }

    public void setEnabled(boolean enabled) {
        thermostatView.setEnabled(enabled);
    }

    private void updateModes() {
        resetSpinner();

        if (thermostatModes == null) {
            ArrayList<String> modes = new ArrayList<>();
            thermostatModes = new ArrayList<>();
            if (thermostat.canCool) {
                modes.add(resources.getString(R.string.mode_cool));
                thermostatModes.add(HvacMode.COOL);
            }
            if (thermostat.canHeat) {
                modes.add(resources.getString(R.string.mode_heat));
                thermostatModes.add(HvacMode.HEAT);
            }
            if (thermostat.canCool && thermostat.canHeat) {
                modes.add(resources.getString(R.string.mode_heat_cool));
                thermostatModes.add(HvacMode.HEAT_AND_COOL);
            }
            modes.add(resources.getString(R.string.mode_off));
            thermostatModes.add(HvacMode.OFF);
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(rootView.getContext(), R.layout.spinner_item, modes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            hvacModeSpinner.setAdapter(adapter);
        }

        int selectedPos = 0;
        for (int i = 0; i < thermostatModes.size(); i++) {
            if (thermostatModes.get(i) == thermostat.hvacMode) {
                selectedPos = i;
                break;
            }
        }
        hvacModeSpinner.setSelection(selectedPos);
    }

    private void update() {
        if (thermostat.hasFan && thermostat.fanTimerActive) {
            fanView.setVisibility(View.VISIBLE);
        } else {
            fanView.setVisibility(View.GONE);
        }

        if (thermostat.hasLeaf) {
            leafView.setVisibility(View.VISIBLE);
        } else {
            leafView.setVisibility(View.GONE);
        }
        thermostatView.setShowHighLowTemp(thermostat.hvacMode == HvacMode.HEAT_AND_COOL);
        thermostatView.setTargetTemperature((float) thermostat.targetTemperatureC);
        thermostatView.setTargetLowTemperature((float) thermostat.targetTemperatureLowC);
        thermostatView.setTargetHighTemperature((float) thermostat.targetTemperatureHighC);
        if (thermostat.hvacMode == HvacMode.COOL) {
            thermostatView.setColorTarget(resources.getColor(R.color.thermostat_target_cool));
        } else {
            thermostatView.setColorTarget(resources.getColor(R.color.thermostat_target_heat));
        }
        thermostatView.setCurrentTemperature((float) thermostat.ambientTemperatureC);
        currentTempTextView.setText(resources.getString(R.string.temperature, thermostat.ambientTemperatureC));
        thermostatView.setColorCurrent(resources.getColor(R.color.thermostat_current));
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.increase_temp:
                tempUp();
                break;
            case R.id.decrease_temp:
                tempDown();
                break;
            case R.id.temperature_low:
                selectedTemperature = SelectedTemperature.LOW;
                updateTemperatureViews();
                break;
            case R.id.temperature_high:
                selectedTemperature = SelectedTemperature.HIGH;
                updateTemperatureViews();
                break;
            case R.id.status_button:
                switchStatus();
                break;
        }
    }

    private void updateTemperatureViews() {
        int highColor = resources.getColor(R.color.temperature_color);
        int lowColor = highColor;
        if (!isOnline()) {
            selectedTemperature = SelectedTemperature.NONE;
        }
        switch (selectedTemperature) {
            case NONE:
                targetTempTextView.setVisibility(View.VISIBLE);
                temperatureLowTextView.setVisibility(View.GONE);
                temperatureHighTextView.setVisibility(View.GONE);
                break;
            case HIGH:
                targetTempTextView.setVisibility(View.GONE);
                temperatureLowTextView.setVisibility(View.VISIBLE);
                temperatureHighTextView.setVisibility(View.VISIBLE);
                highColor = resources.getColor(R.color.temperature_color_selected);
                break;
            case LOW:
                targetTempTextView.setVisibility(View.GONE);
                temperatureLowTextView.setVisibility(View.VISIBLE);
                temperatureHighTextView.setVisibility(View.VISIBLE);
                lowColor = resources.getColor(R.color.temperature_color_selected);
                break;
        }
        temperatureLowTextView.setTextColor(lowColor);
        temperatureHighTextView.setTextColor(highColor);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.hvac_mode_spinner: {
                if (isOnline()) {
                    firebaseService.sendRequest(thermostat.getPath(Thermostat.FIELD_HVAC_MODE), thermostatModes.get(position).getValue());
                }
            }
            break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void resetSpinner() {
        hvacModeSpinner.setOnItemSelectedListener(null);
        hvacModeSpinner.post(() -> hvacModeSpinner.setOnItemSelectedListener(ThermostatViewHolder.this));
    }

    public void tempUp() {
        setTempDelta(.5F);
    }

    public void tempDown() {
        setTempDelta(-.5F);
    }

    private void setTempDelta(float delta) {
        if (isOnline()) {
            String field = Thermostat.FIELD_TARGET_TEMP_C;
            double temp = thermostat.targetTemperatureC;
            switch (selectedTemperature) {
                case NONE:
                    temp = validateTemp(thermostat.targetTemperatureC + delta);
                    thermostat.targetTemperatureC = temp;
                    thermostatView.setTargetTemperature((float) temp);
                    break;
                case HIGH:
                    temp = validateTemp(thermostat.targetTemperatureHighC + delta);
                    thermostat.targetTemperatureHighC = temp;
                    field = Thermostat.FIELD_TARGET_TEMP_HIGH_C;
                    thermostatView.setTargetHighTemperature((float) temp);
                    break;
                case LOW:
                    temp = validateTemp(thermostat.targetTemperatureLowC + delta);
                    thermostat.targetTemperatureLowC = temp;
                    field = Thermostat.FIELD_TARGET_TEMP_LOW_C;
                    thermostatView.setTargetLowTemperature((float) temp);
                    break;
            }
            firebaseService.sendRequest(thermostat.getPath(field), temp);
        }
    }

    private double validateTemp(double value) {
        double temp = Math.min(32, value);
        return Math.max(9, temp);
    }

    private boolean isOnline() {
        return thermostat != null && thermostat.isOnline && currentState == State.HOME;
    }
}
