package fr.unice.polytech.elim.elim9.firebasearchi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nathael on 17/02/17.
 */
public class User {
    private Map<String, Device> devices = new HashMap<>();

    public User() {}
    public User(String json) {
        this();
        fromJson(json);
    }


    public void fromJson(String json) {
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(json).getAsJsonObject();

        for(Map.Entry<String, JsonElement> entry : object.entrySet()) {
            devices.put(entry.getKey(), new Device(entry.getValue().toString()));
        }
    }


    public int countDataSnapshot() {
        int i = 0;
        for(Device d : devices.values())
            i += d.countDataSnapshot();
        return i;
    }

    public Map<String, Serializable> getDataSnapshot(int i) {
        if(i<0 || i >= countDataSnapshot())
            return null;

        for(Device d : devices.values()) {
            int dCount = d.countDataSnapshot();
            if(dCount < i)
                i-= dCount;
            else
                return d.getDataSnapshot(i);
        }

        return null;
    }
}
