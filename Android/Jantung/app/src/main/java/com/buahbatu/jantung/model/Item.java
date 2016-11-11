package com.buahbatu.jantung.model;

/**
 * Created by maakbar on 11/8/16.
 */

public class Item {
    protected String name;
    private int itemType;

    public Item(String name, int itemType) {
        this.name = name;
        this.itemType = itemType;
    }

    public String getName() {
        return name;
    }

    public int getItemType() {
        return itemType;
    }
}
