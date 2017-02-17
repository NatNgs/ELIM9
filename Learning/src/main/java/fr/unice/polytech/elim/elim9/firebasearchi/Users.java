package fr.unice.polytech.elim.elim9.firebasearchi;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.stream.JsonReader;
import jdk.nashorn.internal.parser.JSONParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nathael on 17/02/17.
 */
public class Users extends HashMap<String, User> {

    public void fromJson(String json) {
        JsonReader reader = new JsonReader();



    }


}
