package com.example.admin.smartchatalphav1;

import android.content.Context;
import android.preference.PreferenceManager;

public class QueryPreferences {
    private static final String PREF_PHONE_QUERY = "com.example.admin.smartchatalphav1.phone";
    private static final String PREF_PASSWORD_QUERY = "com.example.admin.smartchatalphav1.password";
    private static final String PREF_TOKEN_QUERY = "com.example.admin.smartchatalphav1.token";
    private static final String PREF_SUCCESS_QUERY = "com.example.admin.smartchatalphav1.success";
    private static final String PREF_ID_QUERY = "com.example.admin.smartchatalphav1.id";

    public static void setPrefIdQuery(Context context, String id) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_ID_QUERY, id)
                .apply();
    }

    public static String getPrefIdQuery() {
        return PREF_ID_QUERY;
    }

    public static void resettingAuthorisedData(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PREF_SUCCESS_QUERY, false).apply();
    }

    public static String getStoredPhone(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_PHONE_QUERY, null);
    }

    public static String getStoredPassword(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_PASSWORD_QUERY, null);
    }

    public static void setStoredPhone(Context context, String phone) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_PHONE_QUERY, phone)
                .apply();
    }

    public static void setStoredPassword(Context context, String password) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_PASSWORD_QUERY, password)
                .apply();
    }

    public static void setStoredToken(Context context, String token) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_TOKEN_QUERY, token)
                .apply();
    }

    public static String getStoredToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_TOKEN_QUERY, null);
    }

    public static void setStoredSuccess(Context context, boolean success) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PREF_SUCCESS_QUERY, success)
                .apply();
    }

    public static Boolean getStoredSuccess(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_SUCCESS_QUERY, false);
    }
}
