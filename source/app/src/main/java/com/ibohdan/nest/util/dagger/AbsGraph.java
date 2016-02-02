package com.ibohdan.nest.util.dagger;

import com.ibohdan.nest.App;
import com.ibohdan.nest.activity.HomeActivity;
import com.ibohdan.nest.activity.LoginActivity;
import com.ibohdan.nest.service.FirebaseService;
import com.ibohdan.nest.ui.ThermostatViewHolder;

public interface AbsGraph {

    // Application
    void inject(App app);

    void inject(LoginActivity loginActivity);

    void inject(HomeActivity homeActivity);

    void inject(FirebaseService firebaseService);

    void inject(ThermostatViewHolder thermostatViewHolder);

    final class Initializer {
        private Initializer() {
        } // No instances.

        public static DaggerGraph init(App app) {
            return DaggerDaggerGraph.builder()
                    .defaultModule(new DefaultModule(app))
                    .build();
        }
    }
}
