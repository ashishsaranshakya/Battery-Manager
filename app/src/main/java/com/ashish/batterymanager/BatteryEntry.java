package com.ashish.batterymanager;

import java.util.Date;

public class BatteryEntry{
    private long date;
    private int battery;
    private long change;
    private float temp;
    private long current;

    public BatteryEntry(String date, String battery, String change, String temp, String current) {
        this.date = Long.valueOf(date);
        this.battery = Integer.valueOf(battery);
        this.change = Long.valueOf(change);
        this.temp = Float.valueOf(temp);
        this.current = Long.valueOf(current);
    }

    public BatteryEntry(long date, int battery, long change, float temp, long current) {
        this.date = date;
        this.battery = battery;
        this.change = change;
        this.temp = temp;
        this.current = current;
    }

    public long getDate() {
        return date;
    }

    public int getBattery() {
        return battery;
    }

    public long getChange() {
        return change;
    }

    public float getTemp() {
        return temp;
    }

    public long getCurrent() {
        return current;
    }

    @Override
    public String toString() {
        return "BatteryEntry{" + "Date=" + new Date(date).toString() + ", Battery=" + battery + ", Change=" + change + ", Temp=" + temp + ", Current=" + current + '}';
    }
}
