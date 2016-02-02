package com.ibohdan.nest.util.dagger;

import com.ibohdan.nest.App;
import com.ibohdan.nest.network.Api;
import com.ibohdan.nest.network.ApiAdapter;
import com.ibohdan.nest.service.FirebaseService;
import com.ibohdan.nest.util.Preferences;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DefaultModule {

    private final App app;

    public DefaultModule(App application) {
        app = application;
    }

    @Provides
    App provideApp() {
        return app;
    }

    @Provides
    OkHttpClient provideHttpClient() {
        return new OkHttpClient();
    }

    @Provides
    @Singleton
    Preferences providePreferences() {
        return new Preferences(app);
    }

    @Provides
    Api provideApi() {
        return new ApiAdapter();
    }

    @Provides
    @Singleton
    FirebaseService provideConnectionService() {
        return new FirebaseService();
    }
}
