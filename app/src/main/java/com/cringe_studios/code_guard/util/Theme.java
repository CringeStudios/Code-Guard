package com.cringe_studios.code_guard.util;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import com.cringe_studios.code_guard.R;

public enum Theme {

    BLUE_GREEN(R.string.theme_blue_green, R.style.Theme_CringeAuthenticator_Blue_Green, R.drawable.background_blue_green_light, R.drawable.background_blue_green),
    RED_BLUE(R.string.theme_red_blue, R.style.Theme_CringeAuthenticator_Red_Blue, R.drawable.background_red_blue_light, R.drawable.background_red_blue),
    PINK_GREEN(R.string.theme_pink_green, R.style.Theme_CringeAuthenticator_Pink_Green, R.drawable.background_pink_green_light, R.drawable.background_pink_green),
    BLUE_YELLOW(R.string.theme_blue_yellow, R.style.Theme_CringeAuthenticator_Blue_Yellow, R.drawable.background_blue_yellow_light, R.drawable.background_blue_yellow),
    GREEN_YELLOW(R.string.theme_green_yellow, R.style.Theme_CringeAuthenticator_Green_Yellow, R.drawable.background_green_yellow_light, R.drawable.background_green_yellow),
    ORANGE_TURQUOISE(R.string.theme_orange_turquoise, R.style.Theme_CringeAuthenticator_Orange_Turquoise, R.drawable.background_orange_turquoise_light, R.drawable.background_orange_turquoise),
    KETCHUP_MUSTARD(R.string.theme_ketchup_mustard, R.style.Theme_CringeAuthenticator_Ketchup_Mustard, R.drawable.background_red_blue_light, R.drawable.background_red_blue),
    ;

    @StringRes
    private final int name;

    @StyleRes
    private final int style;

    @DrawableRes
    private final int lightBackground;

    @DrawableRes
    private final int darkBackground;

    Theme(@StringRes int name, @StyleRes int style, @DrawableRes int lightBackground, @DrawableRes int darkBackground) {
        this.name = name;
        this.style = style;
        this.lightBackground = lightBackground;
        this.darkBackground = darkBackground;
    }

    @StringRes
    public int getName() {
        return name;
    }

    @StyleRes
    public int getStyle() {
        return style;
    }

    @DrawableRes
    public int getLightBackground() {
        return lightBackground;
    }

    @DrawableRes
    public int getDarkBackground() {
        return darkBackground;
    }
}
