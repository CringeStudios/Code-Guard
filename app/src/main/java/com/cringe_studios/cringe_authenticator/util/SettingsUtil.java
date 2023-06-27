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

    public static List<String> getGroups(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(GROUPS_PREFS_NAME, Context.MODE_PRIVATE);
        return Arrays.asList(GSON.fromJson(prefs.getString("groups", "[]"), String[].class));
    }

    public static void addGroup(Context ctx, String group) {
        List<String> groups = new ArrayList<>(getGroups(ctx));
        groups.add(group);

        SharedPreferences prefs = ctx.getSharedPreferences(GROUPS_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("groups", GSON.toJson(groups)).apply();
    }

    public static void removeGroup(Context ctx, String group) {
        List<String> groups = new ArrayList<>(getGroups(ctx));
        groups.remove(group);

        SharedPreferences prefs = ctx.getSharedPreferences(GROUPS_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("groups", GSON.toJson(groups)).apply();

        deleteOTPs(ctx, group);
    }

    public static List<OTPData> getOTPs(Context ctx, String group) {
        String currentOTPs = ctx.getSharedPreferences(GROUPS_PREFS_NAME, Context.MODE_PRIVATE).getString("group." + group, "[]");
        return Arrays.asList(GSON.fromJson(currentOTPs, OTPData[].class));
    }

    public static void addOTP(Context ctx, String group, @NonNull OTPData data) {
        List<OTPData> otps = new ArrayList<>(getOTPs(ctx, group));
        otps.add(data);

        ctx.getSharedPreferences(GROUPS_PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putString("group." + group, GSON.toJson(otps.toArray(new OTPData[0])))
                .apply();
    }

    private static void deleteOTPs(Context ctx, String group) {
        ctx.getSharedPreferences(GROUPS_PREFS_NAME, Context.MODE_PRIVATE).edit()
                .remove("group." + group)
                .apply();
    }

    public static void setEnableIntroVideo(Context ctx, boolean enableIntroVideo) {
        SharedPreferences prefs = ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("enableIntroVideo", enableIntroVideo).apply();
    }

    public static boolean isIntroVideoEnabled(Context ctx) {
        return ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getBoolean("enableIntroVideo", true);
    }

    public static void setBiometricLock(Context ctx, boolean biometricLock) {
        SharedPreferences prefs = ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("biometricLock", biometricLock).apply();
    }

    public static boolean isBiometricLock(Context ctx) {
        return ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getBoolean("biometricLock", true);
    }

    public static void setTheme(Context ctx, String theme) {
        SharedPreferences prefs = ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("theme", theme).apply();
    }

    public static String getTheme(Context ctx) {
        return ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getString("theme", THEME_NAMES.get(0));
    }

    public static void enableSuperSecretHamburgers(Context ctx) {
        ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean("iLikeHamburgers", true).apply();
    }

    public static boolean isSuperSecretHamburgersEnabled(Context ctx) {
        return ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getBoolean("iLikeHamburgers", false);
    }

}
