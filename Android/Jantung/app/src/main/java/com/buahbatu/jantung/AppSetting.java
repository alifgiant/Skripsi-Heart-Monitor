package com.buahbatu.jantung;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by maakbar on 11/8/16.
 */

class AppSetting {
    private static String PREFERENCE_NAME = "JANTUNG PREF";

    static boolean LOGGED_IN = true;
    static boolean LOGGED_OUT = false;
    private static ProgressDialog dialog;

    static void showProgressDialog(Context context, String message){
        dialog = new ProgressDialog(context, android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        dialog.setMessage(message);
        dialog.show();
    }

    static void dismissProgressDialog(){
        dialog.dismiss();
    }

    static boolean isLoggedIn(Context context){
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).getBoolean("isLoggedIn", false);
    }
    static void setLogin(Context context, boolean isLoggedIn){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.apply();
    }
    static void saveAccount(Context context, String username, String password){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();
    }

    static AccountInfo getSavedAccount(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return new AccountInfo(pref.getString("username", ""), pref.getString("password", ""));
    }

    static class AccountInfo{
        String username;
        String password;
        AccountInfo(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
