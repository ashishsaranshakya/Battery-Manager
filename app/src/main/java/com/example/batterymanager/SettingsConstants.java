package com.example.batterymanager;

import android.util.Log;

import java.util.LinkedList;

public class SettingsConstants {
    private static final String TAG="Settings Constants";
    public static final String HISTORY_PERIOD="history";
    public static final String FINAL_PERCENT="percent";
    public static final String ACTUAL_BATTERY="actual_battery";
    public static final String NO_OF_ENTRIES="entries";
    public static final String FULL_CAPACITY="max_capacity";
    public static final String PREDICTION_TIME="prediction_time";
    public static final String SAMPLING_RATE_FOR_SCREEN_STATUS="screen_status_sampling_status";
    public static final String TIME_OR_PERCENT="time_or_percent";
    public static final String WINDOW_SIZE="window_size";
    public static final String MAIN_WINDOW_PRESET="main_window_preset";
    public static final String LAST_AGGREGATE_TIME="last_aggregate_time";
    public static final int TOTAL_SETTINGS=11;


    public static int getMonthsFromProgress(int progress){
        switch (progress){
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 4;
            case 3:
                return 6;
            case 4:
                return 8;
            case 5:
                return 10;
            case 6:
                return 12;
            default:
                Log.w(TAG,"Corrupted data");
                return 0;
        }
    }

    public static long getHistoryTime(int progress){
        long month=2721600000L;
        switch (progress){
            case 0:
                return month;
            case 1:
                return month*2;
            case 2:
                return month*4;
            case 3:
                return month*6;
            case 4:
                return month*8;
            case 5:
                return month*10;
            case 6:
                return month*12;
            default:
            Log.w(TAG,"Corrupted data");
                return 0;
        }
    }

    public static long getTimeDifferenceFromProgress(int progress){
        switch (progress){
            case 0:
                return (1000*60*60);
            case 1:
                return (1000*60*60*3);
            case 2:
                return (1000*60*60*6);
            case 3:
                return (1000*60*60*24);
            case 4:
                return (1000*60*60*24*7);
            case 5:
                return (100L *60*60*24*305);
            default:
                Log.w(TAG,"Corrupt data");
                return 0;
        }
    }

    public static String getTimeFromTableSetting(int progress){
        switch (progress){
            case 0:
                return "1 hour";
            case 1:
                return "3 hours";
            case 2:
                return "6 hours";
            case 3:
                return "1 day";
            case 4:
                return "1 week";
            case 5:
                return "1 month";
            default:
                Log.w(TAG,"corrupt data");
                return "";
        }
    }

    public static String getPresetFromRadioGroup(boolean battery,boolean rateOfChange,boolean temperature,boolean screenStatus){
        String result="";
        if (battery) result=result+"1";
        else result=result+"0";
        if (rateOfChange) result=result+"1";
        else result=result+"0";
        if (temperature) result=result+"1";
        else result=result+"0";
        if (screenStatus) result=result+"1";
        else result=result+"0";
        return result;
    }

    public static LinkedList<Boolean> getRadioGroupFromTableSetting(String data){
        LinkedList<Boolean> result=new LinkedList<>();
        if (data.equals("0")) {
            result.add(false);result.add(false);result.add(false);result.add(false);
            return result;
        }
        else if (data.equals("1")) {
            result.add(false);result.add(false);result.add(false);result.add(true);
            return result;
        }
        else if (data.equals("10")){
            result.add(false);result.add(false);result.add(true);result.add(false);
            return result;
        }
        else if (data.equals("11")){
            result.add(false);result.add(false);result.add(true);result.add(true);
            return result;
        }
        else if (data.equals("100")){
            result.add(false);result.add(true);result.add(false);result.add(false);
            return result;
        }
        else if (data.equals("110")){
            result.add(false);result.add(true);result.add(true);result.add(false);
            return result;
        }
        else if (data.equals("101")){
            result.add(false);result.add(true);result.add(false);result.add(true);
            return result;
        }
        else if (data.equals("111")){
            result.add(false);result.add(true);result.add(true);result.add(true);
            return result;
        }
        if (data.charAt(0)=='1') result.add(true);
        else result.add(false);
        if (data.charAt(1)=='1') result.add(true);
        else result.add(false);
        if (data.charAt(2)=='1') result.add(true);
        else result.add(false);
        if (data.charAt(3)=='1') result.add(true);
        else result.add(false);
        return result;
    }
}
