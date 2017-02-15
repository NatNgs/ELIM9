package fr.unice.polytech.elim.elim9;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class MonitoringService extends IntentService {
    private static final String phoneModel = android.os.Build.MODEL;
    private static final String DATA_FILENAME = "ELIM9MonitoredData";
    protected static final String PARAM_ON_OFF = "activation";

    private static File dataFile = null;

    private final Set<BroadcastReceiver> receivers = new HashSet<>();
    private boolean isMonitoring = false;
    private boolean isBatteryCharging = isBatteryCharging();
    private boolean isScreenActive = isScreenActive();

    final private AsyncTask<Void,Void,Void> at = new AsyncTask<Void,Void,Void>() {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("MonitoringService", "Activating...");

            dataFile = new File(getApplicationContext().getCacheDir(), DATA_FILENAME);
            if(dataFile.exists()) {
                dataFile.delete();
                Log.d("MonitoringService", "Ancient DataFile deleted");
            }

            // Preparing listeners
            BroadcastReceiver powerConnectedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    isBatteryCharging = true;
                    pushData();
                }
            };
            BroadcastReceiver powerDisconnectedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    isBatteryCharging = false;
                    pushData();
                }
            };
            BroadcastReceiver screenUnlocked = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    isScreenActive = true;
                    pushData();
                }
            };
            BroadcastReceiver screenLocked = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    isScreenActive = false;
                    pushData();
                }
            };

            // Launching listeners
            registerReceiver(powerConnectedReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
            registerReceiver(powerDisconnectedReceiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
            registerReceiver(screenUnlocked, new IntentFilter(Intent.ACTION_SCREEN_ON));
            registerReceiver(screenLocked, new IntentFilter(Intent.ACTION_SCREEN_OFF));


            Log.d("MonitoringService", "Active !");

            return null;
        }
    };

    public MonitoringService() {
        super("Monitoring Service");
        Log.d("MonitoringService","Instantiating new Monitoring Service at "+ Calendar.getInstance().getTimeInMillis());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String msg = intent.getStringExtra(PARAM_ON_OFF);

        if(msg.equalsIgnoreCase("on")) {
            toggleOn();
        } else {
            Log.d("MonitoringService", "Received message: "+msg);
            toggleOff();
        }
    }

    private void toggleOn() {
        if(!isMonitoring)
            at.execute();
        else
            Log.e("MonitoringService", "Already Active");
    }

    private void toggleOff() {
        if(isMonitoring) {
            Log.d("MonitoringService", "Shutting down listeners...");
            for (BroadcastReceiver br : receivers)
                unregisterReceiver(br);
            receivers.clear();
            isMonitoring = false;
            Log.d("MonitoringService", "Inactive !");
        } else {
            Log.e("MonitoringService", "Already Inactive");
        }
    }

    public boolean isBatteryCharging() {
        // Battery is charging or not ?
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;
    }
    public boolean isScreenActive() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return powerManager.isInteractive();
        }
        return powerManager.isScreenOn();
    }


    private void pushData() {
        // TODO
        Log.d("PushedData", "Data:"+isBatteryCharging+"/"+isScreenActive);
    }

    public static File getDataFile() {
        return dataFile;
    }
}
