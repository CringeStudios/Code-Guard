package com.cringe_studios.code_guard.util;

import android.content.Context;

import androidx.annotation.StringRes;

import com.cringe_studios.code_guard.R;

import java.util.Locale;

public enum AppLocale {

    SYSTEM_DEFAULT(R.string.locale_system_default),
    ENGLISH(Locale.ENGLISH),
    GERMAN(Locale.GERMAN),
    FRENCH(Locale.FRENCH),
    POLISH(new Locale("pl")),

    UKRAINIAN(new Locale("uk")),
    ;

    @StringRes
    private final int name;

    private final Locale locale;

    AppLocale(@StringRes int name) {
        this.name = name;
        this.locale = null;
    }

    AppLocale(Locale locale) {
        this.name = 0;
        this.locale = locale;
    }

    public String getName(Context context) {
        return locale == null ? context.getString(name) : locale.getDisplayName(locale);
    }

    public Locale getLocale() {
        return locale;
    }

}
