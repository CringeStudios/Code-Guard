package com.cringe_studios.cringe_authenticator.util;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.cringe_studios.cringe_authenticator.R;

public class ThemeUtil {

    public static void loadTheme(AppCompatActivity activity) {
        Theme theme = SettingsUtil.getTheme(activity);
        activity.setTheme(theme.getStyle());

        AppCompatDelegate.setDefaultNightMode(SettingsUtil.getAppearance(activity).getValue());
    }

    public static void loadBackground(AppCompatActivity activity) {
        Theme theme = SettingsUtil.getTheme(activity);
        Appearance appearance = SettingsUtil.getAppearance(activity);

        View v = activity.findViewById(R.id.app_background);
        if(v != null) {
            v.setBackgroundResource(appearance == Appearance.LIGHT ? theme.getLightBackground() : theme.getDarkBackground());
        }
    }

}
