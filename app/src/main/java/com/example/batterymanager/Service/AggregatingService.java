package com.example.batterymanager.Service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.example.batterymanager.BatteryEntry;
import com.example.batterymanager.HistoryEntry;
import com.example.batterymanager.Provider.BatteryManagerDBHelper;
import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.zip.DataFormatException;

public class AggregatingService extends Service {
    private static final String TAG="Aggregating Service";
    BatteryManagerDBHelper helper;
    public AggregatingService() {}

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        helper=new BatteryManagerDBHelper(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        aggregateCheck();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    void aggregate(){
        ArrayList<BatteryEntry> arrayList= helper.readAll();
        int previousBattery=arrayList.get(0).getBattery();
        long initialDate=arrayList.get(0).getDate();
        int initialBattery=arrayList.get(0).getBattery();
        boolean charging=false;
        for (int i=1;i<arrayList.size();i++){
            if(previousBattery+2==arrayList.get(i).getBattery()){
                charging=true;
                String status="Used for ";
                long diff=(arrayList.get(i).getDate()-initialDate)/(1000*60);
                if(diff<60) status=status+diff+"m";
                else status=status+(int)(diff/60)+"h "+(diff%60)+"m";
                String changeInterval=(initialBattery)+"%-"+(arrayList.get(i).getBattery())+"%";
                String screenWattage=getScreenTime(initialDate,arrayList.get(i).getDate());
                String time=new Date(arrayList.get(i).getDate()).toString().substring(4,16);
                String change=(Math.abs(initialBattery-arrayList.get(i).getBattery()))+"%";
                helper.addHistoryEntry(new HistoryEntry(status,changeInterval,screenWattage,time,change));
                initialBattery=arrayList.get(i).getBattery();
                initialDate=arrayList.get(i).getDate();
            }
            else if(previousBattery-2==arrayList.get(i).getBattery()){
                charging=false;
                String status="Charged for ";
                long diff=(arrayList.get(i).getDate()-initialDate)/(1000*60);
                if(diff<60) status=status+diff+"m";
                else status=status+(int)(diff/60)+"h "+(diff%60)+"m";
                String changeInterval=(initialBattery)+"%-"+(arrayList.get(i).getBattery())+"%";
                String screenWattage=getScreenTime(initialDate,arrayList.get(i).getDate());
                String time=new Date(arrayList.get(i).getDate()).toString().substring(4,16);
                String change=(Math.abs(initialBattery-arrayList.get(i).getBattery()))+"%";
                helper.addHistoryEntry(new HistoryEntry(status,changeInterval,screenWattage,time,change));
                initialBattery=arrayList.get(i).getBattery();
                initialDate=arrayList.get(i).getDate();
            }
            previousBattery=arrayList.get(i).getBattery();
        }
        this.onDestroy();
    }

    void aggregateCheck(){
        ArrayList<BatteryEntry> arrayList=helper.readAll();
        int ipos=0;
        for (int i=0;i<arrayList.size();i++){
            if (arrayList.get(i).getDate()==1655235309693L) ipos=i;
        }
        int previousBattery=arrayList.get(ipos).getBattery();
        long initialDate=arrayList.get(ipos).getDate();
        for (int i=ipos;i<arrayList.size()-1;i++){
            if (arrayList.get(i+1).getBattery()+1<arrayList.get(i).getBattery()){
                Log.w(TAG,"From "+previousBattery+" "+arrayList.get(i).getBattery()+" "+arrayList.get(i+1).getBattery()+" "+new Date(initialDate) +" "+new Date(arrayList.get(i).getDate()));
                Log.w(TAG,getScreenTime(initialDate,arrayList.get(i).getDate()));
                previousBattery=arrayList.get(i+1).getBattery();
                initialDate=arrayList.get(i+1).getDate();
            }
            if (arrayList.get(i+1).getBattery()-1>arrayList.get(i).getBattery()){
                Log.w(TAG,"From "+previousBattery+" "+arrayList.get(i).getBattery()+" "+arrayList.get(i+1).getBattery()+" "+new Date(initialDate) +" "+new Date(arrayList.get(i).getDate()));
                Log.w(TAG,getScreenTime(initialDate,arrayList.get(i).getDate()));
                previousBattery=arrayList.get(i+1).getBattery();
                initialDate=arrayList.get(i+1).getDate();
            }
        }
        stopSelf();
    }

    String getScreenTime(long initialTime,long finalTime){
        LinkedHashMap<Long,Boolean> hashMap= helper.readAllScreenStatus();
        Iterator<Long> iterator=hashMap.keySet().iterator();
        ArrayList<Long> arrayListDate=new ArrayList<>();
        ArrayList<Boolean> arrayListBoolean=new ArrayList<>();
        while(iterator.hasNext()){
            long date=iterator.next();
            arrayListDate.add(date);
            arrayListBoolean.add(hashMap.get(date));
        }
        int ipos=arrayListDate.indexOf(initialTime);
        int fpos=arrayListDate.indexOf(finalTime);
        float time=0;
        if (ipos==-1 || fpos==-1) {
            Log.w(TAG,ipos+" "+initialTime+"\n"+finalTime+" "+fpos);
            return "";
        }
        for (int i=ipos;i<fpos-1;i++){
            if(arrayListBoolean.get(i)&&arrayListBoolean.get(i+1)){
                time+=((arrayListDate.get(ipos+1)-arrayListDate.get(ipos))/(1000.0*60));
            }
        }
        if (time<60) return "Screen on time: "+time+"m";
        else return "Screen on time: "+(int)(time/60)+"h "+(int)time%60+"m";
    }
}