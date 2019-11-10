package com.wearemagic.lasalle.one;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.r0adkll.slidr.Slidr;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Slidr.attach(this);
    }
}
