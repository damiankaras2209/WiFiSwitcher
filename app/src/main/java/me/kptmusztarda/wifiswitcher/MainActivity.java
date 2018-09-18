package me.kptmusztarda.wifiswitcher;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity {

    private void checkPermissions() {
        int p1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int p2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int p3 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE);
        int p4 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        int p5 = ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE);
        int p6 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(p1 != 0 || p2 != 0 || p3 != 0 || p4 != 0 || p5 != 0 || p6 != 0) ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_WIFI_STATE,Manifest.permission.CHANGE_WIFI_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_NETWORK_STATE}, 2137);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();

        Logger.setDirectory("", "wifi_switcher_log.txt");

        SharedPreferences preferences = getSharedPreferences(getPackageName() + Integer.toString(2137), Context.MODE_PRIVATE);
        final SharedPreferences.Editor prefEditor = preferences.edit();

        final TextView thresholdTextView = findViewById(R.id.textView_treshold_value);
        SeekBar thresholdSeekBar = findViewById(R.id.seekBar_threshold);
        thresholdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                thresholdTextView.setText(Integer.toString(i - 127));

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                prefEditor.putInt("threshold", seekBar.getProgress() -127);
                prefEditor.apply();
            }
        });
        thresholdSeekBar.setProgress(preferences.getInt("threshold", -80) + 127);

        final TextView frequencyTextView = findViewById(R.id.textView_scan_frequency);
        SeekBar frequencySeekBar = findViewById(R.id.seekBar_scan_frequency);
        frequencySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                frequencyTextView.setText(Integer.toString(i));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                prefEditor.putInt("frequency", seekBar.getProgress());
                prefEditor.apply();
            }
        });
        frequencySeekBar.setProgress(preferences.getInt("frequency", 30));

        if(!WiFiScanReceiverService.isRunning())
            startForegroundService(new Intent(this, WiFiScanReceiverService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

//    private boolean isMyServiceRunning(Class<?> serviceClass) {
//        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if (serviceClass.getName().equals(service.service.getClassName())) {
//                return true;
//            }
//        }
//        return false;
//    }
}
