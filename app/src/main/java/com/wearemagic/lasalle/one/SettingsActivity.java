package com.wearemagic.lasalle.one;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.r0adkll.slidr.Slidr;

public class SettingsActivity extends AppCompatActivity {

    public String packageName = "com.wearemagic.lasalle.one";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences sharedP = getSharedPreferences(packageName, MODE_PRIVATE);
        int nightMode = sharedP.getInt("nightMode", -1);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Switch darkModeSwitch = findViewById(R.id.darkModeSwitch);
        Slidr.attach(this);

        if (nightMode == 2){
            darkModeSwitch.setChecked(true);
        } else {
            darkModeSwitch.setChecked(false);
        }

        darkModeSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                int localNightMode;

                if (isChecked) {
                    localNightMode = 2;
                } else {
                    localNightMode = -1;
                }

                sharedP.edit().putInt("nightMode", localNightMode).apply();
                Toast.makeText(getApplicationContext(), getString(R.string.toast_dark_mode), Toast.LENGTH_SHORT).show();
                AppCompatDelegate.setDefaultNightMode(localNightMode);
        });
    }

}
