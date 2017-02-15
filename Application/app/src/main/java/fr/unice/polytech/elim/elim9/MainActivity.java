package fr.unice.polytech.elim.elim9;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

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
    }

    private void onActivationSwitchStateChanged(boolean b) {
        Intent serviceIntent = new Intent(this, MonitoringService.class);
        serviceIntent.putExtra(MonitoringService.PARAM_ON_OFF, b);
        startService(serviceIntent);
    }

    public void onClickDisconnect(View view) {
        // TODO
    }
}
