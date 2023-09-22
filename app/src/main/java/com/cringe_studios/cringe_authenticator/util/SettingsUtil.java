package com.cringe_studios.cringe_authenticator.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.crypto.BiometricKey;
import com.cringe_studios.cringe_authenticator.crypto.CryptoException;
import com.cringe_studios.cringe_authenticator.crypto.CryptoParameters;
import com.google.gson.Gson;

import org.bouncycastle.jcajce.provider.symmetric.ARC4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    /*public static List<OTPData> getOTPs(Context ctx, String group) {
        String currentOTPs = ctx.getSharedPreferences(GROUPS_PREFS_NAME, Context.MODE_PRIVATE).getString("group." + group + ".otps", "[]");
        return Arrays.asList(GSON.fromJson(currentOTPs, OTPData[].class));
    }

    public static void addOTP(Context ctx, String group, @NonNull OTPData data) {
        // TODO: check for code with same name

        List<OTPData> otps = new ArrayList<>(getOTPs(ctx, group));
        otps.add(data);

        ctx.getSharedPreferences(GROUPS_PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putString("group." + group + ".otps", GSON.toJson(otps.toArray(new OTPData[0])))
                .apply();
    }

    public static void updateOTPs(Context ctx, String group, List<OTPData> otps) {
        ctx.getSharedPreferences(GROUPS_PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putString("group." + group + ".otps", GSON.toJson(otps.toArray(new OTPData[0])))
                .apply();
    }*/

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
