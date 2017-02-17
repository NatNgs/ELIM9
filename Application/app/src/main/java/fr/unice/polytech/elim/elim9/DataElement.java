package fr.unice.polytech.elim.elim9;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.unice.polytech.elim.elim9.DataElement.DataKind.chActPct;
import static fr.unice.polytech.elim.elim9.DataElement.DataKind.chActTime;
import static fr.unice.polytech.elim.elim9.DataElement.DataKind.chInactPct;
import static fr.unice.polytech.elim.elim9.DataElement.DataKind.chInactTime;
import static fr.unice.polytech.elim.elim9.DataElement.DataKind.dischActPct;
import static fr.unice.polytech.elim.elim9.DataElement.DataKind.dischActTime;
import static fr.unice.polytech.elim.elim9.DataElement.DataKind.dischInactPct;
import static fr.unice.polytech.elim.elim9.DataElement.DataKind.dischInactTime;
import static fr.unice.polytech.elim.elim9.DataElement.DataKind.ramUsage;

/**
 * Created by nathael on 16/02/17.
 */

public class DataElement {
    private static final long MAX_DIFF_TIME = 1000L*60*60*24*7; // 7 days in ms
    public static DataElement instance = null;
    private static final double MIN_PCT = 0.05;

    private final Map<DataKind, List<PairDateValue>> dataArrays = new HashMap<>();
    private final Map<String, Double> lastValues = new HashMap<>();

    public static DataElement getInstance(){
        if(instance == null){
            instance = new DataElement();
        }
        return instance;
    }

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
        double pct = count(dischActPct);

        Log.e("ELIM9DataElement","dischActTime:"+dataArrays.get(dischActTime));
        Log.e("ELIM9DataElement","dischActPct:"+dataArrays.get(dischActPct));
        Log.e("ELIM9DataElement","time="+time+", pct="+pct+", will return "+((time/10)/pct));

        if(time == 0 || Math.abs(pct) <= MIN_PCT) {
            return 0;
        }
        return (time)/pct; // ((time/1000 > seconds)/ %)*100 > s/100%
    }

    /**
     *
     * @return s/100% value
     */
    private double getDischargeInactiveAvgTime() {
        double time = count(dischInactTime);
        double pct = count(dischInactPct);

        Log.e("ELIM9DataElement","dischInactTime:"+dataArrays.get(dischInactTime));
        Log.e("ELIM9DataElement","dischInactPct:"+dataArrays.get(dischInactPct));
        Log.e("ELIM9DataElement","time="+time+", pct="+pct+", will return "+((time/10)/pct));

        if(time == 0 || Math.abs(pct) <= MIN_PCT) {
            return 0;
        }
        return (time)/pct; // ((time > seconds)/ %)*100 > s/100%
    }

    /**
     *
     * @return s/100% value
     */
    private double getChargeActiveAvgTime() {
        double time = count(chActTime);
        double pct = count(chActPct);

        Log.e("ELIM9DataElement","chActTime:"+dataArrays.get(chActTime));
        Log.e("ELIM9DataElement","chActPct:"+dataArrays.get(chActPct));
        Log.e("ELIM9DataElement","time="+time+", pct="+pct+", will return "+((time)/pct));


        if(time == 0 || Math.abs(pct) <= MIN_PCT) {
            return 0;
        }
        return (time)/pct; // ((time > seconds)/ %)*100 > s/100%
    }
    /**
     *
     * @return s/100% value
     */
    private double getChargeInactiveAvgTime () {
        double time = count(chInactTime);
        double pct = count(chInactPct);

        Log.e("ELIM9DataElement","chInactTime:"+dataArrays.get(chInactTime));
        Log.e("ELIM9DataElement","chInactPct:"+dataArrays.get(chInactPct));
        Log.e("ELIM9DataElement","time="+time+", pct="+pct+", will return "+((time/10)/pct));

        if(time == 0 || Math.abs(pct) <= MIN_PCT) {
            return 0;
        }
        return (time)/pct; // ((time > seconds)/ %)*100 > s/100%
    }
    /**
     *
     * @return s/100% value
     */
    private double getAvgRamUsage() {
        if (dataArrays.get(ramUsage).size() == 0)
            return 0;

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
    public void putRam(long time, long ram) {
        List<PairDateValue> ramArray = dataArrays.get(ramUsage);
        ramArray.add(new PairDateValue(time, ram));
        removeOld(ramArray, time);
    }

    private void putTimeChargeData(long time, double charge, List<PairDateValue> timeArray, List<PairDateValue> pctArray) {
        if ((lastValues.containsKey("time") && lastValues.containsKey("chargeLevel"))) {
            double lastTime = lastValues.get("time");
            double lastCharge = lastValues.get("chargeLevel");

            Log.e("ELIM9DataElement", "Charge="+charge+", lastChargeWas="+lastCharge);
            if(charge != lastCharge) {
                pctArray.add(new PairDateValue(time,charge - lastCharge));
                Log.e("ELIM9DataElement", "Added pairDateValue:"+time+", "+(charge-lastCharge));
            }
            if(time != lastTime) {
                timeArray.add(new PairDateValue(time, time - lastTime));
            }

            removeOld(timeArray, time);
            removeOld(pctArray, time);
        }

        lastValues.put("time", (double)time);
        lastValues.put("chargeLevel", charge);
    }

    private void removeOld(List<PairDateValue> array, long date) {
        while(array.size() > 0 && array.get(0).date + MAX_DIFF_TIME < date) {
            Log.e("ELIM9DataElement", "Removed Old:"+array.get(0).date+" + "+MAX_DIFF_TIME+" = "+(array.get(0).date+MAX_DIFF_TIME)+" < "+date);
            array.remove(0);
        }
    }

    public Map<String, Object> toMap() {
        Map<String,Object> ret = new HashMap<>();

        long value = (long)getChargeActiveAvgTime();
        if(value != 0)
            ret.put("chargeActive", value);

        value = (long)getChargeInactiveAvgTime();
        if(value != 0)
            ret.put("chargeInactive", value);

        value = (long)getDischargeActiveAvgTime();
        if(value != 0)
            ret.put("dischargeActive", value);

        value = (long)getDischargeInactiveAvgTime();
        if(value != 0)
            ret.put("dischargeInactive", value);

        value = (long)getAvgRamUsage();
        if(value != 0)
            ret.put("ramUsage", value);

        return ret;
    }

    public static void save(final String SAVE_FILE_NAME) {
        File f = new File(SAVE_FILE_NAME);
        try {
            if (!f.exists() || f.delete())
                f.createNewFile();

            String json = toSaveStr(getInstance().dataArrays);
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(json);
            Log.e("ELIM9DataElement", "Saved: "+json);
            oos.close();
            fos.close();
        } catch (IOException e) {
            Log.e("ELIM9DataElement", "Save:"+e.getMessage(), e);
        }
    }

    public static void load(final String SAVE_FILE_NAME) {
        File f = new File(SAVE_FILE_NAME);
        try {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            String json = (String) ois.readObject();
            Log.e("ELIM9DataElement", "Loaded: "+json);

            Map<DataKind, List<PairDateValue>> map = fromSaveStr(json);
            if (map != null) {
                getInstance().dataArrays.clear();
                getInstance().dataArrays.putAll(map);
            }

            ois.close();
            fis.close();
        } catch (Exception e) {
            Log.d("ELIM9DataElement", "Cannot load: "+e.toString(), e);
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
        private long date; // miliseconds
        private final double value;

        private PairDateValue(long date, double value) {
            this.date = date;
            this.value = value;
        }

        @Override
        public String toString() {
            return "["+date+","+value+"]";
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
