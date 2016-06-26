package com.motion.lab.pulse.network;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.motion.lab.pulse.R;

import cz.msebera.android.httpclient.HttpEntity;

/**
 * Created by maaakbar on 6/24/16.
 */
public class Nethelper {
    private static final String TAG = "NetHelper";
    private static AsyncHttpClient client = new AsyncHttpClient();

    private static String getWebDomain(Context context){
        return "http://"+context.getString(R.string.domain_url)+":3000/api";
    }

    public static String getMqttDomain(Context context){
        return "tcp://"+context.getString(R.string.domain_url)+":1883";
    }

    public static String getLoginUrl(Context context){
        return getWebDomain(context)+"/login";
    }

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, params, responseHandler);
    }
    public static void get(Context context, String url, HttpEntity entity, String contentType,
                           AsyncHttpResponseHandler responseHandler) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(context, url, entity, contentType, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, params, responseHandler);
    }
    public static void post(Context context, String url, HttpEntity entity, String contentType,
                            AsyncHttpResponseHandler responseHandler) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(context, url, entity, contentType, responseHandler);
    }
}
