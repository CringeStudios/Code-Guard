package com.cringe_studios.cringe_authenticator.util;

import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import com.cringe_studios.cringe_authenticator.R;

public enum Theme {

    BLUE_GREEN(R.string.theme_blue_green, R.style.Theme_CringeAuthenticator_Blue_Green),
    RED_BLUE(R.string.theme_red_blue, R.style.Theme_CringeAuthenticator_Red_Blue),
    PINK_GREEN(R.string.theme_pink_green, R.style.Theme_CringeAuthenticator_Pink_Green),
    BLUE_YELLOW(R.string.theme_blue_yellow, R.style.Theme_CringeAuthenticator_Blue_Yellow),
    GREEN_YELLOW(R.string.theme_green_yellow, R.style.Theme_CringeAuthenticator_Green_Yellow),
    ORANGE_TURQUOISE(R.string.theme_orange_turquoise, R.style.Theme_CringeAuthenticator_Orange_Turquoise),
    ;

    @StringRes
    private final int name;

    @StyleRes
    private final int style;

    Theme(@StringRes int name, @StyleRes int style) {
        this.name = name;
        this.style = style;
    }

    @StringRes
    public int getName() {
        return name;
    }

    @StyleRes
    public int getStyle() {
        return style;
    }

}
