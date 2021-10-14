package com.example.opsapp;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mMainActivity;

    public WifiDirectBroadcastReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel,
                                       MainActivity mainActivity){
        this.mWifiP2pManager= wifiP2pManager;
        this.mChannel= channel;
        this.mMainActivity= mainActivity;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                //mMainActivity.setIsWifiP2pEnabled(true);
                Toast.makeText(context, "Wifi ON", Toast.LENGTH_SHORT).show();
            } else {
                //mMainActivity.setIsWifiP2pEnabled(false);
                Toast.makeText(context, "Wifi OFF", Toast.LENGTH_SHORT).show();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // The peer list has changed! We should probably do something about
            // that.
            if(mWifiP2pManager!=null){
                mWifiP2pManager.requestPeers(mChannel, mMainActivity.peerListListener);
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // Connection state changed! We should probably do something about
            // that.

            if(mWifiP2pManager==null){
                return;
            }

            NetworkInfo networkInfo= intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkInfo.isConnected()){
                mWifiP2pManager.requestConnectionInfo(mChannel, mMainActivity.connectionInfoListener);
            } else{
                //mMainActivity.mConnectionStatusTextView.setText("Disconnect");
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
//            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
//                    .findFragmentById(R.id.frag_list);
//            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
//                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

        }

    }
}
