package com.cringe_studios.cringe_authenticator.crypto;

public class CryptoResult {

    private byte[] encrypted;
    private byte[] iv;

    public CryptoResult(byte[] encrypted, byte[] iv) {
        this.encrypted = encrypted;
        this.iv = iv;
    }

    public byte[] getEncrypted() {
        return encrypted;
    }

    public byte[] getIV() {
        return iv;
    }
}
