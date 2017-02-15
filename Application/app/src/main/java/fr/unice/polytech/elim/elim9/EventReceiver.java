package fr.unice.polytech.elim.elim9;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;

import java.util.Calendar;

/**
 * Created by nathael on 29/12/16.
 */

public abstract class EventReceiver extends BroadcastReceiver {
    private static boolean isScreenOn;
    private static boolean isCharging;

    @Override
    public void onReceive(Context context, Intent intent) {
        //IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        //Intent batteryStatus = context.registerReceiver(null, ifilter);

        BatteryData dt = new BatteryData(intent, isCharging, isScreenOn);

        try {
            BatteryData last = Util.loadDatatable();

            double spendTime = (Calendar.getInstance().getTimeInMillis() - last.getTime())/1000.;

            double diffPct = dt.getLevel()/(double)dt.getLevelScale() - last.getLevel()/(double)last.getLevelScale();

            if(last.getLevelScale() == -1)
                diffPct = 0;
            else if(diffPct < 0)
                diffPct *= -1;

            Log.i("EventReceiver#onRecieve", "DiffTime="+spendTime+", DiffPct="+diffPct+", dt="+dt.toJsonString()+", last="+last.toJsonString());

            if(last.getStatus()==Status.CHARGING) {
                dt.put(StatType.CHARGING_PCT, last.get(StatType.CHARGING_PCT) + diffPct);
                dt.put(StatType.CHARGING_TIME, last.get(StatType.CHARGING_TIME) + spendTime);
            } else {
                dt.put(StatType.CHARGING_PCT, last.get(StatType.CHARGING_PCT));
                dt.put(StatType.CHARGING_TIME, last.get(StatType.CHARGING_TIME));
            }
            if(last.getStatus()==Status.ACTIVE) {
                dt.put(StatType.ACTIVE_DISCHARGING_PCT, last.get(StatType.ACTIVE_DISCHARGING_PCT) + diffPct);
                dt.put(StatType.ACTIVE_DISCHARGING_TIME, last.get(StatType.ACTIVE_DISCHARGING_TIME) + spendTime);
            } else {
                dt.put(StatType.ACTIVE_DISCHARGING_PCT, last.get(StatType.ACTIVE_DISCHARGING_PCT));
                dt.put(StatType.ACTIVE_DISCHARGING_TIME, last.get(StatType.ACTIVE_DISCHARGING_TIME));
            }
            if(last.getStatus()==Status.INACTIVE) {
                dt.put(StatType.INACTIVE_DISCHARGING_PCT, last.get(StatType.INACTIVE_DISCHARGING_PCT) + diffPct);
                dt.put(StatType.INACTIVE_DISCHARGING_TIME, last.get(StatType.INACTIVE_DISCHARGING_TIME) + spendTime);
            } else {
                dt.put(StatType.INACTIVE_DISCHARGING_PCT, last.get(StatType.INACTIVE_DISCHARGING_PCT));
                dt.put(StatType.INACTIVE_DISCHARGING_TIME, last.get(StatType.INACTIVE_DISCHARGING_TIME));
            }
        } catch (JSONException ignored) {}

        Util.storeDatatable(dt);
    }

    public static void setCharging(boolean isCharging) {
        EventReceiver.isCharging = isCharging;
    }
    public static void setScreenOn(boolean isScreenOn) {
        EventReceiver.isScreenOn = isScreenOn;
    }
}
