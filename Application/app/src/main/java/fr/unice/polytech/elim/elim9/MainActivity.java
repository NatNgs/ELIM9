package fr.unice.polytech.elim.elim9;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    //You use in average "applicationNumber" ram, it's more than "applicationPCT" of our others users
    private String ramAverage;
    private String ramPCT;

    private double good;
    private double bad;

    //You have "applicationNumber" applications installed, it's more than "applicationPCT" of our others users
    private String applicationNumber;
    private String applicationPCT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((Switch)findViewById(R.id.main_monitoring_activation_switch))
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        onActivationSwitchStateChanged(b);
                    }
                }
        );

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            final String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            ((TextView) findViewById(R.id.main_id_value)).setText(id);
        } else {
            ((TextView) findViewById(R.id.main_id_value)).setText(R.string.disconnected_status);
        }
        listenResult();

    }

    private void onActivationSwitchStateChanged(boolean b) {
        Log.d("ELIM9MainActivity","Activation state set to "+b);
        Intent serviceIntent = new Intent(this, MonitoringService.class);
        if(b) {
            startService(serviceIntent);
        } else {
            stopService(serviceIntent);
        }
    }

    public void onClickDisconnect(View view) {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    /**
     *
     * @param good Between 0 and 1 included, else progress will be indeterminate
     * @param bad Between 0 and 1 included, else progress will be indeterminate
     */
    public void changeProgressBarValue(final double good, final double bad) {
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.main_prediction_progress);
        TextView textValue = (TextView)findViewById(R.id.main_prediction_value);
        final int PRECISION = progressBar.getWidth();

        final double value = (good==bad)?50:good / (good + bad);

        if(value < 0 || value > 1 || PRECISION == 0) {
            progressBar.setIndeterminate(true);
            textValue.setText("Waiting for more monitored Data...");

            if(PRECISION==0) {
                Log.e("MainActivity", "Progressbar width = 0 !!");
            }
        } else {
            progressBar.setIndeterminate(false);
            progressBar.setMax(PRECISION);
            progressBar.setProgress((int) (value * PRECISION)); // because progressbar is inverted
            textValue.setText("Good chance: "+((int)(good*1000)/10.)+"%, Bad chance: "+(int)(bad*1000)/10.+"%; Total chance: "+((int)(value*10000)/100.)+"% good");
        }
    }

    public void listenResult(){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference dbRef = database.getReference().child("results").child(id).child(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!= null) {
                    Log.d("MainActivityResult : ", dataSnapshot.toString());

                    Log.d("Test", dataSnapshot.getValue().toString());
                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(dataSnapshot.getValue().toString());

                        List<ApplicationInfo> appList = getApplicationContext().getPackageManager().getInstalledApplications(0);
                        applicationNumber = ""+appList.size();

                        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();

                        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
                        activityManager.getMemoryInfo(mi);
                        ramAverage = "" + (mi.totalMem - mi.availMem);

                        ramPCT = ""+(int)(obj.getDouble("ramPct")*10000)/100.;
                        applicationPCT = ""+(int)(obj.getDouble("appPct")*10000)/100.;
                        good = obj.getDouble("good");
                        bad = obj.getDouble("bad");

                        ((TextView)  findViewById(R.id.AppsPCT)).setText(applicationPCT+"%");
                        ((TextView)  findViewById(R.id.RamPCT)).setText(ramPCT+"%");
                        ((TextView)  findViewById(R.id.RamAverage)).setText(ramAverage);
                        ((TextView)  findViewById(R.id.Apps)).setText(applicationNumber);

                        changeProgressBarValue(good, bad);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
