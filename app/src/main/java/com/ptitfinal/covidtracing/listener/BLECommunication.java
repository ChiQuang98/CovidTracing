package com.ptitfinal.covidtracing.listener;

public interface BLECommunication {
    void onBleSupported();
    void onBleNotSupported();
    void onBluetoothEnbled(boolean enabled,String message);
}
