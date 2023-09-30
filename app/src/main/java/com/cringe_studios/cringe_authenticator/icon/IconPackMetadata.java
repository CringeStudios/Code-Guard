package com.cringe_studios.cringe_authenticator.icon;

public class IconPackMetadata {

    private String uuid;
    private String name;
    private int version;
    private IconMetadata[] icons;

    private IconPackMetadata() {}

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public IconMetadata[] getIcons() {
        return icons;
    }
}
