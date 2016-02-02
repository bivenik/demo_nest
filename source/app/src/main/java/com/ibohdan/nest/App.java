package com.ibohdan.nest;

import android.app.Application;

import com.ibohdan.nest.util.dagger.DaggerGraph;

import timber.log.Timber;

public class App extends Application {

    private static DaggerGraph graph;

    public static DaggerGraph getGraph() {
        return graph;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        graph = DaggerGraph.Initializer.init(this);
        graph.inject(this);

        Timber.plant(new Timber.DebugTree());
    }
}
