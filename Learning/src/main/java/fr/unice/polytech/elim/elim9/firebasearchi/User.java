package fr.unice.polytech.elim.elim9.firebasearchi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nathael on 17/02/17.
 */
public class User {
    private Map<String, Device> devices = new HashMap<>();

    private final String address;

    public User(String address, String json) {
        this.address = address;
        fromJson(json);
    }


    public void fromJson(String json) {
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(json).getAsJsonObject();

        for(Map.Entry<String, JsonElement> entry : object.entrySet()) {
            devices.put(entry.getKey(), new Device(address+"/"+entry.getKey(), entry.getValue().toString()));
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
            if(dCount <= i)
                i-= dCount;
            else
                return d.getDataSnapshot(i);
        }

        return null;
    }

    public String getAddressAt(int i) {
        if(i<0 || i >= countDataSnapshot())
            return null;

        for(Device d : devices.values()) {
            int dCount = d.countDataSnapshot();
            if(dCount < i)
                i-= dCount;
            else
                return d.getAddressAt(i);
        }

        return null;
    }
}
