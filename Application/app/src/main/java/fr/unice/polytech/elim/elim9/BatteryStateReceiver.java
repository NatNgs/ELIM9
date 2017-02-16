package fr.unice.polytech.elim.elim9;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Debug;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static android.content.Context.ACTIVITY_SERVICE;

public class BatteryStateReceiver extends BroadcastReceiver {
    private static final String DATA_FILENAME = "ELIM9MonitoredData";
    private static boolean isBatteryCharging = false;
    private static boolean isScreenActive = true;
    private static File dataFile = null;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving

        AsyncTask<Void,Void,Void> async = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                pushData(context);
                IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = context.registerReceiver(null, ifilter);

                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                isBatteryCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;

                switch (intent.getAction()) {
                    case Intent.ACTION_POWER_CONNECTED:
                        isBatteryCharging = true;
                        break;
                    case Intent.ACTION_POWER_DISCONNECTED:
                        isBatteryCharging = false;
                        break;
                    case Intent.ACTION_SCREEN_ON:
                        isScreenActive = true;
                        break;
                    case Intent.ACTION_SCREEN_OFF:
                        isScreenActive = false;
                        break;
                }

                return null;
            }
        };
        async.execute();
    }


    private String lastSentData = "";
    @SuppressLint("SimpleDateFormat")
    private void pushData(Context context) {
        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference dbRef = database.getReference().child("users").child(id).child(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));

        long time = Calendar.getInstance().getTimeInMillis();

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        assert batteryStatus != null;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int levelScale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        List<ApplicationInfo> appList = context.getPackageManager().getInstalledApplications(0);
        int systemAppsNb = appList.size();
        int nonSystemAppsNb = 0;
        for(ApplicationInfo info : appList) {
            if((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                nonSystemAppsNb++;
            }
        }

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long ram = mi.totalMem - mi.availMem;


        Log.d("BatteryStateReceiver", "ScreenActive:"+isScreenActive+", BatteryCharging:"+isBatteryCharging);
        DataElement.getInstance().putRam(time, mi.totalMem - mi.availMem);
        if(isScreenActive && isBatteryCharging) {
            DataElement.getInstance().putChargeActive(time,level,levelScale);
        } else if(isScreenActive){
            DataElement.getInstance().putDischargeActive(time,level,levelScale);
        } else if(isBatteryCharging){
            DataElement.getInstance().putChargeInactive(time,level,levelScale);
        } else{
            DataElement.getInstance().putDischargeInactive(time,level,levelScale);
        }

        // TODO remove from here for disable automatically push
        Map<String,Object> data = DataElement.getInstance().toMap();
        String dataStr = data.toString();
        if(data.size() < 2 || dataStr.isEmpty() || dataStr.equals(lastSentData)) {
            Log.d("BatteryStateReceiver", "Data not pushed because empty or remaining unchanged:"+data);
            return;
        }


        DatabaseReference childRef = dbRef.child("dates").child(new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime()));
        childRef.setValue(data);
        childRef.child("lastBatteryValue")
                .setValue(level+"/"+levelScale);
        dbRef.child("buildId")
                .setValue(Build.ID);
        dbRef.child("systemAppNb")
                .setValue(systemAppsNb-nonSystemAppsNb);
        dbRef.child("othersAppNb")
                .setValue(nonSystemAppsNb);

        Log.d("BatteryStateReceiver", "PushedData:"+data);
        lastSentData = data.toString();
    }
}
