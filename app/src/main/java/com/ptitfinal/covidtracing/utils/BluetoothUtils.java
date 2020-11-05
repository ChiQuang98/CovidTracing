package com.ptitfinal.covidtracing.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class BluetoothUtils {
    public static final int REQUEST_ENABLE_BT = 129;
    public static boolean isBleSupported(Context context){
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }
    public static void requestEnableBluetooth(Activity activity){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent,REQUEST_ENABLE_BT);
    }
}
