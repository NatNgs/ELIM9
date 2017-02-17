package fr.unice.polytech.elim.elim9.firebasearchi;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import jdk.nashorn.internal.parser.JSONParser;

import java.io.Serializable;
import java.util.*;

/**
 * Created by nathael on 17/02/17.
 */
public class Users {
    private final Map<String, User> users = new HashMap<>();

    public Users() {}
    public Users(String json) {
        this();
        fromJson(json);
    }

    public void fromJson(String json) {
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(json).getAsJsonObject();

        for(Map.Entry<String, JsonElement> entry : object.entrySet()) {
            users.put(entry.getKey(), new User(entry.getValue().toString()));
        }
    }

    public int countDataSnapshot() {
        int i = 0;
        for(User u : users.values())
            i += u.countDataSnapshot();
        return i;
    }

    public Map<String,Serializable> getDataSnapshot(int i) {
        if(i<0 || i >= countDataSnapshot())
            return null;

        for(User u : users.values()) {
            int uCount = u.countDataSnapshot();
            if(uCount < i)
                i-= uCount;
            else
                return u.getDataSnapshot(i);
        }

        return null;
    }

}
