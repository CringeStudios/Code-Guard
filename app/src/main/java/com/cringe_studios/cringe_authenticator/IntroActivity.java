package com.cringe_studios.cringe_authenticator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cringe_studios.cringe_authenticator.databinding.ActivityIntroBinding;

public class IntroActivity extends AppCompatActivity {
    public static boolean show_logoanimation = false;

    private static ActivityIntroBinding binding;

    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences p = getSharedPreferences("appsettings", Activity.MODE_PRIVATE);
        show_logoanimation = p.getBoolean("Logoanimation", true);
        super.onCreate(savedInstanceState);
        if (show_logoanimation) {
            setContentView(R.layout.activity_intro);
        } else {
        }

        binding = ActivityIntroBinding.inflate(getLayoutInflater());

        Uri uri = Uri.parse(String.format("android.resource://%s/%s", getPackageName(), R.raw.intro));
        binding.videoView.setVideoURI(uri);
        binding.videoView.start();

        binding.videoView.setOnPreparedListener(mediaPlayer -> {
            mMediaPlayer = mediaPlayer;
            setDimension();
        });

        binding.videoView.setOnCompletionListener(mp -> openMainActivity());

        setContentView(binding.getRoot());
    }

    public void openMainActivity() {
        Intent m = new Intent(getApplicationContext(), MainActivity.class);
        m.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(m);
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // When the Activity is destroyed, release our MediaPlayer and set it to null.
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    private void setDimension() {
        float videoProportion = (float) mMediaPlayer.getVideoHeight() / mMediaPlayer.getVideoWidth();
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        float screenProportion = (float) screenHeight / (float) screenWidth;
        ViewGroup.LayoutParams lp = binding.videoView.getLayoutParams();

        if (videoProportion < screenProportion) {
            lp.height= screenHeight;
            lp.width = (int) ((float) screenHeight / videoProportion);
        } else {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth * videoProportion);
        }
        binding.videoView.setLayoutParams(lp);
    }


}