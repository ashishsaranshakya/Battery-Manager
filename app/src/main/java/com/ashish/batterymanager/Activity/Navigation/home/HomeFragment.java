package com.ashish.batterymanager.Activity.Navigation.home;

import static com.ashish.batterymanager.SettingsConstants.getRadioGroupFromTableSetting;
import static com.ashish.batterymanager.SettingsConstants.getTimeDifferenceFromProgress;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.ashish.batterymanager.Activity.LogFragment;
import com.ashish.batterymanager.BatteryEntry;
import com.ashish.batterymanager.Provider.BatteryManagerDBHelper;
import com.ashish.batterymanager.Service.BatteryService;
import com.ashish.batterymanager.SettingsConstants;
import com.ashish.batterymanager.databinding.FragmentHomeBinding;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class HomeFragment extends LogFragment {

    private FragmentHomeBinding binding;
    TextView txt;
    GraphView graph;
    Button btn;

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
            seriesBattery.appendData(new DataPoint(intent.getLongExtra(BatteryService.EXTRA_DATE,0),intent.getIntExtra(BatteryService.EXTRA_BATTERY,0)),false,count+1);
            graph.getViewport().setMaxX(intent.getLongExtra(BatteryService.EXTRA_DATE,-1));
            arrayList.add(new BatteryEntry(intent.getLongExtra(BatteryService.EXTRA_DATE,0),intent.getIntExtra(BatteryService.EXTRA_BATTERY,0),intent.getLongExtra(BatteryService.EXTRA_CHANGE,0),intent.getFloatExtra(BatteryService.EXTRA_TEMPERATURE,0),intent.getLongExtra(BatteryService.EXTRA_CURRENT,0)));
            count++;
        }
    };


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        getContext().registerReceiver(mReceiver,new IntentFilter(BatteryService.ACTION_NEW_ENTRY_ADDED));

        txt=binding.home;
        graph=binding.fragmentGraph;
        btn=binding.btn1;
        helper=new BatteryManagerDBHelper(getContext());

        setHistory();
        initGraph();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getContext().unregisterReceiver(mReceiver);
        binding = null;
    }

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
            };
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
}