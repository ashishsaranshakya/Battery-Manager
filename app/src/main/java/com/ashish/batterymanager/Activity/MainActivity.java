package com.ashish.batterymanager.Activity;

import static com.ashish.batterymanager.NotificationChannelUtils.setNotificationChannel;
import static com.ashish.batterymanager.SettingsConstants.getRadioGroupFromTableSetting;
import static com.ashish.batterymanager.SettingsConstants.getTimeDifferenceFromProgress;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.ashish.batterymanager.R;
import com.ashish.batterymanager.BatteryEntry;
import com.ashish.batterymanager.Provider.BatteryManagerDBHelper;
import com.ashish.batterymanager.Service.BatteryService;
import com.ashish.batterymanager.SettingsConstants;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class MainActivity extends LogActivity {
    TextView txt;
    GraphView graph;
    Button btnTemp;
    Button btnChange;
    Button btnText;

    BatteryManagerDBHelper helper;
    int count;

    LineGraphSeries<DataPoint> seriesBattery=new LineGraphSeries<>();
    LineGraphSeries<DataPoint> seriesChange=new LineGraphSeries<>();
    LineGraphSeries<DataPoint> seriesTemperature=new LineGraphSeries<>();
    LineGraphSeries<DataPoint> seriesScreenStatus=new LineGraphSeries<>();
    ArrayList<BatteryEntry> arrayList=new ArrayList<>();

    BroadcastReceiver mReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(TAG,"Inside main receiver");
            try {
                seriesBattery.appendData(new DataPoint(intent.getLongExtra(BatteryService.EXTRA_DATE, 0), intent.getIntExtra(BatteryService.EXTRA_BATTERY, 0)), false, count + 1);
                graph.getViewport().setMaxX(intent.getLongExtra(BatteryService.EXTRA_DATE, -1));
                arrayList.add(new BatteryEntry(intent.getLongExtra(BatteryService.EXTRA_DATE, 0), intent.getIntExtra(BatteryService.EXTRA_BATTERY, 0), intent.getLongExtra(BatteryService.EXTRA_CHANGE, 0), intent.getFloatExtra(BatteryService.EXTRA_TEMPERATURE, 0), intent.getLongExtra(BatteryService.EXTRA_CURRENT, 0)));
                count++;
            }
            catch (Exception e){}
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVariables();
        startService(new Intent(this, BatteryService.class));
        initView();
        updateSettings();
        setHistory();
        initGraph();
        registerReceiver(mReceiver,new IntentFilter(BatteryService.ACTION_NEW_ENTRY_ADDED));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.icon_setting:
                startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initializes graph view and sets required components
     */
    private void initGraph() {
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX){
                    String d=new Date((long) value).toString();
                    return d.substring(11,19)+'\n'+d.substring(4,10);
                }
                else{
                    return super.formatLabel(value,isValueX);
                }
            }
        });
        seriesBattery.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                for(int i=0;i<arrayList.size();i++){
                    if (arrayList.get(i).getDate()==dataPoint.getX()){
                        txt.append(arrayList.get(i).toString()+'\n'+'\n');
                        break;
                    }
                }
            }
        });
        if (getRadioGroupFromTableSetting(helper.getDataFromSettings(SettingsConstants.MAIN_WINDOW_PRESET)).get(0)) {
            seriesBattery.setTitle("Battery Percentage");
            seriesBattery.setColor(Color.GREEN);
            graph.addSeries(seriesBattery);
        }
        if (getRadioGroupFromTableSetting(helper.getDataFromSettings(SettingsConstants.MAIN_WINDOW_PRESET)).get(1)) {
            seriesChange.setTitle("Rate of Change");
            graph.getSecondScale().addSeries(seriesChange);
            graph.getSecondScale().setMinY(-30);
            graph.getSecondScale().setMaxY(70);
            seriesChange.setColor(Color.BLUE);
        }
        if (getRadioGroupFromTableSetting(helper.getDataFromSettings(SettingsConstants.MAIN_WINDOW_PRESET)).get(2)) {
            seriesTemperature.setTitle("Temperature");
            seriesTemperature.setColor(Color.RED);
            graph.addSeries(seriesTemperature);
        }
        if (getRadioGroupFromTableSetting(helper.getDataFromSettings(SettingsConstants.MAIN_WINDOW_PRESET)).get(3)) {
            seriesScreenStatus.setTitle("Screen Status");
            seriesScreenStatus.setColor(Color.LTGRAY);
            graph.addSeries(seriesScreenStatus);
        }
        graph.getGridLabelRenderer().setNumHorizontalLabels(3);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    /**
     * Initialize variables
     */
    public void initVariables(){
        helper=new BatteryManagerDBHelper(this);
        setNotificationChannel(this);
    }

    /**
     * Connects layout components to variables
     */
    public void initView(){
        txt = findViewById(R.id.txt);
        graph = findViewById(R.id.graph);
        btnTemp =findViewById(R.id.btntemp);
        btnChange =findViewById(R.id.btnchange);
        btnText =findViewById(R.id.btnbluetooth);
        setBtnClick();
    }

    /**
     * Sets onClickListeners for all buttons
     */
    public void setBtnClick(){
        btnTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), TempActivity.class));
            }
        });

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ChangeActivity.class));
            }
        });

        btnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView t=findViewById(R.id.txt2);
                t.setText(allText());
            }
        });

        findViewById(R.id.btnaux).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HistoryActivity.class));

                /*helper.queryWriteable("UPDATE "+ TABLE_SETTINGS+" SET "+KEY_SETTING_DATA+"="+4500+
                        " WHERE "+KEY_SETTING+"=\""+SettingsConstants.ACTUAL_BATTERY+"\"");
                helper.queryWriteable("UPDATE "+ TABLE_SETTINGS+" SET "+KEY_SETTING_DATA+"="+10+
                        " WHERE "+KEY_SETTING+"=\""+SettingsConstants.NO_OF_ENTRIES+"\"");*/
            }
        });

        findViewById(R.id.btnService).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showStats();
                startActivity(new Intent(getApplicationContext(),MainActivity2.class));
            }
        });

        findViewById(R.id.btn4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper.refreshTable(BatteryManagerDBHelper.TABLE_HISTORY);
            }
        });
    }

    /**
     * Fetch data from table
     * For debugging
     * @return top 100 entries from BatteryData table
     */
    public String allText(){
        String st="";
        ArrayList<BatteryEntry> a=helper.readAll();
        int x=a.size()-100;
        if(x<0){
            x=0;
        }
        for(int i=x;i<a.size();i++){
            st=st+a.get(i)+"\n\n";
        }
        HashMap<String,String> map=helper.readAllSetting();
        Object[] set= map.keySet().toArray();
        for(int i=0;i<map.size();i++){
            st=st+set[i]+":"+map.get(set[i].toString())+"\n\n";
        }
        return st;
    }

    /**
     * Get data from database and set data points for graph
     */
    public void setHistory(){
        arrayList=helper.readAll();
        count=arrayList.size();
        if (count!=0) {
            for (int i = 0; i < count; i++) {
                seriesBattery.appendData(new DataPoint(new Date(arrayList.get(i).getDate()), arrayList.get(i).getBattery()), false, count);
                if (arrayList.get(i).getChange()<71) seriesChange.appendData(new DataPoint(new Date(arrayList.get(i).getDate()), arrayList.get(i).getChange()), false, count);
                else seriesChange.appendData(new DataPoint(new Date(arrayList.get(i).getDate()), 70), false, count);
                seriesTemperature.appendData(new DataPoint(new Date(arrayList.get(i).getDate()), arrayList.get(i).getTemp()), false, count);
            }
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMaxX(arrayList.get(count - 1).getDate());
            graph.getViewport().setMinX(arrayList.get(count - 1).getDate() - getTimeDifferenceFromProgress(Integer.parseInt(helper.getDataFromSettings(SettingsConstants.WINDOW_SIZE))));
            long var=arrayList.get(count-1).getDate()-arrayList.get(0).getDate();
            txt.append((int)(var/86400000L)+"d "+(int)(var%86400000L)/3600000L+"h "+(int)(var%86400000L)%3600000L/60000L+"m\n\n");
        }else {
            BatteryManager manager=(BatteryManager)getSystemService(BATTERY_SERVICE);
            helper.addBatteryEntry(Calendar.getInstance().getTime().getTime(),manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY),0,32f,0);
            count++;
            arrayList=helper.readAll();
            seriesBattery.appendData(new DataPoint(new Date(arrayList.get(0).getDate()), arrayList.get(0).getBattery()), false, count);
            seriesChange.appendData(new DataPoint(new Date(arrayList.get(0).getDate()),arrayList.get(0).getChange()), false, count);
            seriesTemperature.appendData(new DataPoint(new Date(arrayList.get(0).getDate()), arrayList.get(0).getTemp()), false, count);
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMaxX(arrayList.get(0).getDate());
        }

        int countScreenSample=helper.readAllScreenStatus().size();
        LinkedHashMap<Long,Boolean> hashMap=helper.readAllScreenStatus();
        Iterator<Long> iterator=hashMap.keySet().iterator();
        while(iterator.hasNext()){
            long date=iterator.next();
            int k=20;
            if(hashMap.get(date)) k=40;
            seriesScreenStatus.appendData(new DataPoint(new Date(date),k),false,countScreenSample);
        }
    }

    /**
     * Deleting old data according to user chosen period and
     * also checking if all settings are present in table
     */
    public void updateSettings(){
        if(helper.readAllSetting().size()!=SettingsConstants.TOTAL_SETTINGS){
            helper.addSetting(SettingsConstants.HISTORY_PERIOD,"0");
            helper.addSetting(SettingsConstants.FINAL_PERCENT,"90");
            BatteryManager manager=(BatteryManager)getSystemService(BATTERY_SERVICE);
            helper.addSetting(SettingsConstants.ACTUAL_BATTERY,String.valueOf((manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)/manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY))/10));
            helper.addSetting(SettingsConstants.NO_OF_ENTRIES,"1");
            helper.addSetting(SettingsConstants.FULL_CAPACITY,String.valueOf((manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)/manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY))/10));
            helper.addSetting(SettingsConstants.SAMPLING_RATE_FOR_SCREEN_STATUS,"30");
            helper.addSetting(SettingsConstants.PREDICTION_TIME,"30");
            helper.addSetting(SettingsConstants.TIME_OR_PERCENT,"1");
            helper.addSetting(SettingsConstants.WINDOW_SIZE,"3");
            helper.addSetting(SettingsConstants.MAIN_WINDOW_PRESET,"1001");
            helper.addSetting(SettingsConstants.LAST_AGGREGATE_TIME, String.valueOf(new Date().getTime()));
            Log.w(TAG,"Settings added");
        }
        helper.changeHistory(Integer.parseInt(helper.getDataFromSettings(SettingsConstants.HISTORY_PERIOD)));
    }

    public void showStats()
    {
        @SuppressWarnings("WrongConstant")
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        //List<UsageStats> appList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                //time-1000*60*60*12,time);
        UsageEvents usageEvents= usageStatsManager.queryEvents(time-1000*60*60*12,time);
        while (usageEvents.hasNextEvent()){
            UsageEvents.Event event=new UsageEvents.Event();
            usageEvents.getNextEvent(event);
        }
        String str="";
        int total=0;
        //long minTime=appList.get(0).getFirstTimeStamp();
        //long maxTime=appList.get(0).getLastTimeStamp();
        /*for (int i=0;i<appList.size();i++){
            str=str+appList.get(i).getPackageName()+" "+
                    appList.get(i).getTotalTimeInForeground()/(1000*60)+" "+
                    appList.get(i).getTotalTimeVisible()/(1000*60)+" "+
                    appList.get(i).getTotalTimeForegroundServiceUsed()/(1000*60)+",\n";
            if (appList.get(i).getFirstTimeStamp()<minTime){
                minTime=appList.get(i).getFirstTimeStamp();
                txt.append(appList.get(i).getPackageName()+"\n\n");
            }
            if (appList.get(i).getLastTimeStamp()>maxTime){
                maxTime=appList.get(i).getLastTimeStamp();
                txt.append(appList.get(i).getPackageName()+"\n\n");
            }
            total+=appList.get(i).getTotalTimeInForeground()/(1000*60);
            printUsageStats(appList.get(i),txt);
        }
        txt.append(str);
        txt.append(String.valueOf(total/60));
        txt.append("\n"+ new Date(minTime)+" : "+new Date(time-1000*60*60*12));
        txt.append("\n"+ new Date(maxTime)+" : "+new Date(time));*/
    }
    public void getPermission() {
        txt.setText(String.valueOf(Settings.canDrawOverlays(this)));
        //Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        Intent intent=new Intent(Settings.ACTION_MEMORY_CARD_SETTINGS);
        //intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$SecuritySettingsActivity"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent, null);
    }

}