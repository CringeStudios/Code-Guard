package com.cringe_studios.cringe_authenticator.util;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.cringe_studios.cringe_authenticator.OTPData;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsUtil {

    private static final Gson GSON = new Gson();

    public static List<OTPData> getOTPs(SharedPreferences prefs, String group) {
        String currentOTPs = prefs.getString("group." + group, "[]");
        return Arrays.asList(GSON.fromJson(currentOTPs, OTPData[].class));
    }

    public static void addOTP(SharedPreferences prefs, String group, @NonNull OTPData data) {
        List<OTPData> otps = new ArrayList<>(getOTPs(prefs, group));
        otps.add(data);

        prefs.edit()
                .putString("group." + group, GSON.toJson(otps.toArray(new OTPData[otps.size()])))
                .apply();
    }

}
