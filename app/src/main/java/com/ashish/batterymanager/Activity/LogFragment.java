package com.ashish.batterymanager.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LogFragment extends Fragment {
    public final String TAG=this.getClass().toString();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.w(TAG,"onCreate");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.w(TAG,"onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.w(TAG,"onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        Log.w(TAG,"onViewStateRestored");
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onStart() {
        Log.w(TAG,"onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.w(TAG,"onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.w(TAG,"onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.w(TAG,"onStop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.w(TAG,"onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.w(TAG,"onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.w(TAG,"onDetach");
        super.onDetach();
    }
}
