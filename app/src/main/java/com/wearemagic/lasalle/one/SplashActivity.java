package com.wearemagic.lasalle.one;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;

public class SplashActivity extends AppCompatActivity {

    public String packageName = "com.wearemagic.lasalle.one";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedP = getSharedPreferences(packageName, MODE_PRIVATE);
        int nightMode = sharedP.getInt("nightMode", -1);

        int currentNightMode = AppCompatDelegate.getDefaultNightMode();

        if (currentNightMode != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode);
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();


        super.onCreate(savedInstanceState);
    }
}
