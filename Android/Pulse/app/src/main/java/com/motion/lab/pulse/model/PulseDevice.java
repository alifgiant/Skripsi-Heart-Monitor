package com.motion.lab.pulse.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by maaakbar on 4/28/16.
 */
public class PulseDevice extends RealmObject{
    @PrimaryKey
    private String id;
    private String name;
    private double heartBeat;
    private String lastChecked;
    private boolean isMale;
    private int status;

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
}
