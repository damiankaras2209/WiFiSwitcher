package me.kptmusztarda.wifiswitcher;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceStarter extends BroadcastReceiver {

    private final static String TAG = "ServiceStarter";

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.log(TAG, "Broadcast received: " + intent.getAction());
//        if(intent.getAction().equals("me.kptmusztarda.wifiswitcher.START_SERVICE")) {
            if(!isMyServiceRunning(context, WiFiScanReceiverService.class)) {
                Logger.log(TAG, "Service is not running. Starting...");
                context.startForegroundService(new Intent(context, WiFiScanReceiverService.class));
            } else {
                Logger.log(TAG, "Service is already running");
            }
//        }
    }

    private boolean isMyServiceRunning(Context c, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
