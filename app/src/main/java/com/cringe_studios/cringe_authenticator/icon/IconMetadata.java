package com.cringe_studios.cringe_authenticator.icon;

public class IconMetadata {

    private String filename;
    private String category;
    private String[] issuer;

    private IconMetadata() {}

    public String getFilename() {
        return filename;
    }

    public String getCategory() {
        return category;
    }

    public String[] getIssuer() {
        return issuer;
    }
}
