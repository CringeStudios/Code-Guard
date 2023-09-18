package com.cringe_studios.cringe_authenticator.crypto;

public class BiometricKey {

    private final String id;
    private final byte[] encryptedKey;
    private final byte[] iv;

    public BiometricKey(String id, byte[] encryptedKey, byte[] iv) {
        this.id = id;
        this.encryptedKey = encryptedKey;
        this.iv = iv;
    }

    public String getId() {
        return id;
    }

    public byte[] getEncryptedKey() {
        return encryptedKey;
    }

    public byte[] getIV() {
        return iv;
    }
}
