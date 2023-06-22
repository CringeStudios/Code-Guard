package com.cringe_studios.cringe_authenticator;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {
    public static boolean show_logoanimation = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences p = getSharedPreferences("appsettings", Activity.MODE_PRIVATE);
        show_logoanimation = p.getBoolean("Logoanimation", true);
        super.onCreate(savedInstanceState);
        if (show_logoanimation) {
            setContentView(R.layout.activity_intro);
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 4000ms
                    Intent m = new Intent(getApplicationContext(), MainActivity.class);
                    m.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(m);
                }
            }, 4000);
        } else {
            Intent m = new Intent(getApplicationContext(), MainActivity.class);
            m.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(m);
        }
    }


}