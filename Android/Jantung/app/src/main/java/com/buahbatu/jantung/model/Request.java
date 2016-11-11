package com.buahbatu.jantung.model;

/**
 * Created by maakbar on 11/9/16.
 */

public class Request {
    private String username;
    private String deviceTarget;

    public Request(String username, String deviceTarget) {
        this.username = username;
        this.deviceTarget = deviceTarget;
    }

    public String getUsername() {
        return username;
    }

    public String getDeviceTarget() {
        return deviceTarget;
    }
}
