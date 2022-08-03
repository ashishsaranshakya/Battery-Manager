package com.example.batterymanager.Service;

import static com.example.batterymanager.Provider.BatteryManagerDBHelper.KEY_SETTING;
import static com.example.batterymanager.Provider.BatteryManagerDBHelper.KEY_SETTING_DATA;
import static com.example.batterymanager.Provider.BatteryManagerDBHelper.TABLE_SETTINGS;
import static com.example.batterymanager.Utils.getBattery;
import static com.example.batterymanager.Utils.getCurrent;
import static com.example.batterymanager.Utils.getScreenState;
import static com.example.batterymanager.NotificationChannelUtils.*;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.batterymanager.BatteryEntry;
import com.example.batterymanager.HistoryEntry;
import com.example.batterymanager.Provider.BatteryManagerDBHelper;
import com.example.batterymanager.R;
import com.example.batterymanager.SettingsConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class BatteryService extends Service {
    public static final String TAG="Battery Service";

    public static final String ACTION_NEW_ENTRY_ADDED="new entry";
    public static final String EXTRA_DATE="date";
    public static final String EXTRA_BATTERY="battery";
    public static final String EXTRA_CHANGE="change";
    public static final String EXTRA_TEMPERATURE="temperature";
    public static final String EXTRA_CURRENT="current";
    public static final String EXTRA_WATTAGE="wattage";

    public BatteryService() {}

    BatteryManagerDBHelper helper;
    Intent ping=new Intent();

    int last_battery=0;
    long last_date=0;
    long initial_date=0;
    int count=0;
    int avgPowerForWattage=0;
    int powerCounterForWattage=0;
    int avgPowerForBattery=0;
    int timeChange=0;
    long var=0;
    long var2;
    boolean charging;
    long dateTimeForScreenStatus=0;
    boolean screenStatusCheck=true;
    int lastBatteryForHistory;
    BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @SuppressLint("ServiceCast")
        @Override
        public void onReceive(Context context, Intent intent) {
            int battery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            float temp=(float)intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0)/10;
            int voltage=intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);
            Date dt = Calendar.getInstance().getTime();
            long current=getCurrent(BatteryService.this);
            Log.w(TAG,dt.getTime()+" "+battery+" "+temp+" "+voltage+" "+current);
            if(var!=0) {
                long deltaTime=dt.getTime() - var;
                long weightedPower=Math.abs(current * voltage * deltaTime);
                var2 = (long) avgPowerForBattery * timeChange;
                avgPowerForBattery = (int) (( var2 + weightedPower) / (timeChange + deltaTime));
                timeChange = (int) (timeChange + (dt.getTime() - var));
            }
            if (((dt.getTime()-dateTimeForScreenStatus)/1000)>Long.parseLong(helper.getDataFromSettings(SettingsConstants.SAMPLING_RATE_FOR_SCREEN_STATUS))){
                checkScreenStatus(dt.getTime());
                dateTimeForScreenStatus=dt.getTime();
                screenStatusCheck=false;
            }
            var=dt.getTime();
            avgPowerForWattage = (int) (avgPowerForWattage +(current*voltage));
            powerCounterForWattage++;
            if (count == 0) {
                last_date=dt.getTime();
                initial_date =dt.getTime();
                count++;
            }
            if (last_battery != battery){
                for(int i=0;i<1;i++) {
                    if (last_date==dt.getTime()){
                        break;
                    }
                    long RateChange = ((battery - last_battery) * 3600000L / (dt.getTime() - last_date));
                    if (RateChange<-30){
                        helper.addBatteryEntry(dt.getTime(), battery, -30, temp, -avgPowerForWattage / (powerCounterForWattage * 10000L));
                        break;
                    }
                    if (RateChange>150){
                        helper.addBatteryEntry(dt.getTime(), battery, 150, temp, -avgPowerForWattage / (powerCounterForWattage * 10000L));
                        break;
                    }
                    if (charging) {
                        if (RateChange < 1) {
                            charging = !charging;
                            NotificationCompat.Builder builder=new NotificationCompat.Builder(BatteryService.this,"General");
                            builder.setSmallIcon(R.drawable.ic_batterymanager);
                            builder.setContentTitle("Discharging");
                            builder.setAutoCancel(false);
                            builder.setSilent(true);
                            Notification notification=builder.build();
                            notification.flags|=Notification.FLAG_NO_CLEAR;
                            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(BATTERY_STATUS,notification);
                            break;
                        }
                    } else {
                        if (RateChange > 0) {
                            charging = !charging;
                            NotificationCompat.Builder builder=new NotificationCompat.Builder(BatteryService.this,"General");
                            builder.setSmallIcon(R.drawable.ic_batterymanager);
                            builder.setContentTitle("Charging");
                            builder.setAutoCancel(false);
                            builder.setSilent(true);
                            Notification notification=builder.build();
                            notification.flags|=Notification.FLAG_NO_CLEAR;
                            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(BATTERY_STATUS,notification);
                            break;
                        }
                    }
                    if ( timeChange>1000 && RateChange!=0) {
                        updateBatteryReal(avgPowerForBattery, timeChange, voltage);
                    }
                    helper.addBatteryEntry(dt.getTime(), battery, RateChange, temp, -avgPowerForWattage / (powerCounterForWattage * 10000L));
                    if (screenStatusCheck) {
                        checkScreenStatus(dt.getTime());
                    }
                    count++;
                    Log.w(TAG,"New entry");
                    ping=new Intent();
                    ping.setAction(ACTION_NEW_ENTRY_ADDED);
                    ping.putExtra(EXTRA_DATE,dt.getTime());
                    ping.putExtra(EXTRA_BATTERY,battery);
                    ping.putExtra(EXTRA_CHANGE,RateChange);
                    ping.putExtra(EXTRA_TEMPERATURE,temp);
                    ping.putExtra(EXTRA_CURRENT,current);
                    ping.putExtra(EXTRA_WATTAGE,-avgPowerForWattage / (powerCounterForWattage * 10000L));
                    sendBroadcast(ping);
                    if(RateChange>0) {
                        String mode;
                        switch (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,-1)){
                            case BatteryManager.BATTERY_PLUGGED_AC:
                                mode="Charging via AC charger";
                                break;
                            case BatteryManager.BATTERY_PLUGGED_USB:
                                mode="Charging via USB";
                                break;
                            default:
                                mode="Charging from unknown source";
                        }
                        NotificationCompat.Builder builder=new NotificationCompat.Builder(BatteryService.this,"General");
                        builder.setSmallIcon(R.drawable.ic_batterymanager);
                        builder.setAutoCancel(false);
                        builder.setSilent(true);
                        if (Integer.parseInt(helper.getDataFromSettings(SettingsConstants.FINAL_PERCENT))<=battery){
                            builder.setContentTitle(mode);
                        }else {
                            long extra=RateChange;
                            RateChange = (RateChange + getAvgChangePositive(RateChange)) / 2;
                            builder.setStyle(new NotificationCompat.InboxStyle()
                                    .addLine("Avg: " + (-avgPowerForWattage / (powerCounterForWattage * 10L)) / 100 + " Watt")
                                    .addLine("Now: " + (-current * voltage) / 1000 + " Watt")
                                    .addLine("Average ROC: " + extra));
                            if (helper.getDataFromSettings(SettingsConstants.TIME_OR_PERCENT).equals("1")) {
                                int hr = (int) ((Float.parseFloat(helper.getDataFromSettings(SettingsConstants.FINAL_PERCENT)) - battery) / (float) RateChange);
                                int min = (int) ((((Float.parseFloat(helper.getDataFromSettings(SettingsConstants.FINAL_PERCENT)) - battery) / (float) RateChange) - hr) * 60);
                                if (hr != 0) {
                                    builder.setContentTitle(mode + " (" + hr + "h " + min + "m to " + helper.getDataFromSettings(SettingsConstants.FINAL_PERCENT) + "%)");
                                } else {
                                    builder.setContentTitle(mode + " (" + min + "m to " + helper.getDataFromSettings(SettingsConstants.FINAL_PERCENT) + "%)");
                                }
                            }
                            else{
                                float time=Float.parseFloat(helper.getDataFromSettings(SettingsConstants.PREDICTION_TIME))/60;
                                int finalBattery= (int) ((RateChange*time)+battery);
                                if (finalBattery>100) finalBattery=100;
                                builder.setContentTitle(mode + " (" + (int)(time*60) + "m to " + finalBattery + "%)");
                            }
                        }
                        Notification notification=builder.build();
                        notification.flags|=Notification.FLAG_NO_CLEAR;
                        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(BATTERY_STATUS,notification);
                        Log.w(TAG,"Notification posted");
                    }else{
                        NotificationCompat.Builder builder=new NotificationCompat.Builder(BatteryService.this,"General");
                        builder.setSmallIcon(R.drawable.ic_batterymanager);
                        builder.setContentTitle("Discharging");
                        builder.setAutoCancel(false);
                        builder.setSilent(true);
                        RateChange=(RateChange+ getAvgChangeNegative())/2;
                        int hr= -(int) ((float)battery/(float)RateChange);
                        int min= -(int) (((battery/(float)RateChange) +hr)*60);
                        builder.setContentText("Time remaining "+ hr+"h "+min+"m  Avg ROC: "+getAvgChangeNegative());
                        Notification notification=builder.build();
                        notification.flags|=Notification.FLAG_NO_CLEAR;
                        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(BATTERY_STATUS,notification);
                    }
                }
                avgPowerForBattery=0;
                timeChange=0;
                last_battery = battery;
                last_date = dt.getTime();
                powerCounterForWattage =0;
                avgPowerForWattage =0;
                screenStatusCheck=true;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initVariables();
        registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        Log.w(TAG,"onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.w(TAG,"onStartCommand");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mBatteryReceiver);
        super.onDestroy();
    }

    public void initVariables(){
        helper=new BatteryManagerDBHelper(this);
        count = helper.readAll().size();
        if(count!=0){
            last_battery = helper.readAll().get(helper.readAll().size()-1).getBattery();
            last_date=helper.readAll().get(helper.readAll().size()-1).getDate();
            initial_date =helper.readAll().get(0).getDate();
            if (helper.readAll().get(helper.readAll().size()-1).getChange()<0){
                charging=false;
            }else{
                charging=true;
            }
        }else{
            last_battery = getBattery(this);
            charging=false;
        }
        Iterator<Long> iterator=helper.readAllScreenStatus().keySet().iterator();
        while(iterator.hasNext()){
            dateTimeForScreenStatus=iterator.next();
        }
        builder=new NotificationCompat.Builder(this,"General");
        builder.setSmallIcon(R.drawable.ic_batterymanager);
        builder.setAutoCancel(false);
        builder.setSilent(true);
    }

    public int getAvgChangeNegative(){
        ArrayList<BatteryEntry> data=helper.readAll();
        long avg=0;
        int count=0;
        for(int i=0;i<data.size();i++){
            if (data.get(i).getChange()<1) {
                avg = avg + data.get(i).getChange();
                count++;
            }
        }
        if (count==0){
            return 0;
        }
        return (int)(avg/count);
    }

    public int getAvgChangePositive(long change){
        ArrayList<BatteryEntry> data=helper.readAll();
        long avg=0;
        int count=0;
        for(int i=0;i<data.size();i++){
            if (change<40){
                if (data.get(i).getChange()<41 && data.get(i).getChange()>0){
                    avg = avg + data.get(i).getChange();
                    count++;
                }
            }else if (change<51){
                if (data.get(i).getChange()<51 && data.get(i).getChange()>0){
                    avg = avg + data.get(i).getChange();
                    count++;
                }
            }else{
                if (data.get(i).getChange()>50){
                        avg = avg + data.get(i).getChange();
                        count++;
                }
            }
        }
        if (count==0){
            return (int) change;
        }
        return (int)(avg/count);
    }

    NotificationCompat.Builder builder;
    NotificationCompat.BigTextStyle style;
    ArrayList<Integer> rec=new ArrayList<>();
    int c=0;
    public Object updateBatteryReal(int power,long timeChange,int voltage){
        power=Math.abs(power/10000);
        int entries =Integer.parseInt(helper.getDataFromSettings(SettingsConstants.NO_OF_ENTRIES));
        int actualBattery = Integer.parseInt(helper.getDataFromSettings(SettingsConstants.ACTUAL_BATTERY));
        long bat= Math.round ((power*timeChange)/(voltage*3.6));
        int min= (int) (0.8*actualBattery);
        int max= (int) (1.25*actualBattery);
        rec.add((int) bat);
        if (bat>max || bat<min){
            c=c+1;
            return null;
        }
        int var=actualBattery*entries;
        actualBattery= (int) Math.ceil(((var)+bat)/(++entries));
        helper.queryWriteable("UPDATE "+ TABLE_SETTINGS+" SET "+KEY_SETTING_DATA+"="+actualBattery+
                " WHERE "+KEY_SETTING+"=\""+SettingsConstants.ACTUAL_BATTERY+"\"");
        helper.queryWriteable("UPDATE "+ TABLE_SETTINGS+" SET "+KEY_SETTING_DATA+"="+entries+
                " WHERE "+KEY_SETTING+"=\""+SettingsConstants.NO_OF_ENTRIES+"\"");
        style=new NotificationCompat.BigTextStyle();
        style.bigText(rec.toString()+"\nNo of deviation: "+c);
        builder.setStyle(style);
        Notification notification=builder.build();
        notification.flags|=Notification.FLAG_NO_CLEAR;
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(BATTERY_LEVEL_TESTING,notification);
        return null;
    }

    void checkScreenStatus(long date){
        boolean ch=getScreenState(this);
        helper.addScreenStatus(date,ch);
    }

    void setLastBatteryForHistory(){
        ArrayList<BatteryEntry> arrayList= helper.readAll();
        int previousBattery=arrayList.get(arrayList.size()-1).getBattery();
        for (int i=arrayList.size()-2;i>=0;i--){
            int term=arrayList.get(i).getBattery();
            if(previousBattery+1<term){
                //Charging ended Discharging started
            }
            else if(previousBattery-1>term){
                //
            }
            previousBattery=arrayList.get(i).getBattery();
        }
    }

}