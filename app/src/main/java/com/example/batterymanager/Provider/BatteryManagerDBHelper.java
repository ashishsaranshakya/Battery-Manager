package com.example.batterymanager.Provider;

import static com.example.batterymanager.SettingsConstants.getHistoryTime;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.batterymanager.BatteryEntry;
import com.example.batterymanager.HistoryEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class BatteryManagerDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME="Battery_Manager";
    private static final int DATABASE_VERSION=1;
    public static final String KEY_DATE="date";
    public static final String KEY_BATTERY="battery";
    public static final String KEY_RATE_OF_CHANGE="rate_of_change";
    public static final String KEY_TEMPERATURE="temperature";
    public static final String KEY_CURRENT="current";
    public static final String KEY_SETTING="setting";
    public static final String KEY_SETTING_DATA="data";
    public static final String KEY_SCREENSTATUS_DATE="date";
    public static final String KEY_SCREENSTATUS_STATUS="status";
    public static final String KEY_HISTORY_STATUS="status";
    public static final String KEY_HISTORY_CHANGE_INTERVAL="change_interval";
    public static final String KEY_HISTORY_SCREEN_OR_WATTAGE="screen_or_wattage";
    public static final String KEY_HISTORY_TIME="time";
    public static final String KEY_HISTORY_CHANGE="change";

    private static final String TAG="BatteryManagerDBHelper";
    public static final String TABLE_MAIN ="BatteryData";
    public static final String TABLE_SETTINGS="Settings";
    public static final String TABLE_SCREENSTATUS ="ScreenStatus";
    public static final String TABLE_HISTORY="History";

    public BatteryManagerDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String DATABASE_CREATE_BATTERYDATA ="CREATE TABLE if not exists "+ TABLE_MAIN +
            "("+KEY_DATE+" varchar(128) PRIMARY KEY," +
            KEY_BATTERY+" varchar(128) NOT NULL,"+
            KEY_RATE_OF_CHANGE+" varchar(128) NOT NULL,"+
            KEY_TEMPERATURE+" varchar(128) NOT NULL,"+
            KEY_CURRENT+" varchar(128) NOT NULL);";

    private static final String DATABASE_CREATE_SETTINGS="CREATE TABLE if not exists "+TABLE_SETTINGS+
            '('+KEY_SETTING+" varchar(128) PRIMARY KEY,"+
            KEY_SETTING_DATA+" varchar(128) NOT NULL);";

    private static final String DATABASE_CREATE_SCREENSTATUS="CREATE TABLE if not exists "+ TABLE_SCREENSTATUS +
            '('+KEY_SCREENSTATUS_DATE+" varchar(128) PRIMARY KEY,"+
            KEY_SCREENSTATUS_STATUS+" varchar(1) NOT NULL);";

    private static final String DATABASE_CREATE_HISTORY ="CREATE TABLE if not exists "+ TABLE_HISTORY+
            "("+KEY_HISTORY_STATUS+" varchar(128) NOT NULL," +
            KEY_HISTORY_CHANGE_INTERVAL+" varchar(128) NOT NULL,"+
            KEY_HISTORY_SCREEN_OR_WATTAGE+" varchar(128) NOT NULL,"+
            KEY_HISTORY_TIME+" varchar(128) NOT NULL,"+
            KEY_HISTORY_CHANGE+" varchar(128) NOT NULL);";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_BATTERYDATA);
        db.execSQL(DATABASE_CREATE_SETTINGS);
        db.execSQL(DATABASE_CREATE_SCREENSTATUS);
        db.execSQL(DATABASE_CREATE_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG,"Upgrading database from version "+oldVersion+" to "+newVersion+",which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_MAIN);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_SCREENSTATUS);
        onCreate(db);
    }

    public void addBatteryEntry(long date, int bat, long change, float temp, long current){
        SQLiteDatabase db =this.getWritableDatabase();
        ContentValues values=new ContentValues();

        values.put(KEY_DATE,String.valueOf(date));
        values.put(KEY_BATTERY,String.valueOf(bat));
        values.put(KEY_RATE_OF_CHANGE,String.valueOf(change));
        values.put(KEY_TEMPERATURE,String.valueOf(temp));
        values.put(KEY_CURRENT,String.valueOf(current));

        db.insert(TABLE_MAIN,null,values);
        db.close();
    }

    public void addSetting(String setting,String data){
        SQLiteDatabase db =this.getWritableDatabase();
        ContentValues values=new ContentValues();

        values.put(KEY_SETTING,setting);
        values.put(KEY_SETTING_DATA,data);

        db.insert(TABLE_SETTINGS,null,values);
        db.close();
    }

    /**
     * Adds entry of screen status to database
     * @param date toString of date object
     * @param status boolean for screen on or off
     */
    public void addScreenStatus(long date,boolean status){
        SQLiteDatabase db =this.getWritableDatabase();
        ContentValues values=new ContentValues();
        int ch;
        if(status) ch=1;
        else ch=0;

        values.put(KEY_SCREENSTATUS_DATE,date);
        values.put(KEY_SCREENSTATUS_STATUS,ch);

        db.insert(TABLE_SCREENSTATUS,null,values);
        db.close();
    }

    public void addHistoryEntry(HistoryEntry entry){//String status,String change_interval,String screen_or_wattage,String time,String change){
        SQLiteDatabase db =this.getWritableDatabase();
        ContentValues values=new ContentValues();

        values.put(KEY_HISTORY_STATUS,entry.getStatus());
        values.put(KEY_HISTORY_SCREEN_OR_WATTAGE,entry.getScreenOn_avgWattage());
        values.put(KEY_HISTORY_CHANGE,entry.getChange());
        values.put(KEY_HISTORY_TIME,entry.getTime());
        values.put(KEY_HISTORY_CHANGE_INTERVAL,entry.getChangeInterval());

        db.insert(TABLE_HISTORY,null,values);
        db.close();
    }

    public ArrayList<BatteryEntry> readAll(){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+ TABLE_MAIN,new String[]{});
        ArrayList<BatteryEntry> arrayList=new ArrayList<>();
        if(c.moveToFirst()){
            do{
                arrayList.add(new BatteryEntry(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4)));
            }while (c.moveToNext());
        }
        c.close();
        db.close();
        return arrayList;
    }

    public HashMap<String,String> readAllSetting(){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+ TABLE_SETTINGS,new String[]{});
        HashMap<String,String> hashMap=new HashMap<>();
        if(c.moveToFirst()){
            do{
                hashMap.put(c.getString(0), c.getString(1));
            }while (c.moveToNext());
        }
        c.close();
        db.close();
        return hashMap;
    }

    public LinkedHashMap<Long,Boolean> readAllScreenStatus(){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+ TABLE_SCREENSTATUS,new String[]{});
        LinkedHashMap<Long,Boolean> hashMap=new LinkedHashMap<>();
        if(c.moveToFirst()){
            do{
                Boolean ch;
                if (c.getString(1).equals("1")) ch = true;
                else ch = false;
                hashMap.put(Long.parseLong(c.getString(0)), ch);
            }while (c.moveToNext());
        }
        c.close();
        db.close();
        return hashMap;
    }

    public ArrayList<HistoryEntry> readHistory(){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+ TABLE_HISTORY,new String[]{});
        ArrayList<HistoryEntry> arrayList=new ArrayList<>();
        if(c.moveToFirst()){
            do{
                arrayList.add(new HistoryEntry(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4)));
            }while (c.moveToNext());
        }
        c.close();
        db.close();
        return arrayList;
    }

    public Cursor queryReadable(String sql){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor c=db.rawQuery(sql,new String[]{});
        return c;
    }

    public boolean modifySetting(String col,String data){
        boolean check=false;
        try{
        SQLiteDatabase db =this.getWritableDatabase();
        ContentValues values=new ContentValues();

        values.put(KEY_SETTING_DATA,data);

        db.update(TABLE_SETTINGS,values,KEY_SETTING+"="+col,new String[]{});
        db.close();
        check=true;
        }catch (Exception e){
            Log.w(TAG,e.toString());
        }
        return check;
    }

    public void queryWriteable(String sql){
        SQLiteDatabase db=getWritableDatabase();
        db.execSQL(sql);
        db.close();
    }

    public boolean changeHistory(int months){
        boolean check=false;
        try{
            SQLiteDatabase db =this.getWritableDatabase();

            int rows=db.delete(TABLE_MAIN,KEY_DATE+"<"+ (Calendar.getInstance().getTime().getTime()-getHistoryTime(months)),new String[]{});
            Log.w(TAG,"Rows changed:"+rows);
            db.close();
            check=true;
        }catch (Exception e){
            Log.w(TAG,e.toString());
        }
        return check;
    }

    public String getDataFromSettings(String setting){
        Cursor c=queryReadable("SELECT * FROM "+ TABLE_SETTINGS);
        if(c.moveToFirst()){
            do{
                if (c.getString(0).equals(setting)){
                    String s=c.getString(1);
                    c.close();
                    return s;
                }
            }while (c.moveToNext());
        }
        c.close();
        return "";
    }

    public boolean deleteSetting(String col){
        boolean check=false;
        try{
            SQLiteDatabase db =this.getWritableDatabase();
            db.update(TABLE_SETTINGS,new ContentValues(),KEY_SETTING+"="+col,new String[]{});
            db.close();
            check=true;
        }catch (Exception e){
            Log.w(TAG,e.toString());
        }
        return check;
    }

    /**
     * Deletes table and recreates it to expunge all data the table
     * @param table table name to delete data from
     */
    public void refreshTable(String table){
        SQLiteDatabase db =this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS "+ table);
        onCreate(db);
        db.close();
    }
}
