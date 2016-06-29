package com.motion.lab.pulse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

/**
 * Created by maaakbar on 4/28/16.
 */
public class AppConfig {

    // region CONNECTIVITY STATUS
    private static final String PREFERENCE_KEY = "pulse app";
    private static final String LOGGED_STAT = "status";
    public static final boolean LOGGED_IN = true;
    public static final boolean LOGGED_OUT = false;
    private static SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
    }
    private static SharedPreferences.Editor getSharedPreferencesEditor(Context context){
        return getSharedPreferences(context).edit();
    }

    public static boolean isLoggedIn(Context context){
        return getSharedPreferences(context).getBoolean(LOGGED_STAT, false);
    }
    public static void saveLoggedStatus(Context context, boolean isLoggedIn){
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        editor.putBoolean(LOGGED_STAT, isLoggedIn);
        editor.apply();
    }
    // endregion

    // region PAGE NAVIGATION
    private static final String BUNDLE_EXTRA_KEY = "extra";
    public static void movePage(Context context, Class<?> cls, Bundle bundle) {
        Intent move = new Intent(context, cls);
        move.putExtra(BUNDLE_EXTRA_KEY, bundle);
        context.startActivity(move);
    }
    public static void movePage(Context context, Class<?> cls) {
        Intent move = new Intent(context, cls);
        context.startActivity(move);
    }
    public static void movePageAndFinish(Context context, Class<?> cls, Bundle bundle) {
        movePage(context, cls, bundle);
        ((Activity)context).finish();
    }
    public static void movePageAndFinish(Context context, Class<?> cls) {
        movePage(context, cls);
        ((Activity)context).finish();
    }

    private static Bundle getBundleFromNavigation(Activity activity){
        return activity.getIntent().getBundleExtra(BUNDLE_EXTRA_KEY);
    }

    public static String getStringFromNavigationBundle(Activity activity, String key){
        return getBundleFromNavigation(activity).getString(key);
    }
    // endregion
}
