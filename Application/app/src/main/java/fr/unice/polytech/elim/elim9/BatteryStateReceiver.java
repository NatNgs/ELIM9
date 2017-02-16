package fr.unice.polytech.elim.elim9;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class BatteryStateReceiver extends BroadcastReceiver {
    private static final String DATA_FILENAME = "ELIM9MonitoredData";
    private boolean isBatteryCharging = false;
    private boolean isScreenActive = true;
    private static File dataFile = null;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving

        AsyncTask<Void,Void,Void> async = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                pushData(context);
                switch (intent.getAction()) {
                    case Intent.ACTION_POWER_CONNECTED:
                        break;
                    case Intent.ACTION_POWER_DISCONNECTED:
                        break;
                    case Intent.ACTION_SCREEN_ON:
                        break;
                    case Intent.ACTION_SCREEN_OFF:
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

        DatabaseReference ref = database.getReference();
        DatabaseReference user = ref.child("users").child(id);

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
        Map<String,Object> data = DataElement.instance.toMap();
        if(data == null || data.toString().isEmpty() || data.toString().equals(lastSentData)) {
            Log.d("MonitoringService", "Data not pushed because remain unchanged:"+data);
            return;
        }

        user.child("dates").child(new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime()))
                .setValue(data);
        user.child("buildModel")
                .setValue(android.os.Build.MODEL);
        user.child("systemAppNb")
                .setValue(systemAppsNb);
        user.child("othersAppNb")
                .setValue(nonSystemAppsNb);

        Log.d("MonitoringService", "PushedData:"+data);
        lastSentData = data.toString();
    }
}
