package com.buahbatu.jantung;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;

/**
 * Created by maakbar on 11/20/16.
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidNetworking.initialize(getApplicationContext());
    }
}
