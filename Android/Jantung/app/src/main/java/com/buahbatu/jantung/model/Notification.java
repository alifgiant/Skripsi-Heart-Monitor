package com.buahbatu.jantung.model;

/**
 * Created by maakbar on 11/9/16.
 */

public class Notification {
    public static final int HEALTH = 0;
    public static final int SICK = 1;
    public static final int DANGER = 2;

    private String title;
    private String detail;
    private int condition;

    public Notification(String title, String detail, int condition) {
        this.title = title;
        this.detail = detail;
        this.condition = condition;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public int getCondition() {
        return condition;
    }
}
