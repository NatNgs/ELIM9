package fr.unice.polytech.elim.elim9;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static fr.unice.polytech.elim.elim9.DataElement.DataKind.*;

/**
 * Created by nathael on 16/02/17.
 */

public class DataElement {
    private static final int NB_DATA = 10;
    private static final Map<DataKind, ArrayList<Double>> dataArrays = new HashMap<>();

    private double count(DataKind kind) {
        double value = 0;
        for(double d : dataArrays.get(kind)) {
            value += d;
        }
        return value;
    }
    /**
     *
     * @return s/100% value
     */
    public double getBatteryDischargeActiveAvgTime() {
        double time = count(dischActTime);
        double pct = count(dischActPct);

        if(time == 0 || pct == 0) {
            return -1;
        }
        return time/pct;
    }

    /**
     *
     * @return s/100% value
     */
    public double getBatteryDischargeInactiveAvgTime() {
        double time = count(dischInactTime);
        double pct = count(dischInactPct);

        if(time == 0 || pct == 0) {
            return -1;
        }
        return time/pct;
    }

    /**
     *
     * @return s/100% value
     */
    public double getBatteryChargeActiveAvgTime() {
        double time = count(chActTime);
        double pct = count(chActPct);

        if(time == 0 || pct == 0) {
            return -1;
        }
        return time/pct;
    }
    /**
     *
     * @return s/100% value
     */
    public double getBatteryChargeInactiveAvgTime() {
        double time = count(chInactTime);
        double pct = count(chInactPct);

        if(time == 0 || pct == 0) {
            return -1;
        }
        return time/pct;
    }
    /**
     *
     * @return s/100% value
     */
    public double getAvgRamUsage() {
        if (dataArrays.get(ramUsage).size() == 0)
            return -1;

        double ram = 0;

        for (double d : dataArrays.get(ramUsage))
            ram += d;

        return ram / dataArrays.get(ramUsage).size();
    }

    public void putDischargeActive(double time, int pct, int pctMax) {
        ArrayList<Double> timeArray = dataArrays.get(dischActTime);
        ArrayList<Double> pctArray = dataArrays.get(dischActPct);

        timeArray.add((double)pct/(double)pctMax);
        timeArray.add(time);

        if(timeArray.size() > NB_DATA)
            timeArray.remove(0);

        if(pctArray.size() > NB_DATA)
            pctArray.remove(0);
    }
    public void putChargeActive(double time, int pct, int pctMax) {
        ArrayList<Double> timeArray = dataArrays.get(chActTime);
        ArrayList<Double> pctArray = dataArrays.get(chActPct);

        timeArray.add((double)pct/(double)pctMax);
        timeArray.add(time);

        if(timeArray.size() > NB_DATA)
            timeArray.remove(0);

        if(pctArray.size() > NB_DATA)
            pctArray.remove(0);
    }
    public void putDischargeInactive(double time, int pct, int pctMax) {
        ArrayList<Double> timeArray = dataArrays.get(dischInactTime);
        ArrayList<Double> pctArray = dataArrays.get(dischInactPct);

        timeArray.add((double)pct/(double)pctMax);
        timeArray.add(time);

        if(timeArray.size() > NB_DATA)
            timeArray.remove(0);

        if(pctArray.size() > NB_DATA)
            pctArray.remove(0);
    }
    public void putChargeInactive(double time, int pct, int pctMax) {
        ArrayList<Double> timeArray = dataArrays.get(chInactTime);
        ArrayList<Double> pctArray = dataArrays.get(chInactPct);

        timeArray.add((double)pct/(double)pctMax);
        timeArray.add(time);

        if(timeArray.size() > NB_DATA)
            timeArray.remove(0);

        if(pctArray.size() > NB_DATA)
            pctArray.remove(0);
    }


    enum DataKind {
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
}
