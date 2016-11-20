package com.buahbatu.jantung;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by maakbar on 11/8/16.
 */

public class AppSetting {
    private static String PREFERENCE_NAME = "JANTUNG PREF";

    public static boolean LOGGED_IN = true;
    public static boolean LOGGED_OUT = false;

    public static boolean isLoggedIn(Context context){
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).getBoolean("isLoggedIn", false);
    }
    public static void setLogin(Context context, boolean isLoggedIn){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.apply();
    }
    public static void saveAccount(Context context, String username, String password){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();
    }

    public static AccountInfo getSavedAccount(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return new AccountInfo(pref.getString("username", ""), pref.getString("password", ""));
    }

    static class AccountInfo{
        String username;
        String password;
        public AccountInfo(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
