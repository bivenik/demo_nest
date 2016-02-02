package com.ibohdan.nest.service;

import com.bluelinelabs.logansquare.LoganSquare;
import com.firebase.client.Config;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.Logger;
import com.firebase.client.ValueEventListener;
import com.ibohdan.nest.App;
import com.ibohdan.nest.entity.Device;
import com.ibohdan.nest.entity.Structure;
import com.ibohdan.nest.entity.Thermostat;
import com.ibohdan.nest.network.Api;
import com.ibohdan.nest.util.Preferences;

import org.json.JSONObject;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

public class FirebaseService implements Firebase.AuthListener, ValueEventListener, Firebase.CompletionListener {

    @Inject
    Api api;

    @Inject
    Provider<Preferences> preferences;

    private BehaviorSubject<Structure> structureSubject = BehaviorSubject.create();
    private BehaviorSubject<Thermostat> thermostatSubject = BehaviorSubject.create();

    private volatile Firebase firebase;

    private ThermostatListener listener;

    private boolean auth = false;

    public FirebaseService() {
        App.getGraph().inject(this);
    }

    public boolean isAuth() {
        return auth;
    }

    public Observable<Thermostat> getThermostatObservable() {
        return thermostatSubject;
    }

    public Observable<Structure> getStructureObservable() {
        return structureSubject;
    }

    public void beginListenFirebase(ThermostatListener listener) {
        this.listener = listener;
        addEventListener();
    }

    public void endListenFirebase() {
        removeEventListener();
    }

    public void auth() {
        Timber.v("Authenticating...");
        getFirebase().auth(preferences.get().getToken().token, this);
    }

    public void logout() {
        removeEventListener();
        if (auth) {
            getFirebase().unauth((error, firebase) -> {
                if (error == null) {
                    updateAuthStatus(false);
                    preferences.get().setToken(null);
                    if (listener != null) {
                        listener.onLoggedOut();
                    }
                }
            });
        }
    }

    private Firebase getFirebase() {
        if (firebase == null) {
            synchronized (this) {
                if (firebase == null) {
                    Firebase.goOffline();
                    Firebase.goOnline();
                    Config defaultConfig = Firebase.getDefaultConfig();
                    defaultConfig.setLogLevel(Logger.Level.DEBUG);
                    firebase = new Firebase(Api.FIREBASE_NEST_URL);
                }
            }
        }
        return firebase;
    }

    @Override
    public void onAuthError(FirebaseError firebaseError) {
        Timber.v("onAuthError: " + firebaseError.toString());
        updateAuthStatus(false);
    }

    @Override
    public void onAuthSuccess(Object o) {
        Timber.v("onAuthSuccess");
        updateAuthStatus(true);
        addEventListener();
    }

    @Override
    public void onAuthRevoked(FirebaseError firebaseError) {
        Timber.v("onAuthRevoked: " + firebaseError.toString());
        updateAuthStatus(false);
        auth();
    }

    private void updateAuthStatus(boolean value) {
        if (auth != value) {
            auth = value;
            if (listener != null) {
                listener.onAuthStatusChanged(value);
            }
        }
    }

    private void addEventListener() {
        Timber.v("addEventListener");
        getFirebase().addValueEventListener(this);
    }

    private void removeEventListener() {
        Timber.v("removeEventListener");
        getFirebase().removeEventListener(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Observable.create(subscriber -> {
                    final Map<String, Object> values = dataSnapshot.getValue(StringObjectMapIndicator.INSTANCE);
                    for (Map.Entry<String, Object> entry : values.entrySet()) {
                        notifyListeners(entry);
                    }
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                })
                .subscribeOn(Schedulers.computation())
                .subscribe(value -> {
                }, error -> Timber.e(error, "Failed to parse"));
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        Timber.e("onCancelled: %s", firebaseError);
    }

    private static <T> T parseMap(Map<String, Object> map, Class<T> clazz) {
        try {
            return LoganSquare.parse(new JSONObject(map).toString(), clazz);
        } catch (Exception ex) {
            Timber.e(ex, "Failed to parse object");
        }
        return null;
    }

    private void notifyListeners(Map.Entry<String, Object> entry) {
        final Map<String, Object> value = (Map<String, Object>) entry.getValue();
        switch (entry.getKey()) {
            case Device.DEVICES:
                notifyDevices(value);
                break;
            case Structure.STRUCTURES:
                notifyStructure(value);
                break;
        }
    }

    private void notifyDevices(Map<String, Object> devices) {
        for (Map.Entry<String, Object> entry : devices.entrySet()) {
            final Map<String, Object> map = (Map<String, Object>) entry.getValue();
            switch (entry.getKey()) {
                case Thermostat.THERMOSTATS:
                    notifyThermostat(map);
                    break;
            }
        }
    }

    private void notifyThermostat(Map<String, Object> thermostatsMap) {
        for (Map.Entry<String, Object> entry : thermostatsMap.entrySet()) {
            final Map<String, Object> map = (Map<String, Object>) entry.getValue();
            final Thermostat thermostat = parseMap(map, Thermostat.class);
            if (thermostat != null) {
                thermostatSubject.onNext(thermostat);
            }
        }
    }

    private void notifyStructure(Map<String, Object> structures) {
        for (Map.Entry<String, Object> entry : structures.entrySet()) {
            final Map<String, Object> map = (Map<String, Object>) entry.getValue();
            final Structure structure = parseMap(map, Structure.class);
            if (structure != null) {
                structureSubject.onNext(structure);
            }
        }
    }

    public void sendRequest(String path, Object value) {
        Observable
                .create(subscriber -> {
                    getFirebase().child(path).setValue(value, this);
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                })
                .subscribeOn(Schedulers.io())
                .subscribe(o -> {
                }, throwable -> {
                });
    }

    @Override
    public void onComplete(FirebaseError firebaseError, Firebase firebase) {

    }


    private static class StringObjectMapIndicator extends GenericTypeIndicator<Map<String, Object>> {

        static final StringObjectMapIndicator INSTANCE = new StringObjectMapIndicator();
    }
}
