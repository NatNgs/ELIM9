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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

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
            progressBar.setProgress((int) ((1 - value) * PRECISION)); // because progressbar is inverted
            progressBar.setMax(PRECISION);
        }
    }

    public Map<String, String> getResult(){
        Map<String, String> res = new HashMap<>();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference dbRef = database.getReference().child("results").child(id).child(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));




        return res;
    }
}
