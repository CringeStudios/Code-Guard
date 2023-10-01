package com.cringe_studios.cringe_authenticator.icon;

public class Icon {

    private final IconMetadata metadata;
    private final byte[] bytes;

    public Icon(IconMetadata metadata, byte[] bytes) {
        this.metadata = metadata;
        this.bytes = bytes;
    }

    public IconMetadata getMetadata() {
        return metadata;
    }

    public byte[] getBytes() {
        return bytes;
    }

}
