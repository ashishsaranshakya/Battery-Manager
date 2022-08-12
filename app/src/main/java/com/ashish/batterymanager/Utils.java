package com.ashish.batterymanager;

import android.annotation.SuppressLint;
import android.app.usage.UsageStats;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.BatteryManager;
import android.view.Display;
import android.widget.TextView;

import java.util.Date;

public class Utils {
    private Utils(){}


    public static long getCurrent(Context c){
        BatteryManager mBat =(BatteryManager) c.getSystemService(Context.BATTERY_SERVICE);
        return mBat.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
    }

    public static int getBattery(Context c){
        BatteryManager mBat=(BatteryManager) c.getSystemService(Context.BATTERY_SERVICE);
        return mBat.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    @SuppressLint("ServiceCast")
    public static boolean getScreenState(Context c){
        Display[] d=((DisplayManager)c.getSystemService(Context.DISPLAY_SERVICE)).getDisplays();
        for (Display display:d){
            if (display.getState()==Display.STATE_ON){
                return true;
            }
        }
        return false;
    }

    public static void printUsageStats(UsageStats usageStats, TextView textView){
        String str=usageStats.getPackageName()+"\n"+
                "Visible time:"+usageStats.getTotalTimeVisible()/1000/60/60+"h\n"+
                "Foreground time:"+usageStats.getTotalTimeInForeground()/1000/60/60+"h\n"+
                "Foreground service"+usageStats.getTotalTimeForegroundServiceUsed()/1000/60/60+"h\n"+
                "First time stamp:"+new Date(usageStats.getFirstTimeStamp())+"\n"+
                "Last time stamp:"+new Date(usageStats.getLastTimeStamp())+"\n"+
                "Last time used:"+new Date(usageStats.getLastTimeUsed())+"\n"+
                "Last time visible:"+new Date(usageStats.getLastTimeVisible())+"\n"+
                "Last time foreground:"+new Date(usageStats.getLastTimeForegroundServiceUsed())+"\n\n";
        textView.append(str);
    }


}
