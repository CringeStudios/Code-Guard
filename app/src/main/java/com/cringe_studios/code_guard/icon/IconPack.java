package com.cringe_studios.code_guard.icon;

public class IconPack {

    private final IconPackMetadata metadata;
    private final Icon[] icons;

    public IconPack(IconPackMetadata metadata, Icon[] icons) {
        this.metadata = metadata;
        this.icons = icons;
    }

    public IconPackMetadata getMetadata() {
        return metadata;
    }

    public Icon[] getIcons() {
        return icons;
    }

    public Icon findIconForIssuer(String issuer) {
        if(issuer == null) return null;

        for(Icon icon : icons) {
            for(String i : icon.getMetadata().getIssuer()) {
                if(issuer.equalsIgnoreCase(i)) {
                    return icon;
                }
            }
        }

        return null;
    }

}
