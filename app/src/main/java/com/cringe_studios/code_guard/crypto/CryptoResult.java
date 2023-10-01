package com.cringe_studios.code_guard.crypto;

public class CryptoResult {

    private final byte[] encrypted;
    private final byte[] iv;

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
