package com.buahbatu.jantung;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Locale;

/**
 * Created by maakbar on 11/20/16.
 * Application class, global initialization area
 */

public class MyApp extends Application {
    private MqttAndroidClient mClient;

    @Override
    public void onCreate() {
        super.onCreate();
        // initialize http client
        AndroidNetworking.initialize(getApplicationContext());
        // initialize mqtt client
        mClient = setupMqtt(getApplicationContext());
    }

    public final MqttAndroidClient getClient() {
        return mClient;
    }

    private MqttAndroidClient setupMqtt(Context context){
        try {
//            String clientId = MqttClient.generateClientId();
            AppSetting.AccountInfo accountInfo = AppSetting.getSavedAccount(context);
            // mqtt client
            MqttAndroidClient mqttClient = new MqttAndroidClient(context,
                    /*MQTT SERVER ADDRESS*/
                    String.format(Locale.US,
                            context.getString(R.string.mqtt_url),
                            context.getString(R.string.server_ip_address)),
                    /*MQTT CLIENT ID*/
                    accountInfo.username/*+"/"+clientId*/);
            mqttClient.connect();
            return mqttClient;
        }catch (MqttException ex){
            Log.e("MqttSetup", "can't connect");
            ex.printStackTrace();
        }
        return null;
    }
}
