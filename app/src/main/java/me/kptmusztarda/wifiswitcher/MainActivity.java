package me.kptmusztarda.wifiswitcher;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.List;

public class MainActivity extends Activity {

    private WifiManager mWifiManager;
    private final static String TAG = "MAIN";
    private final static int TRESHOLD = -70;

    private void checkPermissions() {
        int p1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int p2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int p3 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE);
        int p4 = ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE);
        int p5 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(p1 != 0 || p2 != 0 || p3 != 0 || p4 != 0 || p5 != 0) ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_WIFI_STATE,Manifest.permission.CHANGE_WIFI_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2137);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) android.os.Process.killProcess(android.os.Process.myPid());
    }

    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                WifiInfo currentWiFi = mWifiManager.getConnectionInfo();
                if(currentWiFi.getNetworkId() != -1) {
                    List<ScanResult> mScanResults = mWifiManager.getScanResults();


                    ScanResult strongestWiFi = null;
                    int maxLevel = -127;
                    for (ScanResult result : mScanResults) {
                        //Logger.log(TAG, result.toString());
                        if (result.level > maxLevel) {
                            maxLevel = result.level;
                            strongestWiFi = result;
                        }
                    }

                    if (strongestWiFi != null) {

                        Logger.log(TAG, "Strongest network is \"" + strongestWiFi.SSID + "\" with RSSI: " + Integer.toString(maxLevel) + " dBm");
                        Logger.log(TAG, "Current network is \"" + currentWiFi.getSSID() + "\" with RSSI: " + Integer.toString(currentWiFi.getRssi()) + " dBm");

                        if (!currentWiFi.getSSID().contains(strongestWiFi.SSID) && currentWiFi.getRssi() < TRESHOLD) {
                            Logger.log(TAG, "Switching network");

                            int networkId = -1;
                            for (WifiConfiguration tmp : mWifiManager.getConfiguredNetworks()) {
                                if (tmp.SSID.contains(strongestWiFi.SSID)) {
                                    Logger.log(TAG, "preSharedKey: " + tmp.preSharedKey);
                                    networkId = tmp.networkId;
                                    mWifiManager.enableNetwork(networkId, true);
                                }
                            }
                        } else {
                            Logger.log(TAG, "No need to switch network");
                        }
                    }
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();

        Logger.setDirectory("", "wifi_switcher_log.txt");

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        registerReceiver(mWifiScanReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mWifiManager.startScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mWifiScanReceiver);
    }
}
