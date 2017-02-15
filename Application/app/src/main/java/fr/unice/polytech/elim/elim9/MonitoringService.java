package fr.unice.polytech.elim.elim9;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.util.Log;

import java.io.File;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class MonitoringService extends IntentService {
    private static final String DATA_FILENAME = "ELIM9MonitoredData";
    protected static final String PARAM_ON_OFF = "activation";
    private boolean isActive = false;
    private static File dataFile = null;
    private final Set<EventReceiver> receivers = new HashSet<>();
    private final String phoneModel = android.os.Build.MODEL;

    final private AsyncTask<Void,Void,Void> at = new AsyncTask<Void,Void,Void>() {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("MonitoringService", "Activating...");

            dataFile = new File(getApplicationContext().getCacheDir(), DATA_FILENAME);
            if(dataFile.exists()) {
                dataFile.delete();
                Log.d("MonitoringService", "Ancient DataFile deleted");
            }

            // Battery is charging or not ?
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            final Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            BatteryData dt = new BatteryData(batteryStatus, isCharging, true);
            Util.storeDatatable(dt);

            // Setting Current status
            EventReceiver.setCharging(isCharging);
            EventReceiver.setScreenOn(true);

            // Preparing listeners
            EventReceiver powerConnectedReceiver = new EventReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    setCharging(true);
                    super.onReceive(context, intent);
                }
            };
            EventReceiver powerDisconnectedReceiver = new EventReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    setCharging(false);
                    super.onReceive(context, intent);
                }
            };
            EventReceiver screenUnlocked = new EventReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    setScreenOn(true);
                    super.onReceive(context, intent);
                }
            };
            EventReceiver screenLocked = new EventReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    setScreenOn(false);
                    super.onReceive(context, intent);
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
        if(!isActive)
            at.execute();
        else
            Log.e("MonitoringService", "Already Active");
    }

    private void toggleOff() {
        if(isActive) {
            Log.d("MonitoringService", "Shutting down listeners...");
            for (EventReceiver er : receivers)
                unregisterReceiver(er);
            receivers.clear();
            isActive = false;
            Log.d("MonitoringService", "Inactive !");
        } else {
            Log.e("MonitoringService", "Already Inactive");
        }
    }

    public static File getDataFile() {
        return dataFile;
    }
}
