package fr.unice.polytech.elim9;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class Activity1 extends AppCompatActivity {
    private static final String DATA_FILENAME = "batteryMonitorData";
    private boolean isActive = false;
    private static File dataFile = null;
    private final Set<EventReceiver> receivers = new HashSet<>();
    private EventReceiver onSynchronizeReceiver;
    private Runnable refreshTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void monitoringToggled(View view) {
        Switch switchOnOff = (Switch) findViewById(R.id.switchOnOff);
        boolean isActive = switchOnOff.isChecked();

        if (!this.isActive && isActive) {
            this.isActive = true;
            toggleOn();
        } else if(this.isActive && !isActive) {
            this.isActive = false;
            toggleOff();
        }
        synchronizeData(view);
    }

    private void toggleOn() {
        AsyncTask<Void,Void,Void> at = new AsyncTask<Void,Void,Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                final String phoneModel = android.os.Build.MODEL;
                final TextView phoneModelValue = (TextView) findViewById(R.id.phoneModelValue);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        phoneModelValue.setText(phoneModel);
                    }
                });

                dataFile = new File(getApplicationContext().getCacheDir(), DATA_FILENAME);
                if(dataFile.exists()) {
                    dataFile.delete();
                    Log.d("Main#ToggleOn", "Ancient DataFile deleted");
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

                onSynchronizeReceiver = new EventReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        super.onReceive(context, intent);
                    }
                };

                // Launching listeners
                registerReceiver(powerConnectedReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
                registerReceiver(powerDisconnectedReceiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
                registerReceiver(screenUnlocked, new IntentFilter(Intent.ACTION_SCREEN_ON));
                registerReceiver(screenLocked, new IntentFilter(Intent.ACTION_SCREEN_OFF));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Toggled On", Toast.LENGTH_SHORT).show();
                    }
                });

                refreshTask = new Runnable() {
                    @Override
                    public void run() {
                        while(isActive) {
                            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                            Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
                            onSynchronizeReceiver.onReceive(getApplicationContext(), batteryStatus);

                            try {
                                BatteryData dt = Util.loadDatatable();
                                if(dt != null)
                                    updateDataShown(dt);
                                Thread.sleep(5000);
                            } catch (Exception ignored) {}
                        }
                    }
                };
                refreshTask.run();

                return null;
            }
        };
        at.execute();
    }

    private void toggleOff() {
        for(EventReceiver er : receivers)
            unregisterReceiver(er);
        receivers.clear();
        Toast.makeText(getApplicationContext(), "Toggled Off", Toast.LENGTH_SHORT).show();
    }

    public void synchronizeData(View view) {
        AsyncTask<Void,Void,Void> at = new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                // Getting battery informations
                IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
                assert batteryStatus != null;
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;

                // Generating a new batteryData in case we can't read stored one
                BatteryData dt = new BatteryData(batteryStatus, isCharging, true);

                try {
                    // Load stored batteryData
                    BatteryData newDt = Util.loadDatatable();
                    if(newDt != null)
                        dt = newDt;

                    throw new Exception("Cannot access to server");
                } catch (final Exception ex) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                final BatteryData finalDt = dt;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), finalDt.toJsonString(), Toast.LENGTH_LONG).show();
                    }
                });

                updateDataShown(dt);
                return null;
            }
        };
        at.execute();
    }

    private void updateDataShown(BatteryData dt) {
        HorizontalScrollView myPhoneStats = (HorizontalScrollView) findViewById(R.id.myPhoneStats);
        setTextOnUIThread(myPhoneStats.findViewById(R.id.chargingAvgTime),
                (int)(100*dt.get(StatType.CHARGING_TIME)/dt.get(StatType.CHARGING_PCT))+"s");
        setTextOnUIThread(myPhoneStats.findViewById(R.id.chargingTotalTime),
                (int)dt.get(StatType.CHARGING_TIME)+"s");
        setTextOnUIThread(myPhoneStats.findViewById(R.id.chargingTotalValue),
                (int)(dt.get(StatType.CHARGING_PCT)*100)+"%");

        setTextOnUIThread(myPhoneStats.findViewById(R.id.activeAvgTime),
                (int)(100*dt.get(StatType.ACTIVE_DISCHARGING_TIME)/dt.get(StatType.ACTIVE_DISCHARGING_PCT))+"s");
        setTextOnUIThread(myPhoneStats.findViewById(R.id.activeTotalTime),
                (int)dt.get(StatType.ACTIVE_DISCHARGING_TIME)+"s");
        setTextOnUIThread(myPhoneStats.findViewById(R.id.activeTotalValue),
                (int)(dt.get(StatType.ACTIVE_DISCHARGING_PCT)*100)+"%");

        setTextOnUIThread(myPhoneStats.findViewById(R.id.inactiveAvgTime),
                (int)(100*dt.get(StatType.INACTIVE_DISCHARGING_TIME)/dt.get(StatType.INACTIVE_DISCHARGING_PCT))+"s");
        setTextOnUIThread(myPhoneStats.findViewById(R.id.inactiveTotalTime),
                (int)dt.get(StatType.INACTIVE_DISCHARGING_TIME)+"s");
        setTextOnUIThread(myPhoneStats.findViewById(R.id.inactiveTotalValue),
                (int)(dt.get(StatType.INACTIVE_DISCHARGING_PCT)*100)+"%");
    }

    private void setTextOnUIThread(final View view, final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)view).setText(value);
            }
        });
    }

    public static File getDataFile() {
        return dataFile;
    }
}
