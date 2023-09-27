package com.cringe_studios.cringe_authenticator;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cringe_studios.cringe_authenticator.unlock.UnlockContract;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;
import com.cringe_studios.cringe_authenticator.util.ThemeUtil;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    private ActivityResultLauncher<Void> startUnlockActivity;

    private Runnable unlockSuccess, unlockFailure;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerCallbacks();

        if(SettingsUtil.isScreenSecurity(this)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }

        ThemeUtil.loadTheme(this);
        setLocale(SettingsUtil.getLocale(this));
    }

    private void registerCallbacks() {
        startUnlockActivity = registerForActivityResult(new UnlockContract(), success -> {
            if(success && unlockSuccess != null) unlockSuccess.run();
            if(!success && unlockFailure != null) unlockFailure.run();
        });
    }

    public void promptUnlock(Runnable success, Runnable failure) {
        unlockSuccess = success;
        unlockFailure = failure;
        startUnlockActivity.launch(null);
    }

    public void setLocale(Locale locale) {
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

}
