package com.cringe_studios.cringe_authenticator.crypto;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {

    public static byte[] generateHash(CryptoParameters parameters, String password) throws CryptoException {
        Argon2Parameters params = new Argon2Parameters.Builder()
                .withVersion(parameters.getArgon2Version())
                .withIterations(parameters.getArgon2Iterations())
                .withMemoryAsKB(parameters.getArgon2Memory())
                .withParallelism(parameters.getArgon2Parallelism())
                .withSalt(parameters.getSalt())
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);
        byte[] result = new byte[parameters.getEncryptionAESKeyLength()];
        generator.generateBytes(password.getBytes(StandardCharsets.UTF_8), result);
        return result;
    }

    public static SecretKey generateKey(CryptoParameters parameters, String password) throws CryptoException {
        byte[] hash = generateHash(parameters, password);
        return new SecretKeySpec(hash, "AES");
    }

    private static byte[] generateBytes(int length) {
        SecureRandom r = new SecureRandom();
        byte[] bytes = new byte[length];
        r.nextBytes(bytes);
        return bytes;
    }

    public static byte[] generateSalt(CryptoParameters parameters) {
        return generateBytes(parameters.getEncryptionSaltLength());
    }

    public static byte[] generateIV(CryptoParameters parameters) {
        return generateBytes(parameters.getEncryptionIVLength());
    }

    public static byte[] encrypt(CryptoParameters parameters, byte[] bytes, SecretKey key) throws CryptoException {
        try {
            Cipher cipher = Cipher.getInstance(parameters.getEncryptionAlgorithm());
            GCMParameterSpec spec = new GCMParameterSpec(parameters.getEncryptionGCMTagLength() * 8, parameters.getIV());
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            return cipher.doFinal(bytes);
        }catch(NoSuchAlgorithmException | NoSuchPaddingException |
               InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException |
               IllegalBlockSizeException e) {
            throw new CryptoException(e);
        }
    }

    public static byte[] decrypt(CryptoParameters parameters, byte[] bytes, SecretKey key) throws CryptoException {
        try {
            Cipher cipher = Cipher.getInstance(parameters.getEncryptionAlgorithm());
            GCMParameterSpec spec = new GCMParameterSpec(parameters.getEncryptionGCMTagLength() * 8, parameters.getIV());
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            return cipher.doFinal(bytes);
        }catch(NoSuchAlgorithmException | NoSuchPaddingException |
               InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException |
               IllegalBlockSizeException e) {
            throw new CryptoException(e);
        }
    }

}
