package com.ibohdan.nest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ibohdan.nest.App;
import com.ibohdan.nest.R;
import com.ibohdan.nest.service.FirebaseService;
import com.ibohdan.nest.service.ThermostatListener;
import com.ibohdan.nest.ui.ThermostatViewHolder;
import com.ibohdan.nest.util.Preferences;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class HomeActivity extends AppCompatActivity implements ThermostatListener {

    @Inject
    Preferences preferences;

    @Inject
    FirebaseService firebaseService;

    private CompositeSubscription allSubs;

    private ThermostatViewHolder thermostatViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.getGraph().inject(this);
        setContentView(R.layout.home_activity);

        final View content = findViewById(R.id.content);

        thermostatViewHolder = new ThermostatViewHolder(content);
        thermostatViewHolder.setEnabled(firebaseService.isAuth());
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseService.beginListenFirebase(this);
        firebaseService.auth();

        final Subscription structureSubscription = firebaseService.getStructureObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(thermostatViewHolder::setStructure, throwable -> {
                });
        addSubscription(structureSubscription);

        final Subscription thermostatSubscription = firebaseService.getThermostatObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(thermostat -> {
                    setTitle(thermostat.name);
                    thermostatViewHolder.updateThermostat(thermostat);
                }, throwable -> {
                });
        addSubscription(thermostatSubscription);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (allSubs != null) {
            allSubs.clear();
            allSubs = null;
        }
        firebaseService.endListenFirebase();
    }

    @Override
    public void onAuthStatusChanged(boolean isAuth) {
        thermostatViewHolder.setEnabled(isAuth);
    }

    @Override
    public void onLoggedOut() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    protected void addSubscription(Subscription subscription) {
        if (allSubs == null) {
            allSubs = new CompositeSubscription();
        }
        allSubs.add(subscription);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                firebaseService.logout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
