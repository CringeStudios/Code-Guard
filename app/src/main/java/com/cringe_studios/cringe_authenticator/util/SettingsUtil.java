package com.cringe_studios.cringe_authenticator.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.cringe_studios.cringe_authenticator.OTPData;
import com.cringe_studios.cringe_authenticator.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kotlinx.coroutines.flow.SharedFlow;

public class SettingsUtil {

    public static final Map<String, Integer> THEMES;
    public static final List<String> THEME_NAMES;

    static {
        Map<String, Integer> themes = new LinkedHashMap<>();
        themes.put("Blue/Green", R.style.Theme_CringeAuthenticator_Blue_Green);
        themes.put("Red/Blue", R.style.Theme_CringeAuthenticator_Red_Blue);
        themes.put("Pink/Green", R.style.Theme_CringeAuthenticator_Pink_Green);
        themes.put("Blue/Yellow", R.style.Theme_CringeAuthenticator_Blue_Yellow);
        themes.put("Green/Yellow", R.style.Theme_CringeAuthenticator_Green_Yellow);
        themes.put("Orange/Turquoise", R.style.Theme_CringeAuthenticator_Orange_Turquoise);
        THEMES = Collections.unmodifiableMap(themes);
        THEME_NAMES = Collections.unmodifiableList(new ArrayList<>(THEMES.keySet()));
    }

    public static String
            GROUPS_PREFS_NAME = "groups",
            GENERAL_PREFS_NAME = "general";

    private static final Gson GSON = new Gson();

    public static List<OTPData> getOTPs(SharedPreferences prefs, String group) {
        String currentOTPs = prefs.getString("group." + group, "[]");
        return Arrays.asList(GSON.fromJson(currentOTPs, OTPData[].class));
    }

    public static void addOTP(SharedPreferences prefs, String group, @NonNull OTPData data) {
        List<OTPData> otps = new ArrayList<>(getOTPs(prefs, group));
        otps.add(data);

        prefs.edit()
                .putString("group." + group, GSON.toJson(otps.toArray(new OTPData[0])))
                .apply();
    }

    public static void setEnableIntroVideo(Context ctx, boolean enableIntroVideo) {
        SharedPreferences prefs = ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("enableIntroVideo", enableIntroVideo).apply();
    }

    public static boolean isIntroVideoEnabled(Context ctx) {
        return ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getBoolean("enableIntroVideo", true);
    }

    public static void setTheme(Context ctx, String theme) {
        SharedPreferences prefs = ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("theme", theme).apply();
    }

    public static String getTheme(Context ctx) {
        return ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getString("theme", THEME_NAMES.get(0));
    }

}
