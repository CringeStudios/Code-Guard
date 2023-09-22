package com.cringe_studios.cringe_authenticator.util;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;

import com.cringe_studios.cringe_authenticator.R;

public enum Appearance {

    FOLLOW_SYSTEM(R.string.appearance_follow_system, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    LIGHT(R.string.appearance_light, AppCompatDelegate.MODE_NIGHT_NO),
    DARK(R.string.appearance_dark, AppCompatDelegate.MODE_NIGHT_YES),
    ;

    @StringRes
    private int name;

    @AppCompatDelegate.NightMode
    private int value;

    Appearance(@StringRes int name, @AppCompatDelegate.NightMode int value) {
        this.name = name;
        this.value = value;
    }

    @StringRes
    public int getName() {
        return name;
    }

    @AppCompatDelegate.NightMode
    public int getValue() {
        return value;
    }
}
