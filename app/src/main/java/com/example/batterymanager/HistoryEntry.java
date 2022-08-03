package com.example.batterymanager;

public class HistoryEntry {
    private String status;
    private String changeInterval;
    private String screenOn_avgWattage;
    private String time;
    private String change;

    public HistoryEntry(String status, String changeInterval, String screenOn_avgWattage, String time, String change) {
        this.status = status;
        this.changeInterval = changeInterval;
        this.screenOn_avgWattage = screenOn_avgWattage;
        this.time = time;
        this.change = change;
    }

    public String getStatus() {
        return status;
    }

    public String getChangeInterval() {
        return changeInterval;
    }

    public String getScreenOn_avgWattage() {
        return screenOn_avgWattage;
    }

    public String getTime() {
        return time;
    }

    public String getChange() {
        return change;
    }

    @Override
    public String toString() {
        return "HistoryEntry{" +
                "status='" + status + '\'' +
                ", changeInterval='" + changeInterval + '\'' +
                ", screenOn_avgWattage='" + screenOn_avgWattage + '\'' +
                ", time='" + time + '\'' +
                ", change='" + change + '\'' +
                '}';
    }
}
