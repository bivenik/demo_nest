package com.ibohdan.nest.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ibohdan.nest.entity.Token;

public class Preferences {

    private static final String TOKEN_KEY = "TOKEN_KEY";

    private final Context context;

    private SharedPreferences prefs;

    public Preferences(Context context) {
        this.context = context;
    }

    public Token getToken() {
        final String token = getPrefs().getString(TOKEN_KEY, null);

        if (token == null) {
            return null;
        }

        return new Token(token);
    }

    public void setToken(Token token) {
        if (token == null) {
            getPrefs().edit()
                    .remove(TOKEN_KEY)
                    .apply();
        } else {
            getPrefs().edit()
                    .putString(TOKEN_KEY, token.token)
                    .apply();
        }
    }

    private SharedPreferences getPrefs() {
        if (prefs == null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return prefs;
    }
}
