package com.example.batterymanager.Activity.Navigation.rate_of_change;

import static com.example.batterymanager.SettingsConstants.getTimeDifferenceFromProgress;

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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.batterymanager.Activity.LogFragment;
import com.example.batterymanager.BatteryEntry;
import com.example.batterymanager.Provider.BatteryManagerDBHelper;
import com.example.batterymanager.Service.BatteryService;
import com.example.batterymanager.SettingsConstants;
import com.example.batterymanager.databinding.FragmentRateOfChangeBinding;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Date;

public class RateOfChangeFragment extends LogFragment {

    private FragmentRateOfChangeBinding binding;
    TextView txt;
    GraphView graph;
    Button btn;

    BatteryManagerDBHelper helper;
    int count;

    LineGraphSeries<DataPoint> seriesBattery=new LineGraphSeries<>();
    LineGraphSeries<DataPoint> seriesChange=new LineGraphSeries<>();
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
        RateOfChangeViewModel rateOfChangeViewModel =
                new ViewModelProvider(this).get(RateOfChangeViewModel.class);

        binding = FragmentRateOfChangeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        getContext().registerReceiver(mReceiver,new IntentFilter(BatteryService.ACTION_NEW_ENTRY_ADDED));

        txt=binding.rateOfChange;
        graph=binding.fragmentGraphRateOfChange;
        btn=binding.btn2;

        helper=new BatteryManagerDBHelper(getContext());

        setHistory();
        initGraph();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txt.setText("Rate of Change "+(Integer.parseInt(txt.getText().toString().substring(15))+1));
            }
        });

        //final TextView textView = binding.textDashboard;
        //rateOfChangeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getContext().unregisterReceiver(mReceiver);
        binding = null;
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
        graph.getGridLabelRenderer().setNumVerticalLabels(6);
        seriesBattery.setTitle("Battery Percentage");
        seriesChange.setTitle("Rate of Change");
        seriesBattery.setColor(Color.GREEN);
        seriesChange.setColor(Color.RED);

        graph.addSeries(seriesBattery);
        graph.getSecondScale().addSeries(seriesChange);
        graph.getSecondScale().setMinY(-30);
        graph.getSecondScale().setMaxY(70);
        graph.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(Color.RED);
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
                if (arrayList.get(i).getChange()<71) seriesChange.appendData(new DataPoint(new Date(arrayList.get(i).getDate()), arrayList.get(i).getChange()), false, count);
                else seriesChange.appendData(new DataPoint(new Date(arrayList.get(i).getDate()), 70), false, count);
            }
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMaxX(arrayList.get(count - 1).getDate());
            graph.getViewport().setMinX(arrayList.get(count - 1).getDate() - getTimeDifferenceFromProgress(Integer.parseInt(helper.getDataFromSettings(SettingsConstants.WINDOW_SIZE))));
        }
    }
}