package com.cringe_studios.code_guard.util;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.cringe_studios.code_guard.R;

public class ThemeUtil {

    public static void loadTheme(AppCompatActivity activity) {
        Theme theme = SettingsUtil.getTheme(activity);
        activity.setTheme(theme.getStyle());

        if(SettingsUtil.isMinimalistThemeEnabled(activity)) {
            activity.getTheme().applyStyle(R.style.Theme_CringeAuthenticator_Minimalist, true);
        }

        AppCompatDelegate.setDefaultNightMode(SettingsUtil.getAppearance(activity).getValue());
    }

    @DrawableRes
    public static int getBackground(Context context) {
        if(!SettingsUtil.isThemedBackgroundEnabled(context)) return 0;

        Theme theme = SettingsUtil.getTheme(context);

        int nightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
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

        return !isNightMode ? theme.getLightBackground() : theme.getDarkBackground();
    }

    public static void loadBackground(AppCompatActivity activity) {
        if(!SettingsUtil.isThemedBackgroundEnabled(activity)) return;

        int background = getBackground(activity);
        View v = activity.findViewById(R.id.app_background);
        if(v != null) {
            v.setBackgroundResource(background);
        }
    }

}
