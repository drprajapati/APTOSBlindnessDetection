package com.dprajapati.android.aptosblindnessdetection.splash;

import android.os.Bundle;

import com.dprajapati.android.aptosblindnessdetection.MainActivity;
import com.dprajapati.android.aptosblindnessdetection.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SplashTheme);
        startActivity(MainActivity.getIntent(this));
        finish();
    }

}
