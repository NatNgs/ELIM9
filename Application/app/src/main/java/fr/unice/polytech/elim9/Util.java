package fr.unice.polytech.elim9;

import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by nathael on 29/12/16.
 */

public class Util {
    public static BatteryData loadDatatable() throws JSONException {
        try {
            FileInputStream inputStream = new FileInputStream(Activity1.getDataFile());
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            BufferedReader bufferedReader = new BufferedReader(reader);

            StringBuilder stringBuilder = new StringBuilder();
            String receiveString = "";
            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(receiveString);
            }

            inputStream.close();

            BatteryData dt = new BatteryData(stringBuilder.toString());
            Log.d("LoadDatatable", "Successfully loaded !");
            return dt;
        } catch (IOException e) {
            Log.e("LoadDatatable", "Impossible to load", e);
        }
        return null;
    }

    public static void storeDatatable(BatteryData dt) {
        File dataFile = Activity1.getDataFile();

        try {
            FileOutputStream outputStream = new FileOutputStream(dataFile);
            outputStream.write(dt.toJsonString().getBytes());
            outputStream.close();
            Log.d("StoreDatatable", "Successfully stored !");
        } catch (IOException e) {
            Log.e("StoreDatatable", "Impossible to store", e);
        }
    }
}
