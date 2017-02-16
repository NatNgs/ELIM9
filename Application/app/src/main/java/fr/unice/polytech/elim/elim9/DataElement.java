package fr.unice.polytech.elim.elim9;

import android.util.Log;
import android.widget.SectionIndexer;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

import static fr.unice.polytech.elim.elim9.DataElement.DataKind.*;

/**
 * Created by nathael on 16/02/17.
 */

public class DataElement {
    private static final long MAX_DIFF_TIME = 1000L*60*60*24*7;
    public static final DataElement instance = new DataElement();

    private final Map<DataKind, List<PairDateValue>> dataArrays = new HashMap<>();
    private final Map<String, Double> lastValues = new HashMap<>();

    private DataElement() {
        for(DataKind dk : DataKind.values())
            dataArrays.put(dk, new ArrayList<PairDateValue>());
    }

    private double count(DataKind kind) {
        double value = 0;
        for(PairDateValue d : dataArrays.get(kind)) {
            value += d.value;
        }
        return value;
    }
    /**
     *
     * @return s/100% value
     */
    private double getDischargeActiveAvgTime() {
        double time = count(dischActTime);
        double pct = count(dischActPct)*100;

        if(time == 0 || pct == 0) {
            return -1;
        }
        return time/pct;
    }

    /**
     *
     * @return s/100% value
     */
    private double getDischargeInactiveAvgTime() {
        double time = count(dischInactTime);
        double pct = count(dischInactPct)*100;

        if(time == 0 || pct == 0) {
            return -1;
        }
        return time/pct;
    }

    /**
     *
     * @return s/100% value
     */
    private double getChargeActiveAvgTime() {
        double time = count(chActTime);
        double pct = count(chActPct)*100;

        if(time == 0 || pct == 0) {
            return -1;
        }
        return time/pct;
    }
    /**
     *
     * @return s/100% value
     */
    private double getChargeInactiveAvgTime() {
        double time = count(chInactTime);
        double pct = count(chInactPct)*100;

        if(time == 0 || pct == 0) {
            return -1;
        }
        return time/pct;
    }
    /**
     *
     * @return s/100% value
     */
    private double getAvgRamUsage() {
        if (dataArrays.get(ramUsage).size() == 0)
            return -1;

        double ram = 0;

        for (PairDateValue d : dataArrays.get(ramUsage))
            ram += d.value;

        return ram / dataArrays.get(ramUsage).size();
    }

    public void putDischargeActive(long time, int pct, int pctMax) {
        putTimeChargeData(time, (double)pct/(double)pctMax, dataArrays.get(dischActTime), dataArrays.get(dischActPct));
    }
    public void putChargeActive(long time, int pct, int pctMax) {
        putTimeChargeData(time, (double)pct/(double)pctMax, dataArrays.get(chActTime), dataArrays.get(chActPct));
    }
    public void putDischargeInactive(long time, int pct, int pctMax) {
        putTimeChargeData(time, (double)pct/(double)pctMax, dataArrays.get(dischInactTime), dataArrays.get(dischInactPct));
    }
    public void putChargeInactive(long time, int pct, int pctMax) {
        putTimeChargeData(time, (double)pct/(double)pctMax, dataArrays.get(chInactTime), dataArrays.get(chInactPct));
    }

    private void putTimeChargeData(long time, double charge, List<PairDateValue> timeArray, List<PairDateValue> pctArray) {
        if ((lastValues.containsKey("time") && lastValues.containsKey("chargeLevel") && lastValues.containsKey("chargeLevelScale"))) {
            double lastTime = lastValues.get("time");
            double lastCharge = lastValues.get("chargeLevel");


            if(charge != lastCharge) {
                pctArray.add(new PairDateValue(time,charge - lastCharge));
            }
            if(time != lastTime) {
                timeArray.add(new PairDateValue(time, time - lastTime));
            }

            removeOld(timeArray, time);
            removeOld(pctArray, time);
        }

        lastValues.put("time", time/1000.);
        lastValues.put("chargeLevel", charge);
    }

    private void removeOld(List<PairDateValue> array, long date) {
        while(array.size() > 0 && array.get(0).date + MAX_DIFF_TIME < date) {
            array.remove(0);
        }
    }


    public Map<String, Object> toMap() {
        Map<String,Object> ret = new HashMap<>();

        ret.put("chargeActive", getChargeActiveAvgTime());
        ret.put("chargeInactive", getChargeInactiveAvgTime());
        ret.put("dischargeActive", getDischargeActiveAvgTime());
        ret.put("dischargeInactive", getDischargeInactiveAvgTime());
        ret.put("ramUsage", getAvgRamUsage());

        return ret;
    }

    public static void save(final String SAVE_FILE_NAME) {
        File f = new File(SAVE_FILE_NAME);
        try {
            if (!f.exists() || f.delete())
                f.createNewFile();

            String json = toSaveStr(instance.dataArrays);
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(json);
            Log.e("DataElement", "Saved: "+json);
            oos.close();
            fos.close();
        } catch (IOException e) {
            Log.e("DataElement", "Save:"+e.getMessage(), e);
        }
    }

    public static void load(final String SAVE_FILE_NAME) {
        File f = new File(SAVE_FILE_NAME);
        try {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            String json = (String) ois.readObject();
            Log.e("DataElement", "Loaded: "+json);

            Map<DataKind, List<PairDateValue>> map = fromSaveStr(json);
            if (map != null) {
                instance.dataArrays.clear();
                instance.dataArrays.putAll(map);
            }

            ois.close();
            fis.close();
        } catch (Exception e) {
            Log.d("DataElement", "Cannot load: "+e.toString(), e);
        }
    }


    enum DataKind implements Serializable {
        dischActTime,
        dischInactTime,
        chActTime,
        chInactTime,
        dischActPct,
        dischInactPct,
        chActPct,
        chInactPct,
        ramUsage
    }

    public static class PairDateValue {
        private long date;
        private final double value;

        private PairDateValue(long date, double value) {
            this.date = date;
            this.value = value;
        }
    }

    public static String toSaveStr(Map<DataKind, List<PairDateValue>> dataArrays) {
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<DataKind, List<PairDateValue>> entry : dataArrays.entrySet()) {
            if(!entry.getValue().isEmpty()) {
            sb.append(entry.getKey().name()).append(":");

                for(PairDateValue pair : entry.getValue()) {
                    sb.append(pair.date).append("/").append(pair.value).append(",");
                }

                sb.deleteCharAt(sb.length()-1);
                sb.append(";");
            }
        }

        return sb.toString();
    }

    public static Map<DataKind, List<PairDateValue>> fromSaveStr(String saveStr) {
        Map<DataKind, List<PairDateValue>> map = new HashMap<>();
        for(DataKind kind : DataKind.values()) {
            map.put(kind, new ArrayList<PairDateValue>());
        }

        String[] entries = saveStr.split(";");
        for(String entry : entries) {
            if(!entry.isEmpty()) {
                String[] splitted = entry.split(":");
                try {
                    DataKind kind = DataKind.valueOf(splitted[0]);
                    String[] pairs = splitted[1].split(",");
                    for (String pair : pairs) {
                        String[] pairSplitted = pair.split("/");
                        PairDateValue pdv = new PairDateValue(Long.valueOf(pairSplitted[0]), Double.valueOf(pairSplitted[1]));
                        map.get(kind).add(pdv);
                    }
                } catch(IllegalArgumentException ignored) {} // when DataKind.valueOf fail
            }
        }
        return map;
    }
}
