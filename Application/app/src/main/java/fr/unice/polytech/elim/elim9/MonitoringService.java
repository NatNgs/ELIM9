package fr.unice.polytech.elim.elim9;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.BatteryManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MonitoringService extends IntentService {
    private static final String DATA_FILENAME = "ELIM9MonitoredData";
    protected static final String PARAM_ON_OFF = "activation";

    private static File dataFile = null;

    private final Set<BroadcastReceiver> receivers = new HashSet<>();
    private boolean isMonitoring = false;
    private boolean isBatteryCharging;
    private boolean isScreenActive;

    private DataElement datas;

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
            Log.d("MonitoringService", "Loading DataElement...");
            DataElement.load();

            Log.d("MonitoringService", "StartMonitoring...");

            int numberOfNonSystemApps = 0;

            List<ApplicationInfo> appList = getPackageManager().getInstalledApplications(0);
            for(ApplicationInfo info : appList) {
                if((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    numberOfNonSystemApps++;
                }
            }
            // Application number ^

            startMonitoring();
            isMonitoring = true;
        }
        else
            Log.e("MonitoringService", "Already Active");
    }

    private void toggleOff() {
        if(isMonitoring) {
            Log.d("MonitoringService", "Shutting down listeners...");
            for (BroadcastReceiver br : receivers) {
                unregisterReceiver(br);
            }

            Log.d("MonitoringService", "Saving DataElement to File...");
            DataElement.save();

            receivers.clear();
            isMonitoring = false;
            Log.d("MonitoringService", "Inactive !");
        } else {
            Log.e("MonitoringService", "Already Inactive");
        }
    }

    public void startMonitoring(){
        Log.d("MonitoringService", "Activating...");

        dataFile = new File(getCacheDir(), DATA_FILENAME);
        if(dataFile.exists()) {
            //dataFile.delete();
            Log.d("MonitoringService", "Ancient DataFile deleted");
        }

        // Preparing listeners
        /*BroadcastReceiver powerConnectedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                pushData();
                isBatteryCharging = true;
            }
        };
        BroadcastReceiver powerDisconnectedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                pushData();
                isBatteryCharging = false;
            }
        };
        BroadcastReceiver screenUnlocked = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                pushData();
                isScreenActive = true;
            }
        };
        BroadcastReceiver screenLocked = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                pushData();
                isScreenActive = false;
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
        */

        BatteryStateReceiver batRec = new BatteryStateReceiver();
        //TODO Tester s'il est déjà link au évènement. Tout faire dans le BRReceiver. 1 event récupère les états de tout!
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
        // TODO
        /*PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return powerManager.isInteractive();
        }
        return powerManager.isScreenOn();
        */
        return true;
    }


    @SuppressLint("SimpleDateFormat")
    private void pushData() {
        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = database.getReference();
        DatabaseReference user = ref.child("users").child(id);

        long time = Calendar.getInstance().getTimeInMillis();

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
        assert batteryStatus != null;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int levelScale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        if(isScreenActive && isBatteryCharging) {
            DataElement.instance.putChargeActive(time,level,levelScale);
        } else if(isScreenActive){
            DataElement.instance.putDischargeActive(time,level,levelScale);
        } else if(isBatteryCharging){
            DataElement.instance.putChargeInactive(time,level,levelScale);
        } else{
            DataElement.instance.putDischargeInactive(time,level,levelScale);
        }

        // TODO remove from here for disable automatically push
        String json = DataElement.instance.toJsonString();
        user.setValue(
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance()),
                json);

        user.setValue(
                "buildModel",
                android.os.Build.MODEL);

        Log.d("PushedData", "Data:"+json);
    }

    public static File getDataFile() {
        return dataFile;
    }
}
