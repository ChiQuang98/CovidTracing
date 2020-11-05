package com.ptitfinal.covidtracing.manager;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

import com.ptitfinal.covidtracing.listener.BLECommunication;
import com.ptitfinal.covidtracing.utils.BluetoothUtils;

public class BleManager {
    private BLECommunication listener;
    private Context mContext;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    public BleManager(Context context,BLECommunication listener){
        this.listener = listener;
        this.mContext = context;
        validateBleFeature(mContext);
    }
    private void validateBleFeature(Context context){
        if(BluetoothUtils.isBleSupported(context)){
            listener.onBleSupported();
            init();
        }
        else{
            listener.onBleNotSupported();
        }
    }
    private void init(){
        bluetoothManager = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }
    public void enabledBluetooth(Activity activity){
        if(bluetoothAdapter!=null&&bluetoothAdapter.isEnabled()){
            //send Intent to request enable bluetooth
            BluetoothUtils.requestEnableBluetooth(activity);
        }else{
            listener.onBluetoothEnbled(true,"bluetooth enabled");
        }
    }
    public void onRequestPermissionBluetooth(int requestCode, int resultCode, Intent data){
        if(resultCode== Activity.RESULT_OK && requestCode == BluetoothUtils.REQUEST_ENABLE_BT){
            listener.onBluetoothEnbled(true,"bluetooth enabled");
        }
        else {
            listener.onBluetoothEnbled(false,"bluetooth doesn't enabled");
        }
    }
}
