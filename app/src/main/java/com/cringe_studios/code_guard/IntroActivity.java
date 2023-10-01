package com.cringe_studios.code_guard;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cringe_studios.code_guard.databinding.ActivityIntroBinding;
import com.cringe_studios.code_guard.unlock.UnlockActivity;
import com.cringe_studios.code_guard.util.SettingsUtil;

public class IntroActivity extends BaseActivity {

    private ActivityIntroBinding binding;

    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SettingsUtil.isIntroVideoEnabled(this)) {
            openMainActivity();
            return;
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

        binding.videoView.setOnErrorListener((MediaPlayer mp, int what, int extra) -> {
            Toast.makeText(this, R.string.intro_video_failed, Toast.LENGTH_LONG).show();
            openMainActivity();
            return true;
        });

        setContentView(binding.getRoot());
    }

    public void openMainActivity() {
        Intent m = new Intent(getApplicationContext(), UnlockActivity.class);
        m.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(m);
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMediaPlayer != null) mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.videoView.start();
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