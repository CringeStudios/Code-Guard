package com.cringe_studios.code_guard.otplist;

import androidx.annotation.StringRes;

import com.cringe_studios.code_guard.R;

public enum HiddenStyle {

    STARS(R.string.style_stars, '\u2731'),
    DOTS(R.string.style_dots, '\u2022'),
    ;

    @StringRes
    private final int name;

    private final char hiddenChar;

    HiddenStyle(@StringRes int name, char hiddenChar) {
        this.name = name;
        this.hiddenChar = hiddenChar;
    }

    public int getName() {
        return name;
    }

    public char getHiddenChar() {
        return hiddenChar;
    }
}
