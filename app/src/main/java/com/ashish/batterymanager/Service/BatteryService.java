package com.ashish.batterymanager.Service;

import static com.ashish.batterymanager.Provider.BatteryManagerDBHelper.KEY_BATTERY;
import static com.ashish.batterymanager.Provider.BatteryManagerDBHelper.KEY_CURRENT;
import static com.ashish.batterymanager.Provider.BatteryManagerDBHelper.KEY_DATE;
import static com.ashish.batterymanager.Provider.BatteryManagerDBHelper.KEY_SETTING;
import static com.ashish.batterymanager.Provider.BatteryManagerDBHelper.KEY_SETTING_DATA;
import static com.ashish.batterymanager.Provider.BatteryManagerDBHelper.TABLE_MAIN;
import static com.ashish.batterymanager.Provider.BatteryManagerDBHelper.TABLE_SETTINGS;
import static com.ashish.batterymanager.Utils.getCurrent;
import static com.ashish.batterymanager.Utils.getScreenState;
import static com.ashish.batterymanager.NotificationChannelUtils.*;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.ashish.batterymanager.Activity.MainActivity;
import com.ashish.batterymanager.BatteryEntry;
import com.ashish.batterymanager.HistoryEntry;
import com.ashish.batterymanager.Provider.BatteryManagerDBHelper;
import com.ashish.batterymanager.R;
import com.ashish.batterymanager.SettingsConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    long avgPowerForWattage=0;
    int powerCounterForWattage=0;
    int avgPowerForBattery=0;
    int timeChange=0;
    long var=0;
    long var2;
    boolean charging;
    long dateTimeForScreenStatus=0;
    boolean screenStatusCheck=true;
    int lastBatteryForHistory;
    int battery;
    BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @SuppressLint("ServiceCast")
        @Override
        public void onReceive(Context context, Intent intent) {
            battery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            float temp=(float)intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0)/10;
            int voltage=intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);
            Date dt = Calendar.getInstance().getTime();
            long current=getCurrent(BatteryService.this);
            //Log.w(TAG,voltage+" "+current);
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
            avgPowerForWattage = (avgPowerForWattage +(current*voltage));
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
                            //Charging ended
                            addHistoryCharged();
                            lastBatteryForHistory=last_battery;
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
                            //Discharging ended
                            addHistoryDischarged();
                            lastBatteryForHistory=last_battery;
                            break;
                        }
                    }
                    if (RateChange<-30){
                        helper.addBatteryEntry(dt.getTime(), battery, -30, temp, -avgPowerForWattage / (powerCounterForWattage * 1000L));
                        break;
                    }
                    if (RateChange>150){
                        helper.addBatteryEntry(dt.getTime(), battery, 150, temp, -avgPowerForWattage / (powerCounterForWattage * 1000L));
                        break;
                    }
                    if ( timeChange>1000 && RateChange!=0) {
                        updateBatteryReal(avgPowerForBattery, timeChange, voltage);
                    }
                    helper.addBatteryEntry(dt.getTime(), battery, RateChange, temp, -avgPowerForWattage / (powerCounterForWattage * 1000L));
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
                    ping.putExtra(EXTRA_WATTAGE,-avgPowerForWattage / (powerCounterForWattage * 1000L));
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
                        long extra=(RateChange + getAvgChangePositive(RateChange)) / 2;
                        if (Integer.parseInt(helper.getDataFromSettings(SettingsConstants.FINAL_PERCENT))<=battery){
                            builder.setContentTitle(mode);

                            if (Integer.parseInt(helper.getDataFromSettings(SettingsConstants.FINAL_PERCENT))==battery || battery==100){
                                Intent fullScreenIntent = new Intent(context, MainActivity.class);
                                PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, 0,
                                        fullScreenIntent, PendingIntent.FLAG_IMMUTABLE);

                                NotificationCompat.Builder popUpBuilder = new NotificationCompat.Builder(context, "Pop up")
                                        .setSmallIcon(R.drawable.ic_batterymanager)
                                        .setContentTitle("Battery charged !")
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                        .setFullScreenIntent(fullScreenPendingIntent, true);
                                Notification notification=popUpBuilder.build();
                                ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(BATTERY_CHARGED,notification);
                            }
                        }
                        else {

                            if (helper.getDataFromSettings(SettingsConstants.TIME_OR_PERCENT).equals("1")) {
                                int hr = (int) ((Float.parseFloat(helper.getDataFromSettings(SettingsConstants.FINAL_PERCENT)) - battery) / (float) extra);
                                int min = (int) ((((Float.parseFloat(helper.getDataFromSettings(SettingsConstants.FINAL_PERCENT)) - battery) / (float) extra) - hr) * 60);
                                if (hr != 0) {
                                    builder.setContentTitle(mode + " (" + hr + "h " + min + "m to " + helper.getDataFromSettings(SettingsConstants.FINAL_PERCENT) + "%)");
                                } else {
                                    builder.setContentTitle(mode + " (" + min + "m to " + helper.getDataFromSettings(SettingsConstants.FINAL_PERCENT) + "%)");
                                }
                            }
                            else{
                                float time=Float.parseFloat(helper.getDataFromSettings(SettingsConstants.PREDICTION_TIME))/60;
                                int finalBattery= (int) ((extra*time)+battery);
                                if (finalBattery>100) finalBattery=100;
                                builder.setContentTitle(mode + " (" + (int)(time*60) + "m to " + finalBattery + "%)");
                            }
                        }
                        builder.setStyle(new NotificationCompat.InboxStyle()
                                .addLine("Avg: " + (-avgPowerForWattage / (powerCounterForWattage * 1000L))  + " Watt")
                                .addLine("Now: " + (-current * voltage) / 1000L + " Watt")
                                .addLine("Average ROC: " + extra));
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
        }/*else{
            last_battery = getBattery(this);
            charging=false;
        }*/
        Iterator<Long> iterator=helper.readAllScreenStatus().keySet().iterator();
        while(iterator.hasNext()){
            dateTimeForScreenStatus=iterator.next();
        }
        builder=new NotificationCompat.Builder(this,"General");
        builder.setSmallIcon(R.drawable.ic_batterymanager);
        builder.setAutoCancel(false);
        builder.setSilent(true);

        setLastBatteryForHistory();
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

        ArrayList<HistoryEntry> array=helper.readHistory();
        if (array.size()!=0) {
            lastBatteryForHistory=Integer.parseInt(array.get(array.size()-1).getChangeInterval().split("%")[1].substring(1));
        }
        else {
            try {
                ArrayList<BatteryEntry> arrayList = helper.readAll();
                int previousBattery = arrayList.get(arrayList.size() - 1).getBattery();
                for (int i = arrayList.size() - 2; i >= 0; i--) {
                    int term = arrayList.get(i).getBattery();
                    if (previousBattery + 1 < term) {
                        lastBatteryForHistory = term;
                        break;
                        //Charging ended Discharging started
                    } else if (previousBattery - 1 > term) {
                        lastBatteryForHistory = term;
                        break;
                        //Discharging ended Charging started
                    }
                    previousBattery = arrayList.get(i).getBattery();
                }
                Log.w(TAG, String.valueOf(lastBatteryForHistory));
            }
            catch (NumberFormatException e){
                lastBatteryForHistory=helper.readAll().get(0).getBattery();
            }
        }

    }

    void addHistoryCharged(){
        Cursor cursor=helper.queryReadable("SELECT MAX("+KEY_DATE+") FROM "+TABLE_MAIN+" WHERE "+KEY_BATTERY+"="+lastBatteryForHistory+";");
        long initialDate=0;
        ;
        if(cursor.moveToFirst()){
            try{
                initialDate= Long.parseLong(cursor.getString(0));
            }
            catch(Exception e){
                initialDate=helper.readAll().get(0).getDate();
            }
        }

        cursor.close();

        //long diff=(new Date().getTime()-initialDate)/(60000);
        long diff=(last_date-initialDate)/(60000);
        String status="Charged for ";
        if (diff>=60) status=status+(int)(diff/60)+"h "+(int)(diff%60)+"m";
        else status=status+(int)(diff)+"m";

        String changeInterval=lastBatteryForHistory+"%-"+last_battery+"%";

        //cursor=helper.queryReadable("SELECT AVG("+KEY_CURRENT+") FROM "+TABLE_MAIN+" WHERE "+KEY_DATE+"<"+new Date().getTime()+" AND "+KEY_DATE+">"+lastBatteryForHistory+";");
        cursor=helper.queryReadable("SELECT AVG("+KEY_CURRENT+") FROM "+TABLE_MAIN+" WHERE "+KEY_DATE+"<"+last_date+" AND "+KEY_DATE+">"+initialDate+";");
        double power=0;
        if(cursor.moveToFirst()){
            try{
                power= Double.parseDouble(cursor.getString(0));
            }
            catch (Exception ignored){}
        }
        cursor.close();

        String screenOn_avgWattage="Average wattage: "+(int)power+"W";

        //String time=new Date().toString();
        String time=new Date(last_date).toString();
        time=time.substring(4,10)+", "+time.substring(11,16);

        String change="+"+Math.abs(last_battery-lastBatteryForHistory)+"%";

        helper.addHistoryEntry(new HistoryEntry(status,changeInterval,screenOn_avgWattage,time,change));
    }

    void addHistoryDischarged(){
        Cursor cursor=helper.queryReadable("SELECT MAX("+KEY_DATE+") FROM "+TABLE_MAIN+" WHERE "+KEY_BATTERY+"="+lastBatteryForHistory+";");
        long initialDate=0;
        if(cursor.moveToFirst()){
            initialDate= Long.parseLong(cursor.getString(0));
        }
        cursor.close();

        //long diff=(new Date().getTime()-initialDate)/(60000);
        long diff=(last_date-initialDate)/(60000);
        String status="Used for ";
        if (diff>=60) status=status+(int)(diff/60)+"h "+(int)(diff%60)+"m";
        else status=status+(int)(diff)+"m";

        String changeInterval=lastBatteryForHistory+"%-"+last_battery+"%";

        //int screenOn=getUsageStatistics(initialDate,new Date().getTime());
        int screenOn=getUsageStatistics(initialDate,last_date);
        String screenOn_avgWattage="";
        if ((screenOn/60)>0) screenOn_avgWattage="Screen on for: "+ (screenOn/60) + "h " + (screenOn%60)+"m";//TODO Screen on time TBA;
        else screenOn_avgWattage="Screen on for: " + screenOn+"m";

        //String dateTime=new Date().toString();
        String dateTime=new Date(last_date).toString();
        String time=dateTime.substring(4,10)+", "+dateTime.substring(11,16);

        String change="-"+Math.abs(lastBatteryForHistory-last_battery)+"%";

        helper.addHistoryEntry(new HistoryEntry(status,changeInterval,screenOn_avgWattage,time,change));
    }

    /**
     * Not implemented by me
     * @param start_time Start time to record after
     * @param end_time End time to finish recording
     * @return Returns Screen on time in minutes between start_time and end_time
     */
    int getUsageStatistics(long start_time, long end_time) {

        UsageEvents.Event currentEvent;
        //  List<UsageEvents.Event> allEvents = new ArrayList<>();
        HashMap<String, AppUsageInfo> map = new HashMap<>();
        HashMap<String, List<UsageEvents.Event>> sameEvents = new HashMap<>();

        UsageStatsManager mUsageStatsManager = (UsageStatsManager)
                getSystemService(Context.USAGE_STATS_SERVICE);

        int time=0;

        if (mUsageStatsManager != null) {
            // Get all apps data from starting time to end time
            UsageEvents usageEvents = mUsageStatsManager.queryEvents(start_time, end_time);

            // Put these data into the map
            while (usageEvents.hasNextEvent()) {
                currentEvent = new UsageEvents.Event();
                usageEvents.getNextEvent(currentEvent);
                if (currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED ||
                        currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED) {
                    //  allEvents.add(currentEvent);
                    String key = currentEvent.getPackageName();
                    if (map.get(key) == null) {
                        map.put(key, new AppUsageInfo(key));
                        sameEvents.put(key,new ArrayList<UsageEvents.Event>());
                    }
                    sameEvents.get(key).add(currentEvent);
                }
            }

            // Traverse through each app data which is grouped together and count launch, calculate duration
            for (Map.Entry<String,List<UsageEvents.Event>> entry : sameEvents.entrySet()) {
                int totalEvents = entry.getValue().size();
                if (totalEvents > 1) {
                    for (int i = 0; i < totalEvents - 1; i++) {
                        UsageEvents.Event E0 = entry.getValue().get(i);
                        UsageEvents.Event E1 = entry.getValue().get(i + 1);

                        if (E1.getEventType() == 1 || E0.getEventType() == 1) {
                            map.get(E1.getPackageName()).launchCount++;
                        }

                        if (E0.getEventType() == 1 && E1.getEventType() == 2) {
                            long diff = E1.getTimeStamp() - E0.getTimeStamp();
                            map.get(E0.getPackageName()).timeInForeground += diff;
                        }
                    }
                }

                // If First eventtype is ACTIVITY_PAUSED then added the difference of start_time and Event occuring time because the application is already running.
                if (entry.getValue().get(0).getEventType() == 2) {
                    long diff = entry.getValue().get(0).getTimeStamp() - start_time;
                    map.get(entry.getValue().get(0).getPackageName()).timeInForeground += diff;
                }

                // If Last eventtype is ACTIVITY_RESUMED then added the difference of end_time and Event occuring time because the application is still running .
                if (entry.getValue().get(totalEvents - 1).getEventType() == 1) {
                    long diff = end_time - entry.getValue().get(totalEvents - 1).getTimeStamp();
                    map.get(entry.getValue().get(totalEvents - 1).getPackageName()).timeInForeground += diff;
                }
            }

            ArrayList<AppUsageInfo> smallInfoList = new ArrayList<>(map.values());

            String strMsg="";
            // Concatenating data to show in a text view. You may do according to your requirement
            for (AppUsageInfo appUsageInfo : smallInfoList)
            {
                // Do according to your requirement
                if (appUsageInfo.packageName.equals("")) continue;
                strMsg = strMsg.concat(appUsageInfo.packageName + " : " + appUsageInfo.timeInForeground/(1000*60) + "\n\n");
                time+=appUsageInfo.timeInForeground;
            }

            //txt.append(strMsg);
            //txt.append("\n\n"+new Date(start_time)+"\n"+new Date(end_time)+"\n"+time/(1000*60*60)+"h "+time/(1000*60)%(60)+"m\n");
            return time/(1000*60);

        } else {
            Toast.makeText(this, "Sorry...", Toast.LENGTH_SHORT).show();
            return 0;
        }

    }

    class AppUsageInfo {
        Drawable appIcon; // You may add get this usage data also, if you wish.
        String appName, packageName;
        long timeInForeground;
        int launchCount;

        AppUsageInfo(String pName) {
            this.packageName=pName;
        }
    }

    void postNotification(String str){
        NotificationCompat.Builder builder=new NotificationCompat.Builder(BatteryService.this,"General");
        builder.setSmallIcon(R.drawable.ic_batterymanager);
        builder.setContentTitle("Charging");
        builder.setContentText(str);
        builder.setAutoCancel(false);
        builder.setSilent(true);
        Notification notification=builder.build();
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(BATTERY_STATUS,notification);
    }

    /*void postAlarmingNotification(String str){
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

// Set the alarm to start at 8:30 a.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 30);

// setRepeating() lets you specify a precise custom interval--in this case,
// 20 minutes.
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000 * 60 * 20, alarmIntent);
    }*/

}