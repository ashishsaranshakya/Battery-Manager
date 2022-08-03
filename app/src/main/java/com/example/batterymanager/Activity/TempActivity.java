package com.example.batterymanager.Activity;


import static com.example.batterymanager.SettingsConstants.getTimeDifferenceFromProgress;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.batterymanager.BatteryEntry;
import com.example.batterymanager.Provider.BatteryManagerDBHelper;
import com.example.batterymanager.R;
import com.example.batterymanager.Service.BatteryService;
import com.example.batterymanager.SettingsConstants;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Date;

public class TempActivity extends LogActivity {
    GraphView graph;
    TextView txt;

    BatteryManagerDBHelper helper;
    int count;

    ArrayList<BatteryEntry> arrayList=new ArrayList<>();
    LineGraphSeries<DataPoint> seriesBattery=new LineGraphSeries<>();
    LineGraphSeries<DataPoint> seriesTemp =new LineGraphSeries<>();

    BroadcastReceiver mReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(TAG,"Inside temp receiver");
            seriesBattery.appendData(new DataPoint(intent.getLongExtra(BatteryService.EXTRA_DATE,0),intent.getIntExtra(BatteryService.EXTRA_BATTERY,0)),false,count+1);
            graph.getViewport().setMaxX(intent.getLongExtra(BatteryService.EXTRA_DATE,-1));
            arrayList.add(new BatteryEntry(intent.getLongExtra(BatteryService.EXTRA_DATE,0),intent.getIntExtra(BatteryService.EXTRA_BATTERY,0),intent.getLongExtra(BatteryService.EXTRA_CHANGE,0),intent.getFloatExtra(BatteryService.EXTRA_TEMPERATURE,0),intent.getLongExtra(BatteryService.EXTRA_CURRENT,0)));
            count++;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);
        helper=new BatteryManagerDBHelper(this);
        initView();
        initGraph();
        setHistory();
        setTitle("Temperature");
        registerReceiver(mReceiver,new IntentFilter(BatteryService.ACTION_NEW_ENTRY_ADDED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public void initView(){
        graph=findViewById(R.id.graphtemp);
        txt=findViewById(R.id.txt);
    }

    public void initGraph(){
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
            };
        });
        graph.getGridLabelRenderer().setNumHorizontalLabels(3);
        seriesBattery.setTitle("Battery Percentage");
        seriesTemp.setTitle("Temperature");
        seriesBattery.setColor(Color.GREEN);
        seriesTemp.setColor(Color.RED);
        graph.addSeries(seriesBattery);
        graph.getSecondScale().addSeries(seriesTemp);
        graph.getSecondScale().setMinY(0);
        graph.getSecondScale().setMaxY(50);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
    }

    public void setHistory() {
        arrayList = helper.readAll();
        count = arrayList.size();
        if (count != 0) {
            for (int i = 0; i < count; i++) {
                seriesBattery.appendData(new DataPoint(new Date(arrayList.get(i).getDate()), arrayList.get(i).getBattery()), false, count);
                seriesTemp.appendData(new DataPoint(new Date(arrayList.get(i).getDate()), arrayList.get(i).getTemp()), false, count);
            }
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(arrayList.get(arrayList.size() - 1).getDate() - getTimeDifferenceFromProgress(Integer.parseInt(helper.getDataFromSettings(SettingsConstants.WINDOW_SIZE))));
            graph.getViewport().setMaxX(arrayList.get(arrayList.size() - 1).getDate());
        }
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
}