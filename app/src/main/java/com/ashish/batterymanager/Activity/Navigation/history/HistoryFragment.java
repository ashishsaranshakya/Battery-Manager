package com.ashish.batterymanager.Activity.Navigation.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ashish.batterymanager.Activity.LogFragment;
import com.ashish.batterymanager.HistoryEntry;
import com.ashish.batterymanager.HistoryRecyclerViewAdapter;
import com.ashish.batterymanager.Provider.BatteryManagerDBHelper;
import com.ashish.batterymanager.databinding.FragmentHistoryBinding;

import java.util.ArrayList;

public class HistoryFragment extends LogFragment {

    private FragmentHistoryBinding binding;
    RecyclerView recyclerView;
    ArrayList<HistoryEntry> arrayList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HistoryViewModel historyViewModel =
                new ViewModelProvider(this).get(HistoryViewModel.class);

        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initView();
        setData();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    void initView() {
        recyclerView=binding.historyView;

    }

    void setData(){
        BatteryManagerDBHelper helper=new BatteryManagerDBHelper(getContext());
        arrayList=helper.readHistory();

        HistoryRecyclerViewAdapter adapter=new HistoryRecyclerViewAdapter(getContext());
        adapter.setArrayList(arrayList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

}
