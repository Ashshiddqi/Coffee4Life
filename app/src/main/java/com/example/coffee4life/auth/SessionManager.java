package com.example.coffee4life.auth;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.coffee4life.Constants;

public class SessionManager {
    private final SharedPreferences prefs;

    public SessionManager(Context ctx) {
        this.prefs = ctx.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String accessToken, String refreshToken, String email) {
        prefs.edit()
                .putString(Constants.PREF_ACCESS_TOKEN, accessToken)
                .putString(Constants.PREF_REFRESH_TOKEN, refreshToken)
                .putString(Constants.PREF_USER_EMAIL, email)
                .apply();
    }

    public String getAccessToken() {
        return prefs.getString(Constants.PREF_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(Constants.PREF_REFRESH_TOKEN, null);
    }

    public String getEmail() {
        return prefs.getString(Constants.PREF_USER_EMAIL, null);
    }

    public void setDisplayName(String name) {
        prefs.edit().putString(Constants.PREF_USER_NAME, name).apply();
    }

    public String getDisplayName() {
        return prefs.getString(Constants.PREF_USER_NAME, null);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
