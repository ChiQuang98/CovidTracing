package com.ptitfinal.covidtracing.model;

import android.content.Context;
import android.util.Log;

import com.ptitfinal.covidtracing.repository.SQLITE.DeviceDAO;

import java.util.List;

public class User {
    String IDUser;
    String phone;
    String key;
    String isExpose;

//    public User(String key,String phone) {
//        this.phone = phone;
//        this.key = key;
//    }

    public User(String key,String phone,  String isExpose) {
        this.phone = phone;
        this.key = key;
        this.isExpose = isExpose;
    }
    public static boolean checkIsExposed(Context context,String keyAppExposedNew){
        List<DeviceScan> deviceScanList = new DeviceDAO().getAllDeviceScan(context);
//        Log.e("User","Size User: "+deviceScanList.size());

        if(deviceScanList.size()>0&&deviceScanList!=null){
                    Log.e("User"," User: "+deviceScanList.get(0).getPersonBLEid());
            for(DeviceScan obj:deviceScanList){
                if(keyAppExposedNew.toLowerCase().equalsIgnoreCase(obj.getPersonBLEid().toLowerCase())){
                    //nếu tìm thấy key được gửi về trong list key đã lưu tức là mình là F1
                    return true;
                }
            }
        }
        //return true means exposed
        return false;
    }
    public String isExpose() {
        return isExpose;
    }

    public void setExpose(String expose) {
        isExpose = expose;
    }

    public String getIDUser() {
        return IDUser;
    }

    public void setIDUser(String IDUser) {
        this.IDUser = IDUser;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
