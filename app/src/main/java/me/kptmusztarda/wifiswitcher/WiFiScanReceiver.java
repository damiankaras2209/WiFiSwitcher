package me.kptmusztarda.wifiswitcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class WiFiScanReceiver extends BroadcastReceiver {

    private final static String TAG = "WiFiScanReceiver";

    private Timer timer;

    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.i("WiFiScanReceiver", "Broadcast received: " + intent.getAction());
        final WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
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

                    String strongestSSID = "\"" + strongestWiFi.SSID + "\"";
                    Logger.log(TAG, "Strongest network is " + strongestSSID + " with RSSI: " + Integer.toString(maxLevel) + " dBm");
                    Logger.log(TAG, "Current network is " + currentWiFi.getSSID() + " with RSSI: " + Integer.toString(currentWiFi.getRssi()) + " dBm");

                    SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + Integer.toString(2137), Context.MODE_PRIVATE);
                    int threshold = preferences.getInt("threshold", -80);

                    if (!currentWiFi.getSSID().equals(strongestSSID) && currentWiFi.getRssi() < threshold) {
                        Logger.log(TAG, "Switching network (threshold is: " + threshold + " dBm)");

                        int networkId = -1;
                        for (WifiConfiguration tmp : mWifiManager.getConfiguredNetworks()) {
                            if (tmp.SSID.equals(strongestSSID)) {
                                networkId = tmp.networkId;
                                mWifiManager.enableNetwork(networkId, true);
                            }
                        }
                    } else {
                        Logger.log(TAG, "No need to switch network (threshold is: " + threshold + " dBm)");
                    }
                }
            }

        } else if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            int state = Objects.requireNonNull(mWifiManager).getWifiState();
            if(state == WifiManager.WIFI_STATE_ENABLED) {
                Logger.log(TAG, "WiFi is on");

                SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + Integer.toString(2137), Context.MODE_PRIVATE);
                final int delay = preferences.getInt("frequency", 60);
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mWifiManager.startScan();
                        Logger.log(TAG, "Performing scan (delay is: " + delay + "s)");
                    }
                }, 5 * 1000, delay * 1000);

            } else if(state == WifiManager.WIFI_STATE_DISABLED) {
                Logger.log(TAG, "WiFi is off");

                if(timer != null) {
                    timer.cancel();
                    timer.purge();
                }
            }
        }

    }
}
