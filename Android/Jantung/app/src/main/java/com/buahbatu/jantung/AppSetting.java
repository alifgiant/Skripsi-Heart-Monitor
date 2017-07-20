package com.buahbatu.jantung;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Locale;

class AppSetting {
    private static String PREFERENCE_NAME = "JANTUNG PREF";

    static boolean LOGGED_IN = true;
    static boolean LOGGED_OUT = false;
    private static ProgressDialog dialog;

    static MqttAndroidClient getMqttClient(Context context){
        //            String clientId = MqttClient.generateClientId();
        System.out.println("mqtt address "+AppSetting.getMqttAddress(context));
        AppSetting.AccountInfo accountInfo = AppSetting.getSavedAccount(context);
        // mqtt client
        MqttAndroidClient mqttClient = new MqttAndroidClient(context,
                    /*MQTT SERVER ADDRESS*/
                AppSetting.getMqttAddress(context),
                    /*MQTT CLIENT ID*/
                accountInfo.username/*+"/"+clientId*/);
        return mqttClient;
    }

    static void saveIp(Context context, String ip, String port){
        SharedPreferences.Editor edit = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit();
        edit.putString("ip", ip);
        edit.putString("port", port);
        edit.apply();
    }

    static String getHttpAddress(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        String ip = pref.getString("ip", "");
        String port = pref.getString("port", "");
        if (ip.equals(""))
            ip = context.getString(R.string.server_ip_address);
        if (port.equals(""))
            port = context.getString(R.string.server_http_port);
        return String.format(Locale.US, context.getString(R.string.http_url), ip+":"+port);
    }

    static String getMqttAddress(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        String ip = pref.getString("ip", "");
        if (ip.equals(""))
            ip = context.getString(R.string.server_ip_address);
        return String.format(Locale.US, context.getString(R.string.mqtt_url), ip);
    }

    static void showProgressDialog(Context context, String message){
        dialog = new ProgressDialog(context, android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        dialog.setMessage(message);
        dialog.show();
    }

    static void makeACall(Activity context, String number){
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
//        intent.setData(Uri.parse("tel:" + number));
        int res = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE);
        if (res == PackageManager.PERMISSION_GRANTED)
            context.startActivity(intent);
        else ActivityCompat.requestPermissions(context,
                new String[]{Manifest.permission.READ_CONTACTS}, 0 /*REQUEST CODE*/);
    }

    static void makeASms(Context context, String number){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + number));
        context.startActivity(intent);
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
        public String full_name;
        String password;
        String username;

        AccountInfo(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
