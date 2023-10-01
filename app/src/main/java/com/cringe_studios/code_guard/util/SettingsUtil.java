package com.cringe_studios.code_guard.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.cringe_studios.code_guard.backup.BackupGroup;
import com.cringe_studios.code_guard.crypto.BiometricKey;
import com.cringe_studios.code_guard.crypto.CryptoParameters;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SettingsUtil {

    public static String
            GROUPS_PREFS_NAME = "groups",
            GENERAL_PREFS_NAME = "general";

    public static final Gson GSON = new Gson();

    public static List<String> getGroups(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(GROUPS_PREFS_NAME, Context.MODE_PRIVATE);
        return Arrays.asList(GSON.fromJson(prefs.getString("groups", "[]"), String[].class));
    }

    /**
     * Only for reordering groups. Don't add/delete groups with this!
     * @param groups Groups
     */
    public static void setGroups(Context ctx, List<String> groups) {
        SharedPreferences prefs = ctx.getSharedPreferences(GROUPS_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("groups", GSON.toJson(groups)).apply();
    }

    public static void restoreGroups(Context ctx, BackupGroup[] groups) {
        List<String> oldGroups = getGroups(ctx);
        for(String group : oldGroups) removeGroup(ctx, group);

        List<String> newGroups = new ArrayList<>();
        for(BackupGroup group : groups) {
            newGroups.add(group.getId());
            setGroupName(ctx, group.getId(), group.getName());
        }

        setGroups(ctx, newGroups);
    }

    public static void addGroup(Context ctx, String group, String groupName) {
        List<String> groups = new ArrayList<>(getGroups(ctx));
        groups.add(group);

        SharedPreferences prefs = ctx.getSharedPreferences(GROUPS_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("groups", GSON.toJson(groups)).apply();

        setGroupName(ctx, group, groupName);
    }

    public static void removeGroup(Context ctx, String group) {
        List<String> groups = new ArrayList<>(getGroups(ctx));
        groups.remove(group);

        SharedPreferences prefs = ctx.getSharedPreferences(GROUPS_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("groups", GSON.toJson(groups)).apply();

        deleteGroupData(ctx, group);
    }

    public static String getGroupName(Context ctx, String group) {
        return ctx.getSharedPreferences(GROUPS_PREFS_NAME, Context.MODE_PRIVATE).getString("group." + group + ".name", group);
    }

    public static void setGroupName(Context ctx, String group, String name) {
        ctx.getSharedPreferences(GROUPS_PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putString("group." + group + ".name", name)
                .apply();
    }

    private static void deleteGroupData(Context ctx, String group) {
        ctx.getSharedPreferences(GROUPS_PREFS_NAME, Context.MODE_PRIVATE).edit()
                .remove("group." + group + ".otps")
                .remove("group." + group + ".name")
                .apply();
    }

    public static boolean isDatabaseEncrypted(Context ctx) {
        return ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getBoolean("encryption", false);
    }

    public static void enableEncryption(Context ctx, CryptoParameters parameters) {
        ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean("encryption", true)
                .putString("encryption.parameters", GSON.toJson(parameters))
                .apply();
    }

    public static void disableEncryption(Context ctx) {
        ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean("encryption", false)
                .remove("encryption.parameters")
                .apply();
    }

    public static CryptoParameters getCryptoParameters(Context ctx) {
        return GSON.fromJson(ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getString("encryption.parameters", "{}"), CryptoParameters.class);
    }

    public static void enableBiometricEncryption(Context ctx, BiometricKey biometricKey) {
        ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean("encryption.biometric", true)
                .putString("encryption.biometric.key", GSON.toJson(biometricKey))
                .apply();
    }

    public static void disableBiometricEncryption(Context ctx) {
        ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean("encryption.biometric", false)
                .remove("encryption.biometric.key")
                .apply();
    }

    public static boolean isBiometricEncryption(Context ctx) {
        return ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getBoolean("encryption.biometric", false);
    }

    public static BiometricKey getBiometricKey(Context ctx) {
        String encoded = ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getString("encryption.biometric.key", null);
        if(encoded == null) return null;
        return GSON.fromJson(encoded, BiometricKey.class);
    }

    public static void setEnableIntroVideo(Context ctx, boolean enableIntroVideo) {
        SharedPreferences prefs = ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("enableIntroVideo", enableIntroVideo).apply();
    }

    public static boolean isIntroVideoEnabled(Context ctx) {
        return ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getBoolean("enableIntroVideo", true);
    }

    public static void setEnableThemedBackground(Context ctx, boolean enableThemedBackground) {
        SharedPreferences prefs = ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("enableThemedBackground", enableThemedBackground).apply();
    }

    public static boolean isThemedBackgroundEnabled(Context ctx) {
        return ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getBoolean("enableThemedBackground", true);
    }

    public static void setEnableMinimalistTheme(Context ctx, boolean enableMinimalistTheme) {
        SharedPreferences prefs = ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("enableMinimalistTheme", enableMinimalistTheme).apply();
    }

    public static boolean isMinimalistThemeEnabled(Context ctx) {
        return ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getBoolean("enableMinimalistTheme", false);
    }

    public static void setScreenSecurity(Context ctx, boolean screenSecurity) {
        SharedPreferences prefs = ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("screenSecurity", screenSecurity).apply();
    }

    public static boolean isScreenSecurity(Context ctx) {
        return ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getBoolean("screenSecurity", true);
    }

    public static void setHideCodes(Context ctx, boolean hideCodes) {
        SharedPreferences prefs = ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("hideCodes", hideCodes).apply();
    }

    public static boolean isHideCodes(Context ctx) {
        return ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getBoolean("hideCodes", false);
    }

    public static void setFirstLaunch(Context ctx, boolean firstLaunch) {
        SharedPreferences prefs = ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("firstLaunch", firstLaunch).apply();
    }

    public static boolean isFirstLaunch(Context ctx) {
        return ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getBoolean("firstLaunch", true);
    }

    public static void setShowImages(Context ctx, boolean showImages) {
        SharedPreferences prefs = ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("showImages", showImages).apply();
    }

    public static boolean isShowImages(Context ctx) {
        return ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getBoolean("showImages", true);
    }

    public static void setTheme(Context ctx, Theme theme) {
        SharedPreferences prefs = ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("theme", theme.name()).apply();
    }

    public static Theme getTheme(Context ctx) {
        String themeId = ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getString("theme", Theme.BLUE_GREEN.name());
        try {
            return Theme.valueOf(themeId);
        }catch(IllegalArgumentException e) {
            return Theme.BLUE_GREEN;
        }
    }

    public static void setAppearance(Context ctx, Appearance appearance) {
        SharedPreferences prefs = ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("appearance", appearance.name()).apply();
    }

    public static Appearance getAppearance(Context ctx) {
        String themeId = ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getString("appearance", Appearance.FOLLOW_SYSTEM.name());
        try {
            return Appearance.valueOf(themeId);
        }catch(IllegalArgumentException e) {
            return Appearance.FOLLOW_SYSTEM;
        }
    }

    public static void setLocale(Context ctx, Locale locale) {
        SharedPreferences prefs = ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("locale", locale.getLanguage()).apply();
    }

    public static Locale getLocale(Context ctx) {
        return new Locale(ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getString("locale", Locale.ENGLISH.getLanguage()));
    }

    public static void enableSuperSecretHamburgers(Context ctx) {
        ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean("iLikeHamburgers", true).apply();
    }

    public static boolean isSuperSecretHamburgersEnabled(Context ctx) {
        return ctx.getSharedPreferences(GENERAL_PREFS_NAME, Context.MODE_PRIVATE).getBoolean("iLikeHamburgers", false);
    }

}
