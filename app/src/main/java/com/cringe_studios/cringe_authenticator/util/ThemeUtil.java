package com.cringe_studios.cringe_authenticator.util;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.cringe_studios.cringe_authenticator.R;

public class ThemeUtil {

    public static void loadTheme(AppCompatActivity activity) {
        Theme theme = SettingsUtil.getTheme(activity);
        activity.setTheme(theme.getStyle());

        if(SettingsUtil.isMinimalistThemeEnabled(activity)) {
            activity.getTheme().applyStyle(R.style.Theme_CringeAuthenticator_Minimalist, true);
        }

        AppCompatDelegate.setDefaultNightMode(SettingsUtil.getAppearance(activity).getValue());
    }

    public static void loadBackground(AppCompatActivity activity) {
        if(!SettingsUtil.isThemedBackgroundEnabled(activity)) return;

        Theme theme = SettingsUtil.getTheme(activity);

        int nightMode = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isNightMode;
        switch(nightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
            default:
                isNightMode = false;
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                isNightMode = true;
                break;
        }

        View v = activity.findViewById(R.id.app_background);
        if(v != null) {
            v.setBackgroundResource(!isNightMode ? theme.getLightBackground() : theme.getDarkBackground());
        }
    }

}
