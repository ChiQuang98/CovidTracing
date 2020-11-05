package com.ptitfinal.covidtracing.view.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ptitfinal.covidtracing.R;
import com.ptitfinal.covidtracing.adapter.NotiRecycleViewAdapter;
import com.ptitfinal.covidtracing.model.Notification;
import com.ptitfinal.covidtracing.repository.firebase.FirebaseDAO;

import java.util.ArrayList;
import java.util.List;


public class NotificationFragment extends Fragment {
    private static final String TAG = NotificationFragment.class.getSimpleName();
    List<Notification> mListNoti = new ArrayList<>();
    private boolean mScanning;
    private Handler handler = new Handler();
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private NotiRecycleViewAdapter notiRecycleViewAdapter;
    private RecyclerView mRecyclerView;

    public NotificationFragment() {
        // Required empty public constructor
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void setUpRecycleView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager
                .VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        notiRecycleViewAdapter = new NotiRecycleViewAdapter(getContext(), mListNoti);
        mRecyclerView.setAdapter(notiRecycleViewAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        setUpRecycleView();
        FirebaseDAO.getInstance().getAllNoti(new FirebaseDAO.OnNotificationListener() {
            @Override
            public void onNotificationChange(List<Notification> notificationList) {
                mListNoti = notificationList;
//                Log.e(TAG,notificationList.get(0).getContent());
                notiRecycleViewAdapter.setClasses(notificationList);
//                notiRecycleViewAdapter.notifyDataSetChanged();
            }
        });

    }




}