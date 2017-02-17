package fr.unice.polytech.elim.elim9;

import android.content.Intent;
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
import com.google.firebase.database.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //You use in average "applicationNumber" ram, it's more than "applicationPCT" of our others users
    private String ramAverage;
    private String ramPCT;

    private String batterygoodORbad;

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
     * @param value Between 0 and 1 included, else progress will be indeterminate
     */
    public void changeProgressBarValue(double value) {
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.main_prediction_progress);
        final int PRECISION = progressBar.getWidth();

        if(value < 0 || value > 1 || PRECISION == 0) {
            progressBar.setIndeterminate(true);

            if(PRECISION==0) {
                Log.e("MainActivity", "Progressbar width = 0 !!");
            }
        } else {
            progressBar.setIndeterminate(false);
            progressBar.setMax(PRECISION);
            progressBar.setProgress((int) ((value) * PRECISION)); // because progressbar is inverted
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

                        Log.d("applicationNumber", obj.getString("applicationNumber"));
                        Log.d("applicationPCT", obj.getString("applicationPCT"));
                        Log.d("batteryState", obj.getString("batteryState"));
                        Log.d("ramAverage", obj.getString("ramAverage"));
                        Log.d("ramPCT", obj.getString("ramPCT"));


                       applicationNumber = obj.getString("applicationNumber");
                       applicationPCT = obj.getString("applicationPCT");
                       batterygoodORbad = obj.getString("batteryState");
                       ramAverage = obj.getString("ramAverage");
                       ramPCT = obj.getString("ramPCT");

                       ((TextView)  findViewById(R.id.AppsPCT)).setText(applicationPCT);
                       ((TextView)  findViewById(R.id.RamPCT)).setText(ramPCT);
                       ((TextView)  findViewById(R.id.RamAverage)).setText(ramAverage);
                       ((TextView)  findViewById(R.id.Apps)).setText(applicationNumber);
                        changeProgressBarValue(batterygoodORbad.equals("Good")?1:0);
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
