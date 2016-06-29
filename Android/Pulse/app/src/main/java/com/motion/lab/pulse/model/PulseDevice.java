package com.motion.lab.pulse.model;

import android.util.Log;

import com.motion.lab.pulse.adapter.DeviceViewAdapter;
import com.motion.lab.pulse.network.MqttHandler;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class PulseDevice extends RealmObject{
    @PrimaryKey
    private String id;
    private String name;
    private double heartBeat;
    private String lastChecked;
    private boolean isMale;
    private int status;

    @Ignore
    private DeviceViewAdapter.ViewHolder viewHolder;
    @Ignore
    private MqttHandler.MqttMessageListener messageListener;

    public String getId() {
        return id;
    }

    public PulseDevice setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public PulseDevice setName(String name) {
        this.name = name;
        return this;
    }

    public double getHeartBeat() {
        return heartBeat;
    }

    public PulseDevice setHeartBeat(double heartBeat) {
        this.heartBeat = heartBeat;
        return this;
    }

    public String getLastChecked() {
        return lastChecked;
    }

    public PulseDevice setLastChecked(String lastChecked) {
        this.lastChecked = lastChecked;
        return this;
    }

    public boolean isMale() {
        return isMale;
    }

    public PulseDevice setMale(boolean male) {
        isMale = male;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public PulseDevice setStatus(int status) {
        this.status = status;
        return this;
    }

    public DeviceViewAdapter.ViewHolder getViewHolder() {
        return viewHolder;
    }

    public PulseDevice setViewHolder(DeviceViewAdapter.ViewHolder viewHolder) {
        this.viewHolder = viewHolder;
        return this;
    }

    public MqttHandler.MqttMessageListener getMessageListener() {
        return messageListener;
    }

    public PulseDevice createMessageListener() {
        this.messageListener = new MqttHandler.MqttMessageListener() {
            @Override
            public void onMessageArrive(String topic, MqttMessage message) {
                Log.i("PulseDevice", "onMessageArrive topic: "+topic+ " message : "+message);
                if (viewHolder!=null){
                    viewHolder.addData(Float.parseFloat(message.toString()), name);
                }
            }
        };
        return this;
    }
}
