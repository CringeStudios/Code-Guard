package com.cringe_studios.cringe_authenticator.crypto;

import org.bouncycastle.crypto.params.Argon2Parameters;

public class CryptoParameters {

    private final String hashType = "argon2";

    private final int argon2Version = Argon2Parameters.ARGON2_VERSION_13;
    private final int argon2Iterations = 2;
    private final int argon2Memory = 16384;
    private final int argon2Parallelism = 1;

    private final String encryptionAlgorithm = "AES/GCM/NoPadding";
    private final int encryptionGCMTagLength = 16;
    private final int encryptionIVLength = 12;
    private final int encryptionAESKeyLength = 32;
    private final int encryptionSaltLength = 16;

    private byte[] salt;
    private byte[] iv;

    private CryptoParameters() {}

    private void init(byte[] salt, byte[] iv) {
        this.salt = salt;
        this.iv = iv;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getIV() {
        return iv;
    }

    public String getHashType() {
        return hashType;
    }

    public int getArgon2Version() {
        return argon2Version;
    }

    public int getArgon2Iterations() {
        return argon2Iterations;
    }

    public int getArgon2Memory() {
        return argon2Memory;
    }

    public int getArgon2Parallelism() {
        return argon2Parallelism;
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public int getEncryptionGCMTagLength() {
        return encryptionGCMTagLength;
    }

    public int getEncryptionIVLength() {
        return encryptionIVLength;
    }

    public int getEncryptionAESKeyLength() {
        return encryptionAESKeyLength;
    }

    public int getEncryptionSaltLength() {
        return encryptionSaltLength;
    }

    public static CryptoParameters createNew() {
        CryptoParameters params = new CryptoParameters();
        byte[] salt = Crypto.generateSalt(params);
        byte[] iv = Crypto.generateIV(params);
        params.init(salt, iv);
        return params;
    }

}
