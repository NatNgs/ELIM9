package fr.unice.polytech.elim9;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by nathael on 29/12/16.
 */

public class BatteryData {
    private final HashMap<StatType, Double> values = new HashMap<>();
    private final long timeStamp;
    private final int level;
    private final int levelScale;
    private final Status status;

    public BatteryData(Intent batteryStatus, boolean isCharging, boolean isScreenOn) {
        timeStamp = Calendar.getInstance().getTimeInMillis();

        level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        levelScale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        Log.d("BatteryData", "Level:"+level+", levelScale:"+levelScale);

        this.status = isCharging?Status.CHARGING:(isScreenOn?Status.ACTIVE:Status.INACTIVE);
    }
    public BatteryData(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);

        JSONObject charging = json.getJSONObject("isCharging");
        JSONObject active = json.getJSONObject("active");
        JSONObject inactive = json.getJSONObject("inactive");

        put(StatType.CHARGING_TIME, charging.getDouble("time"));
        put(StatType.CHARGING_PCT, charging.getDouble("value"));
        put(StatType.ACTIVE_DISCHARGING_TIME, active.getDouble("time"));
        put(StatType.ACTIVE_DISCHARGING_PCT, active.getDouble("value"));
        put(StatType.INACTIVE_DISCHARGING_TIME, inactive.getDouble("time"));
        put(StatType.INACTIVE_DISCHARGING_PCT, inactive.getDouble("value"));
        timeStamp = json.getLong("timestamp");
        levelScale = json.getInt("levelScale");
        level = json.getInt("level");
        status = Status.valueOf(json.getString("status"));
    }

    public double put(StatType key, double value) {
        if(value == 0) {
            if(values.containsKey(key))
                return values.remove(key);
            else
                return 0;
        } else {
            if (values.containsKey(key))
                return values.put(key, value);
            else {
                values.put(key, value);
                return 0;
            }
        }
    }
    public double get(StatType statType) {
        if(values.containsKey(statType)) {
            return values.get(statType);
        }
        return 0;
    }
    public String toJsonString() {
        JSONObject object = new JSONObject();
        try {
            object.accumulate("timestamp", timeStamp);
            object.accumulate("levelScale", levelScale);
            object.accumulate("level", level);
            object.accumulate("status", status);

            JSONObject charging = new JSONObject();
            JSONObject active = new JSONObject();
            JSONObject inactive = new JSONObject();
            object.accumulate("isCharging", charging);
            object.accumulate("active", active);
            object.accumulate("inactive", inactive);

            charging.accumulate("time", get(StatType.CHARGING_TIME));
            charging.accumulate("value", get(StatType.CHARGING_PCT));
            active.accumulate("time", get(StatType.ACTIVE_DISCHARGING_TIME));
            active.accumulate("value", get(StatType.ACTIVE_DISCHARGING_PCT));
            inactive.accumulate("time", get(StatType.INACTIVE_DISCHARGING_TIME));
            inactive.accumulate("value", get(StatType.INACTIVE_DISCHARGING_PCT));

            return object.toString();
        } catch (JSONException e) {
            Log.e("BatteryData#toJsonStr", "JSONException", e);
        }

        return "{}";
    }

    public long getTime() {
        return timeStamp;
    }

    public Status getStatus() {
        return status;
    }

    public int getLevel() {
        return level;
    }

    public int getLevelScale() {
        return levelScale;
    }
}
