package me.kptmusztarda.wifiswitcher;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class WiFiScanReceiverService extends Service {

    private final static String TAG = "WiFiScanReceiverService";
    private static boolean isRunning;
    private WiFiScanReceiver wiFiScanReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(startId, new Notification());
        isRunning = true;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.setPriority(100);

        wiFiScanReceiver = new WiFiScanReceiver();
        registerReceiver(wiFiScanReceiver, intentFilter);
        Logger.log(TAG, "WiFiScanReceiver is registered");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.log(TAG, "Service.onDestroy");
        isRunning = false;
        if(wiFiScanReceiver!=null) {
            unregisterReceiver(wiFiScanReceiver);
            Logger.log(TAG, "WiFiScanReceiver is registered");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected static boolean isRunning(){
        return isRunning;
    }
}
