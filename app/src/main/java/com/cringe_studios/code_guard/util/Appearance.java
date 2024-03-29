package com.cringe_studios.code_guard.util;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;

import com.cringe_studios.code_guard.R;

public enum Appearance {

    FOLLOW_SYSTEM(R.string.appearance_follow_system, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    LIGHT(R.string.appearance_light, AppCompatDelegate.MODE_NIGHT_NO),
    DARK(R.string.appearance_dark, AppCompatDelegate.MODE_NIGHT_YES),
    ;

    @StringRes
    private final int name;

    @AppCompatDelegate.NightMode
    private final int value;

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
