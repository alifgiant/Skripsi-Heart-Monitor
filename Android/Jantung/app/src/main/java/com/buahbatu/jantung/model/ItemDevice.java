package com.buahbatu.jantung.model;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by maakbar on 11/8/16.
 */

public class ItemDevice extends Item{
    private String deviceId;
    private String full_name;
    private boolean isMale;

    private int condition;
    private float rate;

    public ItemDevice(String name, String full_name, int itemType, String deviceId, boolean isMale) {
        super(name, itemType);
        this.deviceId = deviceId;
        this.full_name = full_name;
        this.isMale = isMale;

        // later change from mqtt data
        Random random = new Random();
        this.rate = random.nextInt(120 /*max*/ - 60 /*min*/ +1) + 70 /*min*/;
        this.condition = random.nextInt(2);  // from 0 to 2 [3 class]
    }

    public String getFull_name() {
        return full_name;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean isMale() {
        return isMale;
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public void setRateRandom() {
        // later change from mqtt data
        Random random = new Random();
        this.rate = random.nextInt(120 /*max*/ - 70 /*min*/ +1) + 70 /*min*/;
        this.condition = random.nextInt(2);  // from 0 to 2 [3 class]
    }
}
