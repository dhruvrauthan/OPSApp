package com.example.opsapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "==MainActivity";
    private final int REQUEST_LOCATION_PERMISSION = 1;

    //ui
    private Button mDiscoverButton;
    private ListView mDevicesListView;
    private TextView mConnectionStatusTextView;

    //wifi
    private WifiManager mWifiManager;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;

    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mIntentFilter;

    private List<WifiP2pDevice> mPeers = new ArrayList<WifiP2pDevice>();
    private String[] mDeviceNameArray;
    private WifiP2pDevice[] mDeviceArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestLocationPermission();
        initUI();
        initWifi();
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        } else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }

    private void initWifi() {
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);

        mBroadcastReceiver = new WifiDirectBroadcastReceiver(mWifiP2pManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if (!peerList.getDeviceList().equals(mPeers)) {
                mPeers.clear();
                mPeers.addAll(peerList.getDeviceList());

                mDeviceNameArray = new String[peerList.getDeviceList().size()];
                mDeviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];

                int index = 0;
                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    mDeviceNameArray[index] = device.deviceName;
                    mDeviceArray[index] = device;
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_list_item_1, mDeviceNameArray);
                mDevicesListView.setAdapter(adapter);
            }

            if (mPeers.size() == 0) {
                makeToast("No devices found");
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener= new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress= wifiP2pInfo.groupOwnerAddress;

            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                mConnectionStatusTextView.setText("Host");
            } else if(wifiP2pInfo.groupFormed){
                mConnectionStatusTextView.setText("Client");
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    private void initUI() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        mDiscoverButton = findViewById(R.id.button_discover);
        mDevicesListView = findViewById(R.id.listview_devices);
        mConnectionStatusTextView = findViewById(R.id.textview_connection_status);

        initOnClicks();
    }

    //TODO ASK FOR PERMISSIONS
    private void initOnClicks() {
        mDiscoverButton.setOnClickListener(v -> {
            Log.d(TAG, "Clicked button");

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();
                return;
            }
            mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    mConnectionStatusTextView.setText("Discovery started");
                }

                @Override
                public void onFailure(int i) {
                    mConnectionStatusTextView.setText("Discovery starting failed");
                }
            });
        });

        mDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice device= mDeviceArray[i];
                WifiP2pConfig config= new WifiP2pConfig();
                config.deviceAddress= device.deviceAddress;

                mWifiP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        makeToast("Connected to "+device.deviceName);
                    }

                    @Override
                    public void onFailure(int i) {
                        makeToast("Connection failed");
                    }
                });
            }
        });
    }

    private void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}