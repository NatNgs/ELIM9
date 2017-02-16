package fr.unice.polytech.elim.elim9;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
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

public class MonitoringService extends Service {
    private static final String DATA_ELEMENT_FILENAME = "elim9.save";
    protected static final String PARAM_ON_OFF = "activation";
    private BatteryStateReceiver receiver;

    public MonitoringService() {
        super();
        Log.d("MonitoringService","Instantiating new Monitoring Service at "+ Calendar.getInstance().getTimeInMillis());
    }

    @Override
    public void onCreate() {
        Log.d("ELIM9MonitoringService","MonitoringService created");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ELIM9MonitoringService","MonitoringService started");
        toggleOn();
        return super.onStartCommand(intent, flags, startId);
    }

    private void toggleOn() {
        if(receiver == null) {
            Log.d("MonitoringService", "Loading DataElement...");
            DataElement.load(getApplicationContext().getFilesDir().getPath() +DATA_ELEMENT_FILENAME);

            Log.d("MonitoringService", "StartMonitoring...");
            startMonitoring();
        }
        else
            Log.e("MonitoringService", "Already Active");
    }

    private void toggleOff() {
        if(receiver != null) {
            Log.d("MonitoringService", "Shutting down listeners...");
            unregisterReceiver(receiver);

            Log.d("MonitoringService", "Saving DataElement to File...");
            DataElement.save(getApplicationContext().getFilesDir().getPath() +DATA_ELEMENT_FILENAME);

            receiver = null;
            Log.d("MonitoringService", "Inactive !");
        } else {
            Log.e("MonitoringService", "Already Inactive");
        }
    }

    public void startMonitoring(){
        Log.d("MonitoringService", "Activating...");

        receiver = new BatteryStateReceiver();
        // Launching listeners
        registerReceiver(receiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
        registerReceiver(receiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
        registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        //BatteryStateReceiver batRec = new BatteryStateReceiver(this);
        //TODO Tester s'il est déjà link au évènement. Tout faire dans le BRReceiver. 1 event récupère les états de tout!

        Log.d("MonitoringService", "Active !");
    }

    @Override
    public void onDestroy() {
        Log.d("ELIM9MonitoringService","MonitoringService destroyed");
        toggleOff();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new Binder() {
        MonitoringService getService() {
            return MonitoringService.this;
        }
    };
}
