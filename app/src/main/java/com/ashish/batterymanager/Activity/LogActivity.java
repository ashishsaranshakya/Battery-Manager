package com.ashish.batterymanager.Activity;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LogActivity extends AppCompatActivity {
    final String TAG=this.getClass().toString();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(TAG,"onCreate function called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w(TAG,"onDestroy function called");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.w(TAG,"onRestart function called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(TAG,"onResume function called");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.w(TAG,"onStart function called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w(TAG,"onStop function called");
    }

    @Override
    protected void onPause() {
        Log.w(TAG,"onPause function called");
        super.onPause();
    }
}
