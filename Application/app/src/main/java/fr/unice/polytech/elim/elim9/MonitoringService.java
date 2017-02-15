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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

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
    private boolean isBatteryCharging;
    private boolean isScreenActive;


    public MonitoringService() {
        super("Monitoring Service");
        Log.d("MonitoringService","Instantiating new Monitoring Service at "+ Calendar.getInstance().getTimeInMillis());
        isBatteryCharging = isBatteryCharging();
        isScreenActive = isScreenActive();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean msg = intent.getBooleanExtra(PARAM_ON_OFF, false);

        if(msg) {
            toggleOn();
        } else {
            Log.d("MonitoringService", "Received message: "+msg);
            toggleOff();
        }
    }

    private void toggleOn() {
        if(!isMonitoring) {
            startMinotoring();
            Log.d("MonitoringService", "StartMonitoring...");
            for (BroadcastReceiver br : receivers) {
                Log.d("MonitoringService", "Listen on :" + br.toString());
                unregisterReceiver(br);
            }
            isMonitoring = true;
        }
        else
            Log.e("MonitoringService", "Already Active");
    }

    private void toggleOff() {
        if(true) {
            Log.d("MonitoringService", "Shutting down listeners...");
            for (BroadcastReceiver br : receivers) {
                Log.d("MonitoringService", "Shutting down listener :" + br.toString());
                unregisterReceiver(br);
            }
            receivers.clear();
            isMonitoring = false;
            Log.d("MonitoringService", "Inactive !");
        } else {
            Log.e("MonitoringService", "Already Inactive");
        }
    }

    public void startMinotoring(){
        Log.d("MonitoringService", "Activating...");

        dataFile = new File(getCacheDir(), DATA_FILENAME);
        if(dataFile.exists()) {
            //dataFile.delete();
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
        receivers.add(powerConnectedReceiver);
        registerReceiver(powerDisconnectedReceiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
        receivers.add(powerDisconnectedReceiver);
        registerReceiver(screenUnlocked, new IntentFilter(Intent.ACTION_SCREEN_ON));
        receivers.add(screenUnlocked);
        registerReceiver(screenLocked, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        receivers.add(screenLocked);

        Log.d("MonitoringService", "Active !");
    }

    public boolean isBatteryCharging() {
        //TODO
        // Battery is charging or not ?
        /*Log.d(this.toString(), "yolo");
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        Log.d("d,ffd,",ifilter.toString());
        Intent batteryStatus = registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL; */
        return false;
    }

    public boolean isScreenActive() {
        /*PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return powerManager.isInteractive();
        }
        return powerManager.isScreenOn();
        */
        return true;
    }


    private void pushData() {

        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        final String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // TODO
        Log.d("PushedData", "Data:"+isBatteryCharging+"/"+isScreenActive);

    }

    public static File getDataFile() {
        return dataFile;
    }
}
