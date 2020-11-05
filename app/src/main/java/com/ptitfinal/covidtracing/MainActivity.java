package com.ptitfinal.covidtracing;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ptitfinal.covidtracing.listener.BLECommunication;
import com.ptitfinal.covidtracing.manager.BleManager;
import com.ptitfinal.covidtracing.model.SpecificID;

public class MainActivity extends AppCompatActivity implements BLECommunication {
    private static final String TAG = MainActivity.class.getSimpleName();
    private BleManager bleManager;
    private Button btn;
    private BottomNavigationView navigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        navView.setVisibility(View.VISIBLE);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);
        bleManager = new BleManager(this,this);
        //bleManager.enabledBluetooth(MainActivity.this);
//        btn = findViewById(R.id.button);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {

//            }
//        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        bleManager.onRequestPermissionBluetooth(requestCode,resultCode,data);
    }

    @Override
    public void onBleSupported() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,"BLE Supported",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBleNotSupported() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,"BLE not Supported",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBluetoothEnbled(final boolean enabled, String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String enabledBLE = enabled?"BLE enable":"BLE Not enable";
                Toast.makeText(MainActivity.this,enabledBLE,Toast.LENGTH_SHORT).show();
            }
        });
    }
}