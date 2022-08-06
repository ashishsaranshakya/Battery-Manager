package com.example.batterymanager.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.batterymanager.HistoryEntry;
import com.example.batterymanager.HistoryRecyclerViewAdapter;
import com.example.batterymanager.Provider.BatteryManagerDBHelper;
import com.example.batterymanager.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    MaterialCardView cardView;
    RecyclerView recyclerView;
    ArrayList<HistoryEntry> arrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initView();
        setData();

        HistoryRecyclerViewAdapter adapter=new HistoryRecyclerViewAdapter(this);
        adapter.setArrayList(arrayList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    void initView() {
        cardView=findViewById(R.id.cardView);
        recyclerView=findViewById(R.id.historyRecyclerView);
    }

    void setData(){
        BatteryManagerDBHelper helper=new BatteryManagerDBHelper(this);
        arrayList=helper.readHistory();
        /*arrayList=new ArrayList<>();
        arrayList.add(new HistoryEntry("Changing 1","40%-80%","9 watts","4:56pm","40%"));
        arrayList.add(new HistoryEntry("Changing 2","40%-10%","18 watts","4:36pm","30%"));
        arrayList.add(new HistoryEntry("Changing 3","10%-80%","13 watts","4:16pm","50%"));
        arrayList.add(new HistoryEntry("Changing 4","40%-80%","18 watts","1:56pm","34%"));
        arrayList.add(new HistoryEntry("Changing 5","80%-10%","4 watts","4:26pm","24%"));
        arrayList.add(new HistoryEntry("Changing 6","10%-80%","12 watts","3:56pm","47%"));
        arrayList.add(new HistoryEntry("Changing 7","80%-60%","4 watts","4:26pm","24%"));
        arrayList.add(new HistoryEntry("Changing 8","60%-80%","14 watts","3:52pm","47%"));
        arrayList.add(new HistoryEntry("Changing 9","80%-10%","4 watts","4:26pm","24%"));
        arrayList.add(new HistoryEntry("Changing 10","10%-80%","12 watts","3:56pm","47%"));*/
    }
}