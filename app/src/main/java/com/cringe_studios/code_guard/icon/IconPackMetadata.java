package com.cringe_studios.code_guard.icon;

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

    public boolean validate() {
        if(uuid == null || name == null || icons == null) return false;

        for(IconMetadata i : icons) {
            if(!i.validate()) return false;
        }

        return true;
    }

}
