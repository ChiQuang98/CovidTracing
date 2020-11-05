package com.ptitfinal.covidtracing.repository.SQLITE;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.ptitfinal.covidtracing.model.DeviceScan;

import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "CovidTracing";
    static final int VERSION = 1;
    public SQLiteHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table department
        String createDeviceScanSql =
                "CREATE TABLE DeviceScan(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "keyDevice TEXT," +
                        "dayInteraction TEXT)";
        db.execSQL(createDeviceScanSql);
    }
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }
    public long addDeviceScan(DeviceScan deviceScan) {
        ContentValues values = new ContentValues();
        values.put("keyDevice", deviceScan.getPersonBLEid());
        values.put("dayInteraction", deviceScan.getDayInteraction());
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        return sqLiteDatabase.insert("DeviceScan", null, values);
    }
    public List<DeviceScan> getAllDeviceScan(){
        List<DeviceScan> deviceScans = new ArrayList<>();
        Cursor cursor = getReadableDatabase()
                .query("DeviceScan", null, null,
                        null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String keyDevice = cursor.getString(cursor.getColumnIndex("keyDevice"));
                String date = cursor.getString(cursor.getColumnIndex("dayInteraction"));
                DeviceScan deviceScan = new DeviceScan(keyDevice, date);
                deviceScans.add(deviceScan);
            }
            cursor.close();
        }
        return deviceScans;
    }
    public List<DeviceScan> getDeviceScanByDay(String date){
        String whereClause = "dayInteraction = ?";
        String[] whereArgs = {date };
        List<DeviceScan> deviceScans = new ArrayList<>();
        Cursor cursor = getReadableDatabase()
                .query("DeviceScan", null, whereClause,
                        whereArgs, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String keyDevice = cursor.getString(cursor.getColumnIndex("keyDevice"));
                String dateDevice = cursor.getString(cursor.getColumnIndex("dayInteraction"));
                DeviceScan deviceScan = new DeviceScan(keyDevice, dateDevice);
                deviceScans.add(deviceScan);
            }
            cursor.close();
        }
        return deviceScans;
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
