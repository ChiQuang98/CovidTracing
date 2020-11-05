package com.ptitfinal.covidtracing.repository.SQLITE;

import android.content.Context;

import com.ptitfinal.covidtracing.model.DeviceScan;

import java.util.List;

public class DeviceDAO {
    private Context mContext;
    private static DeviceDAO instance;
    public static DeviceDAO getInstance(){
        if(instance==null){
            instance = new DeviceDAO();
        }
        return instance;
    }
    public List<DeviceScan> getAllDeviceScan(Context context){
        SQLiteHelper sqLiteHelper = new SQLiteHelper(context);
         return  sqLiteHelper.getAllDeviceScan();
    }
    public List<DeviceScan> getDeviceScanByDay(Context context,String date){
        SQLiteHelper sqLiteHelper = new SQLiteHelper(context);
        return sqLiteHelper.getDeviceScanByDay(date);
    }
    public void addDeviceToDB(Context context,DeviceScan deviceScan){
        SQLiteHelper sqLiteHelper = new SQLiteHelper(context);
        sqLiteHelper.addDeviceScan(deviceScan);
    }
}
