package com.cringe_studios.cringe_authenticator.util;

import android.annotation.SuppressLint;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.cringe_studios.cringe_authenticator.R;

public class ThemeUtil {

    public static void loadTheme(AppCompatActivity activity) {
        Theme theme = SettingsUtil.getTheme(activity);
        activity.setTheme(theme.getStyle());

        AppCompatDelegate.setDefaultNightMode(SettingsUtil.getAppearance(activity).getValue());
    }

}
