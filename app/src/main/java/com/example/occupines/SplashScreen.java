package com.example.occupines;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        onAppStart();
    }

    protected void onAppStart() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }, 3000);
    }
}