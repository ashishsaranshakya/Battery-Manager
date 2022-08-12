package com.ashish.batterymanager.Activity.Navigation.rate_of_change;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RateOfChangeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public RateOfChangeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}