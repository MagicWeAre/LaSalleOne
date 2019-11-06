package com.wearemagic.lasalle.one;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.r0adkll.slidr.Slidr;

public class SettingsActivity extends AppCompatActivity {

    public String packageName = "com.wearemagic.lasalle.one";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedP = getSharedPreferences(packageName, MODE_PRIVATE);

        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Switch darkModeSwitch = findViewById(R.id.darkModeSwitch);
        Slidr.attach(this);

        int nightMode = sharedP.getInt("nightMode", -1);

        if (nightMode == 2){
            darkModeSwitch.setChecked(true);
        } else {
            darkModeSwitch.setChecked(false);
        }

        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedP = getSharedPreferences(packageName, MODE_PRIVATE);
                int nightMode;

                if (isChecked) {
                    nightMode = 2;
                } else {
                    nightMode = -1;
                }

                sharedP.edit().putInt("nightMode", nightMode).apply();
                Toast.makeText(getApplicationContext(), getString(R.string.toast_dark_mode), Toast.LENGTH_SHORT).show();
                AppCompatDelegate.setDefaultNightMode(nightMode);
            }
        });
    }

}
