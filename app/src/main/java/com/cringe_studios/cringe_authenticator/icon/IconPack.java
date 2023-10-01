package com.cringe_studios.cringe_authenticator.icon;

public class IconPack {

    private IconPackMetadata metadata;
    private Icon[] icons;

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
