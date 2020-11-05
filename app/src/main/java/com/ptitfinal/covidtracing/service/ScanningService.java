package com.ptitfinal.covidtracing.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.ptitfinal.covidtracing.MainActivity;
import com.ptitfinal.covidtracing.R;
import com.ptitfinal.covidtracing.model.Constants;
import com.ptitfinal.covidtracing.model.DeviceScan;
import com.ptitfinal.covidtracing.repository.SQLITE.DeviceDAO;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static androidx.core.app.ActivityCompat.startActivityForResult;

public class ScanningService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private static final String TAG = ScanningService.class.getSimpleName();
    private static final String DEVICE_NAME = "CVTracing";
    private static final int FOREGROUND_NOTIFICATION_ID =13 ;
    BluetoothLeScanner mBluetoothLeScanner;
    List<ScanFilter> filters;
    List<DeviceScan> listDeviceScaned;
    List<DeviceScan> listDeviceDB;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
    Date dateobj = new Date();
    String today = dateFormat.format(dateobj);
    BluetoothAdapter btAdapter;
    BluetoothManager btManager;
    public static final String SCANNING_FAILED =
            "com.example.android.bluetoothscaning.scanning_failed";
    private Handler mHandler = new Handler();
    Timer timer;
    TimerTask myTask;
    public class LocalBinder extends Binder {
        public ScanningService getScanningService() {
            return ScanningService.this;
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        listDeviceScaned = new ArrayList<>();
        listDeviceDB = new ArrayList<>();
        filters = new ArrayList<>();
        if(listDeviceDB.size()>0){
            listDeviceDB.clear();

        }
        listDeviceDB = DeviceDAO.getInstance().getDeviceScanByDay(getApplicationContext(),today);
        initBLEComponent();
        initbtAdapter();

         turnOnScheduleScan();

    }

    public void turnOnScheduleScan() {
        Log.e(TAG,"In Turn on");
        timer = new Timer();
        myTask = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                // whatever you need to do every 2 seconds
                listDeviceDB = DeviceDAO.getInstance().getDeviceScanByDay(getApplicationContext(),today);
                discover();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothLeScanner.stopScan(mScanCallback);
                        Log.e(TAG,"Stop Scanning: "+listDeviceDB.size());
                    }
                }, 10000);//Thời gian muốn scan
            }
        };
        timer.schedule(myTask, 0,12000);//12000-10000 = 2s means sau 2s bắt đầu scan lại
    }
    public void turnOffScheduleScan(){
        Log.e(TAG,"In Turn off");
        mBluetoothLeScanner.stopScan(mScanCallback);
        timer.cancel();
    }

    public static final int REQUEST_ENABLE_BLE = 1;
    void initbtAdapter(){

        Log.e("GG","IN");
        btAdapter = btManager.getAdapter();
        mBluetoothLeScanner = btAdapter.getBluetoothLeScanner();
        btAdapter.setName(DEVICE_NAME);//Ten App - device
        if (!BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
//            Toast.makeText(ge, "Multiple advertisement not supported", Toast.LENGTH_SHORT).show();
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void goForeground() {
        boolean api;
        String channel;
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Notification n;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            channel = createChannel();
            n = new Notification.Builder(this,channel)
                    .setContentTitle("Scaning device via Bluetooth")
                    .setContentText("This device is Scanning to others nearby.")
                    .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                    .setContentIntent(pendingIntent)
                    .build();
        }
        else {
            channel = "";
            n = new Notification.Builder(this)
                    .setContentTitle("Scaning device via Bluetooth")
                    .setContentText("This device is Scanning to others nearby.")
                    .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                    .setContentIntent(pendingIntent)
                    .build();
        }

        startForeground(FOREGROUND_NOTIFICATION_ID, n);
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
//    @TargetApi(26)
    private synchronized String createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

            String name = "snap map fake location ";
            int importance = NotificationManager.IMPORTANCE_LOW;

            NotificationChannel mChannel = new NotificationChannel("snap map channel", name, importance);

            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
            } else {
                stopSelf();
            }
            return "snap map channel";
        }
        return "snap map channel";
    }


    private void initBLEComponent() {
        btManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
//        btAdapter = btManager.getAdapter();
//        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btAdapter = btManager.getAdapter();
//        enableBt();

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void discover() {
        goForeground();
        Log.e(TAG,"in Scanning");
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(UUID.fromString(String.valueOf(Constants.Service_UUID))))
                .setDeviceName(DEVICE_NAME)
                .build();
        filters.add(filter);
        ScanSettings settings = new ScanSettings.Builder()
                .setReportDelay(1000)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build();

        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);


    }
    private ScanCallback mScanCallback = new ScanCallback() {


        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result == null
                    || result.getDevice() == null
                    || TextUtils.isEmpty(result.getDevice().getName()))
                return;
            ParcelUuid pUuid = new ParcelUuid(UUID.fromString(String.valueOf(Constants.Service_UUID)));
//            StringBuilder builder = new StringBuilder(result.getDevice().getName());
            StringBuilder builder = new StringBuilder(new String(result.getScanRecord().getServiceData(result.getScanRecord().getServiceUuids().get(0)), Charset.forName("UTF-8")));
//            if(result.getScanRecord()!=null)
//                if(result.getScanRecord().getServiceData()!=null)
//                {
//                    String temp = new String(result.getScanRecord().getServiceData().get(pUuid));
//                    builder.append("\n").append(temp);
//                }
//            builder.append("\n").append(new String(result.getScanRecord().getServiceData(result.getScanRecord().getServiceUuids().get(0)), Charset.forName("UTF-8")));
//            builder.append("\n").append(new String(result.getScanRecord().getServiceData(pUuid), Charset.forName("UTF-8")));

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
            Date dateobj = new Date();
            DeviceScan newScanDevice = new DeviceScan(builder.toString(), dateFormat.format(dateobj));
//            if (DeviceScan.checkExist(listDeviceScaned, newScanDevice) == false) {
//                listDeviceScaned.add(newScanDevice);
//            }
            if (DeviceScan.checkExist(listDeviceDB, newScanDevice) == false) {
                Log.e(TAG, "add to DB");
                DeviceDAO.getInstance().addDeviceToDB(getApplicationContext(), newScanDevice);
            }
            Log.e(TAG, "In single");
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.e(TAG,"In Batch");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
            Date dateobj = new Date();
            for (ScanResult result : results) {
                StringBuilder builder = new StringBuilder(new String(result.getScanRecord().getServiceData(result.getScanRecord().getServiceUuids().get(0)), Charset.forName("UTF-8")));
                DeviceScan newScanDevice = new DeviceScan(builder.toString(),dateFormat.format(dateobj));
//                if(DeviceScan.checkExist(listDeviceScaned,newScanDevice)==false){
//                    listDeviceScaned.add(newScanDevice);
//                }
                if(DeviceScan.checkExist(listDeviceDB,newScanDevice)==false){
                    DeviceDAO.getInstance().addDeviceToDB(getApplicationContext(),newScanDevice);
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("BLE", "Discovery onScanFailed: " + errorCode);
            super.onScanFailed(errorCode);
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
