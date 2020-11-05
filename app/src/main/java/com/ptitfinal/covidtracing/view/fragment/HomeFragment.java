package com.ptitfinal.covidtracing.view.fragment;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ptitfinal.covidtracing.R;
import com.ptitfinal.covidtracing.listener.onReceiveKeyListener;
import com.ptitfinal.covidtracing.model.Constants;
import com.ptitfinal.covidtracing.model.DeviceScan;
import com.ptitfinal.covidtracing.model.Notification;
import com.ptitfinal.covidtracing.model.SpecificID;
import com.ptitfinal.covidtracing.model.User;
import com.ptitfinal.covidtracing.repository.SQLITE.DeviceDAO;
import com.ptitfinal.covidtracing.repository.firebase.FirebaseDAO;
import com.ptitfinal.covidtracing.service.AdvertiserService;
import com.ptitfinal.covidtracing.service.MyFirebaseService;
import com.ptitfinal.covidtracing.service.ScanningService;
import com.ptitfinal.covidtracing.view.activity.ListExposedActivity;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.BIND_AUTO_CREATE;


public class HomeFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = HomeFragment.class.getSimpleName();
    public static final int REQUEST_ENABLE_BLE = 1;
    private static final int REQUEST_FINE_LOCATION = 2;
    private static final int REQUEST_BACKGROUND_LOCATION = 3;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String DEVICE_NAME = "CVTracing";
    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    //    BluetoothLeScanner btScanner;
    int cnt = 0;
    BluetoothLeScanner mBluetoothLeScanner;
    List<ScanFilter> filters;
    List<DeviceScan> listDeviceScaned;
    List<DeviceScan> listDeviceDB;
    TextView peripheralTextView, mTvScanDevice;
    Button btnScan;
    Button btnStop;
//    String ID = SpecificID.genID(getContext());
    private TextView mText;
    private Button mbtnHistory;
    private Button mDiscoverButton;
    private Handler mHandler = new Handler();
    private BroadcastReceiver advertisingFailureReceiver;
    private BroadcastReceiver scanningFailureReceiver;
    ScanningService scanningService;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
    Date dateobj = new Date();
    String today = dateFormat.format(dateobj);

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            peripheralTextView.append("Device Name: " + result.getDevice().getName() + " rssi: " + result.getRssi() + "\n");
            // auto scroll for text view
            final int scrollAmount = peripheralTextView.getLayout().getLineTop(peripheralTextView.getLineCount()) - peripheralTextView.getHeight();
            // if there is no need to scroll, scrollAmount will be <=0
            if (scrollAmount > 0)
                peripheralTextView.scrollTo(0, scrollAmount);
        }
    };

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
            DeviceScan newScanDevice = new DeviceScan(builder.toString(),dateFormat.format(dateobj));
            if(DeviceScan.checkExist(listDeviceScaned,newScanDevice)==false){
                listDeviceScaned.add(newScanDevice);
            }
            if(DeviceScan.checkExist(listDeviceDB,newScanDevice)==false){
                Log.e(TAG,"add to DB");
                DeviceDAO.getInstance().addDeviceToDB(getActivity().getApplicationContext(),newScanDevice);
            }
            mTvScanDevice.setText(Integer.toString(listDeviceScaned.size()));
            Log.e(TAG,"In single");
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
                if(DeviceScan.checkExist(listDeviceScaned,newScanDevice)==false){
                    listDeviceScaned.add(newScanDevice);
                }
                if(DeviceScan.checkExist(listDeviceDB,newScanDevice)==false){
                    DeviceDAO.getInstance().addDeviceToDB(getContext(),newScanDevice);
                }
            }
            
            mTvScanDevice.setText(Integer.toString(listDeviceScaned.size()));
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("BLE", "Discovery onScanFailed: " + errorCode);
            super.onScanFailed(errorCode);
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    private static Intent getServiceIntent(Context c) {
        return new Intent(c, AdvertiserService.class);
    }
    private static Intent getServiceIntentScanning(Context c) {
        return new Intent(c, ScanningService.class);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mHandler = new Handler();
        listDeviceScaned = new ArrayList<>();
        listDeviceDB = new ArrayList<>();
        broadCast();
        initBLEComponent();
//        enableBt();
        checkPm();
        startAdvertising();
    }

    private void broadCast() {
        scanningFailureReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
        advertisingFailureReceiver = new BroadcastReceiver() {

            /**
             * Receives Advertising error codes from {@code AdvertiserService} and displays error messages
             * to the user. Sets the advertising toggle to 'false.'
             */
            @Override
            public void onReceive(Context context, Intent intent) {

                int errorCode = intent.getIntExtra(AdvertiserService.ADVERTISING_FAILED_EXTRA_CODE, -1);

//                mSwitch.setChecked(false);

                String errorMessage = getString(R.string.start_error_prefix);
                switch (errorCode) {
                    case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                        errorMessage += " " + getString(R.string.start_error_already_started);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                        errorMessage += " " + getString(R.string.start_error_too_large);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                        errorMessage += " " + getString(R.string.start_error_unsupported);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                        errorMessage += " " + getString(R.string.start_error_internal);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                        errorMessage += " " + getString(R.string.start_error_too_many);
                        break;
                    case AdvertiserService.ADVERTISING_TIMED_OUT:
                        errorMessage = " " + getString(R.string.advertising_timedout);
                        break;
                    default:
                        errorMessage += " " + getString(R.string.start_error_unknown);
                }

                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
            }
        };
    }
    boolean mBounded= false;
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
//            Toast.makeText(Client.this, "Service is disconnected", 1000).show();
            mBounded = false;
            scanningService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
