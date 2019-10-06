package com.wearemagic.lasalle.one;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;

public class SplashActivity extends AppCompatActivity {

    public String packageName = "com.wearemagic.lasalle.one";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedP = getSharedPreferences(packageName, MODE_PRIVATE);

        int nightMode = sharedP.getInt("nightMode", -1);

        AppCompatDelegate.setDefaultNightMode(nightMode);
        super.onCreate(savedInstanceState);

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
