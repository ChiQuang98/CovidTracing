package com.ptitfinal.covidtracing.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.ptitfinal.covidtracing.MainActivity;
import com.ptitfinal.covidtracing.R;
import com.ptitfinal.covidtracing.model.Constants;
import com.ptitfinal.covidtracing.model.SpecificID;

import java.util.concurrent.TimeUnit;

public class AdvertiserService extends Service {

    private static final String TAG = AdvertiserService.class.getSimpleName();

    private static final int FOREGROUND_NOTIFICATION_ID = 1;
    public static boolean running = false;

    public static final String ADVERTISING_FAILED =
            "com.example.android.bluetoothadvertisements.advertising_failed";
    private static final String DEVICE_NAME = "CVTracing";

    public static final String ADVERTISING_FAILED_EXTRA_CODE = "failureCode";

    public static final int ADVERTISING_TIMED_OUT = 6;

    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    private AdvertiseCallback mAdvertiseCallback;

    private Handler mHandler;

    private Runnable timeoutRunnable;
    private long TIMEOUT = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);
    public AdvertiserService() {
    }

    @Override
    public void onCreate() {
        running = true;
        initialize();
        startAdvertising();
//        setTimeout();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private void initialize() {
        if (mBluetoothLeAdvertiser == null) {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager != null) {
                BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
                if (mBluetoothAdapter != null) {
                    mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
                } else {
                    Toast.makeText(this, getString(R.string.bt_null), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.bt_null), Toast.LENGTH_LONG).show();
            }

        }

    }
    private void setTimeout(){
        mHandler = new Handler();
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "AdvertiserService has reached timeout of "+TIMEOUT+" milliseconds, stopping advertising.");
                sendFailureIntent(ADVERTISING_TIMED_OUT);
                stopSelf();
            }
        };
        mHandler.postDelayed(timeoutRunnable, TIMEOUT);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startAdvertising() {
        goForeground();

        Log.d(TAG, "Service: Starting Advertising");

        if (mAdvertiseCallback == null) {
            AdvertiseSettings settings = buildAdvertiseSettings();
            AdvertiseData data = buildAdvertiseData();
            mAdvertiseCallback = new SampleAdvertiseCallback();

            if (mBluetoothLeAdvertiser != null) {
                mBluetoothLeAdvertiser.startAdvertising(settings, data,
                        mAdvertiseCallback);
            }
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
                .setContentTitle("Advertising device via Bluetooth")
                .setContentText("This device is discoverable to others nearby.")
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .setContentIntent(pendingIntent)
                .build();
 }
        else {
            channel = "";
            n = new Notification.Builder(this)
                    .setContentTitle("Advertising device via Bluetooth")
                    .setContentText("This device is discoverable to others nearby.")
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

    private void stopAdvertising() {
        Log.d(TAG, "Service: Stopping Advertising");
        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            mAdvertiseCallback = null;
        }
    }
    private AdvertiseData buildAdvertiseData() {

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.addServiceUuid(Constants.Service_UUID);
        dataBuilder.setIncludeDeviceName(true);
        /* For example - this will cause advertising to fail (exceeds size limit) */
//        String ID = SpecificID.genID(this);
        StringBuilder ID = new StringBuilder(SpecificID.genID(this));
        ID.setLength(9);
        dataBuilder.addServiceData(Constants.Service_UUID, ID.toString().getBytes());
        return dataBuilder.build();
    }
    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        settingsBuilder.setTimeout(0);
        return settingsBuilder.build();
    }
    private class SampleAdvertiseCallback extends AdvertiseCallback {

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);

            Log.d(TAG, "Advertising failed");
            sendFailureIntent(errorCode);
            stopSelf();

        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d(TAG, "Advertising successfully started");
        }
    }
    private void sendFailureIntent(int errorCode){
        Intent failureIntent = new Intent();
        failureIntent.setAction(ADVERTISING_FAILED);
        failureIntent.putExtra(ADVERTISING_FAILED_EXTRA_CODE, errorCode);
        sendBroadcast(failureIntent);
    }
}
