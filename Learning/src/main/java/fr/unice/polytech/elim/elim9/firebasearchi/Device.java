package fr.unice.polytech.elim.elim9.firebasearchi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;
import java.util.*;

/**
 * Created by nathael on 17/02/17.
 */
public class Device {
    public static final int VIEW_ALL = 0b1;
    public static final int VIEW_ONLY_LAST = 0b10;
    public static final int VIEW_ONLY_FEED = 0b100;
    public static final int VIEW_NO_FEED = 0b1000;

    private final Set<String> feedDates = new HashSet<>();
    private String lastDate = null;
    private final Map<String, Map<String, Serializable>> timeDependentAttributes = new HashMap<>();
    private final Map<String, Serializable> constantAttributes = new HashMap<>();

    private static int status = VIEW_ALL;


    public Device() {}
    public Device(String json) {
        this();
        fromJson(json);
    }

    public void fromJson(String json) {
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(json).getAsJsonObject();

        JsonObject dates = object.get("date").getAsJsonObject();
        for(Map.Entry<String, JsonElement> date : dates.entrySet()) {
            Map<String, Serializable> map = new HashMap<>();

            JsonObject dateAsObject = date.getValue().getAsJsonObject();
            for(Map.Entry<String, JsonElement> entry : dateAsObject.entrySet()) {
                try {
                    map.put(entry.getKey(), entry.getValue().getAsDouble());
                } catch(ClassCastException ignored) {}
            }

            timeDependentAttributes.put(date.getKey(), map);
        }

        object.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("date"))
                .forEach(entry -> {
            if (entry.getValue().getAsJsonPrimitive().isNumber())
                constantAttributes.put(entry.getKey(), entry.getValue().getAsDouble());
            else
                constantAttributes.put(entry.getKey(), entry.getValue().getAsString());
        });
    }

    public int countDataSnapshot() {
        switch (status) {
            case VIEW_ALL:
                return timeDependentAttributes.size();
            case VIEW_ONLY_FEED:
                return feedDates.size();
            case VIEW_ONLY_LAST:
                return timeDependentAttributes.isEmpty()?0:1;
            default:
                return 0;
        }
    }

    public String getDateAt(int i) {
        switch (status) {
            case VIEW_ALL:
                return new ArrayList<>(timeDependentAttributes.keySet()).get(i);
            case VIEW_ONLY_FEED:
                return new ArrayList<>(feedDates).get(i);
            case VIEW_ONLY_LAST:
                if(i != 0)
                    throw new IndexOutOfBoundsException("i="+i+", count=1");
                return lastDate;
            default:
                return null;
        }
    }

    public Map<String,Serializable> getDataSnapshot(int i) {
        Map<String, Serializable> ret = new HashMap<>(constantAttributes);
        ret.putAll(timeDependentAttributes.get(getDateAt(i)));

        return ret;
    }

    public static void select(int deviceSelectionCriteria) {
        status = deviceSelectionCriteria;
    }
}