//            Toast.makeText(Client.this, "Service is connected", 1000).show();
            mBounded = true;
            ScanningService.LocalBinder mLocalBinder = (ScanningService.LocalBinder)service;
            scanningService = mLocalBinder.getScanningService();
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        Intent mIntent = new Intent(getContext(), ScanningService.class);
        getActivity().bindService(mIntent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mBounded) {
            getActivity().unbindService(mConnection);
            mBounded = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter failureFilter = new IntentFilter(AdvertiserService.ADVERTISING_FAILED);
        IntentFilter failureFilterScan = new IntentFilter(ScanningService.SCANNING_FAILED);
        getActivity().registerReceiver(scanningFailureReceiver,failureFilter);
        getActivity().registerReceiver(advertisingFailureReceiver, failureFilterScan);
        if(listDeviceDB.size()>0){
            listDeviceDB.clear();

        }
        listDeviceDB = DeviceDAO.getInstance().getDeviceScanByDay(getContext(),today);
        Log.e(TAG,"size today: "+DeviceDAO.getInstance().getDeviceScanByDay(getContext(),today).size());

    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(advertisingFailureReceiver);
        getActivity().unregisterReceiver(scanningFailureReceiver);
    }

    private void startAdvertising() {
        Context c = getActivity();
        c.startService(getServiceIntent(c));
        c.startService(getServiceIntentScanning(c));
    }

    /**
     * Stops BLE Advertising by stopping {@code AdvertiserService}.
     */
    private void stopAdvertising() {
        Context c = getActivity();
        c.stopService(getServiceIntent(c));
    }

    private void initWidget(View view) {
//        peripheralTextView = view.findViewById(R.id.PeripheralTextView);
//        peripheralTextView.setMovementMethod(new ScrollingMovementMethod());
//        btnScan = view.findViewById(R.id.btnScan);
//        btnStop = view.findViewById(R.id.btnStopScan);
        filters = new ArrayList<>();
//        mText = (TextView) view.findViewById( R.id.tv_device_scaned );
//        btnStop.setOnClickListener(this);
//        btnScan.setOnClickListener(this);
        mDiscoverButton = (Button) view.findViewById(R.id.btn_scan);
        mbtnHistory = (Button) view.findViewById(R.id.btn_expose);
        mTvScanDevice = (TextView) view.findViewById(R.id.tv_device_scaned);
        tvNotiF1 = view.findViewById(R.id.tv_F1);
        mDiscoverButton.setOnClickListener(this);
        mbtnHistory.setOnClickListener(this);
    }

    private void checkPm() {
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_FINE_LOCATION);
        checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION, REQUEST_BACKGROUND_LOCATION);//require for ANDROID 10
        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
    }

    private void initBLEComponent() {


        btManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
//        btAdapter = btManager.getAdapter();
//        btAdapter = BluetoothAdapter.getDefaultAdapter();

        btAdapter = btManager.getAdapter();
        enableBt();

    }

    public void enableBt() {
        if (btAdapter == null) {
            Toast.makeText(getContext(), "Thiết bị không hỗ trợ BLE", Toast.LENGTH_SHORT).show();
        } else if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLE);
        }
        else{
            initbtAdapter();
        }
    }

    void initbtAdapter(){
        Log.e("GG","IN");
        btAdapter = btManager.getAdapter();
        mBluetoothLeScanner = btAdapter.getBluetoothLeScanner();
        btAdapter.setName(DEVICE_NAME);//Ten App - device
        if (!BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
            Toast.makeText(getContext(), "Multiple advertisement not supported", Toast.LENGTH_SHORT).show();

        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==REQUEST_ENABLE_BLE&&resultCode==RESULT_OK){
            initbtAdapter();
        }
    }

    public void checkPermission(String permission, int requestCode) {

        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(
                getContext(),
                permission)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat
                    .requestPermissions(
                            getActivity(),
                            new String[]{permission},
                            requestCode);
        } else {
            Toast
                    .makeText(getContext(),
                            "Permission đã được cấp phép",
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BottomNavigationView navView = getActivity().findViewById(R.id.bottom_navigation);
        navView.setVisibility(View.VISIBLE);
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getContext(), "ble_không hỗ trợ trên thiết bị này", Toast.LENGTH_SHORT).show();
//            finish();
        } else {
            Toast.makeText(getContext(), "ble_ có hỗ trợ", Toast.LENGTH_SHORT).show();
        }
        initWidget(view);
        notifyF1Covid();

    }
    private Dialog mDialogNotificaton;
    private TextView tvNotiF1;
    private void notifyF1Covid() {
//        FirebaseDAO.getInstance().addNoti(new Notification("Tessttttttttttttt","title"));

        Log.e(TAG,"In notifyF1");
        SharedPreferences prefs = getContext().getSharedPreferences("MyPreferences",getContext().MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putString("is", );
//        editor.apply();
        String keyExposed = prefs.getString("keyExposedNew", null);
        boolean isViewNotification = prefs.getBoolean("isViewNotification", false);
        if(keyExposed!=null){
            boolean isExposedF1 = User.checkIsExposed(getContext(),keyExposed);
            if(isViewNotification==true&&isExposedF1==true){
                tvNotiF1.setVisibility(View.VISIBLE);
            }
            else{
                tvNotiF1.setVisibility(View.GONE);
            }
        }

        if(keyExposed!=null){
            Log.e(TAG,"In notifyF1: "+keyExposed);
            boolean isExposedF1 = User.checkIsExposed(getContext(),keyExposed);
            String contentNoti = prefs.getString("Notification",null);
            if(isExposedF1==true&&isViewNotification!=true){
                if(keyExposed==null){
                    Log.e(TAG,"key exposed is Null");
//            Toast.makeText(getContext(),"key exposed is Null",Toast.LENGTH_SHORT).show();
                }
                else{
                    tvNotiF1.setVisibility(View.VISIBLE);
                    FirebaseDAO.getInstance().update(prefs.getString("keyCovid",null),"F1");
                    mDialogNotificaton = new Dialog(getContext());
                    mDialogNotificaton.setContentView(R.layout.dialog_notification_f0);
                    Button dialog_btnCancel = mDialogNotificaton.findViewById(R.id.btn_dialog_rename_cancel);
                    TextView tvNotification = mDialogNotificaton.findViewById(R.id.tvNotification);
                    tvNotification.setText(contentNoti);
                    dialog_btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Editor editor = getEditorSharePreferrences(getContext());
                            editor.putBoolean("isViewNotification",true);
                            editor.apply();
                            mDialogNotificaton.cancel();
                        }
                    });
                    Editor editor = getEditorSharePreferrences(getContext());
                    editor.putBoolean("isViewNotification",true);
                    editor.apply();
                    mDialogNotificaton.show();

                    //push dialog hien noti tai day
                    Log.e(TAG,"key: "+keyExposed);
//            return true;
                }
            }

        }

    }
    private Editor getEditorSharePreferrences(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences("MyPreferences", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        return editor;
    }
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                if(btAdapter==null){
                    Toast.makeText(getContext(),"Thiết bị không có BLE",Toast.LENGTH_SHORT).show();
                }
                else{
                    scanningService.turnOffScheduleScan();
                    listDeviceScaned = new ArrayList<>();
                    discover();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBluetoothLeScanner.stopScan(mScanCallback);
                            scanningService.turnOnScheduleScan();
                        }
                    }, 10000);
                }

                break;
            case R.id.btn_expose:
                List<DeviceScan> list = DeviceDAO.getInstance().getDeviceScanByDay(getContext(),today);
//                Log.e(TAG,"Lenght device scanned: "+list.size()+" |test: "+ list.get(0).getDayInteraction() );
                StringBuilder ID = new StringBuilder(SpecificID.genID(getContext()));
                ID.setLength(9);
                Log.e(TAG,"MAC: "+ID.toString());
                Intent intent = new Intent(getActivity(), ListExposedActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void discover() {
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
//    private void stopScanning() {
//        System.out.println("stopping scanning");
//        peripheralTextView.append("Stopped Scanning");
//        btnScan.setVisibility(View.VISIBLE);
//        btnStop.setVisibility(View.INVISIBLE);
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                btScanner.stopScan(leScanCallback);
//            }
//        });
//    }
//
//    private void startScaning() {
//        System.out.println("start scanning");
//        peripheralTextView.setText("Start");
//        btnScan.setVisibility(View.INVISIBLE);
//        btnStop.setVisibility(View.VISIBLE);
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                btScanner.startScan(leScanCallback);
//            }
//        });
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mBluetoothLeScanner!=null){
            mBluetoothLeScanner.stopScan(mScanCallback);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Vị trí đã được cho phép");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("Quyền vị trí không được bật, vì vậy ứng dụng sẽ không thể tìm các thiết bị khác");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }
}