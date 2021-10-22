package com.example.a4finder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.util.Objects;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_sreen);

        Objects.requireNonNull(getSupportActionBar()).hide();

        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();

                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    Intent intent = new Intent(SplashScreen.this,MapsActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

        thread.start();
    }
}