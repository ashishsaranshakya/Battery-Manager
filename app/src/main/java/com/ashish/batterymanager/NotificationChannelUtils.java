package com.ashish.batterymanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

public class NotificationChannelUtils {
    public static void setNotificationChannel(Context context){
        String desc="General notifications";
        NotificationChannel channel=new NotificationChannel("General","General", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(desc);
        context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }
    public static final int BATTERY_STATUS=0;
    public static final int BATTERY_LEVEL_TESTING=1;
    public static final int SCREEN_STATUS=2;
}
