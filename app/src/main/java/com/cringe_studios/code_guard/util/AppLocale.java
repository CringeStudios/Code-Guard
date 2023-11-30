package com.cringe_studios.code_guard.util;

import android.content.Context;

import androidx.annotation.StringRes;

import com.cringe_studios.code_guard.R;

import java.util.Locale;

public enum AppLocale {

    SYSTEM_DEFAULT(R.string.locale_system_default),
    ENGLISH(Locale.ENGLISH),
    GERMAN(Locale.GERMAN),
    FRENCH(Locale.FRENCH, true),
    POLISH(new Locale("pl"), true),

    UKRAINIAN(new Locale("uk"), true),
    ;

    @StringRes
    private final int name;

    private final Locale locale;

    private final boolean experimental;

    AppLocale(@StringRes int name) {
        this.name = name;
        this.locale = null;
        this.experimental = false;
    }

    AppLocale(Locale locale) {
        this.name = 0;
        this.locale = locale;
        this.experimental = false;
    }

    AppLocale(Locale locale, boolean experimental) {
        this.name = 0;
        this.locale = locale;
        this.experimental = experimental;
    }

    public String getName(Context context) {
        return (locale == null ? context.getString(name) : locale.getDisplayName(locale)) + (experimental ? " (in progress)" : "");
    }

    public Locale getLocale() {
        return locale;
    }

}
