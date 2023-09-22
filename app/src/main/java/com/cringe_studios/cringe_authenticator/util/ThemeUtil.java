package com.cringe_studios.cringe_authenticator.util;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.cringe_studios.cringe_authenticator.R;

public class ThemeUtil {

    public static void loadTheme(AppCompatActivity activity) {
        Theme theme = SettingsUtil.getTheme(activity);
        activity.setTheme(theme.getStyle());

        // TODO: use settings
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

}
