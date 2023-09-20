package com.cringe_studios.cringe_authenticator.util;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.cringe_studios.cringe_authenticator.R;

public class ThemeUtil {

    public static void loadTheme(AppCompatActivity activity) {
        Integer themeID = SettingsUtil.THEMES.get(SettingsUtil.getTheme(activity));
        if(themeID != null) {
            activity.setTheme(themeID);
        }else {
            activity.setTheme(R.style.Theme_CringeAuthenticator_Blue_Green);
        }

        // TODO: use settings
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

}
