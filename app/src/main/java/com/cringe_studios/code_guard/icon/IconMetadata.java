package com.cringe_studios.code_guard.icon;

import java.io.File;

public class IconMetadata {

    private String name;
    private String filename;
    private String category;
    private String[] issuer;

    private IconMetadata() {}

    public String getName() {
        if(name != null) return name;

        String fileName = new File(filename).getName();
        int i = fileName.lastIndexOf('.');
        return i == -1 ? fileName : fileName.substring(0, i);
    }

    public String getFilename() {
        return filename;
    }

    public String getCategory() {
        return category == null ? "No category" : category;
    }

    public String[] getIssuer() {
        return issuer;
    }

    public boolean validate() {
        return filename != null && issuer != null;
    }

}
