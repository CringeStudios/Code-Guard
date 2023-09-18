package com.cringe_studios.cringe_authenticator.crypto;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.cringe_studios.cringe_authenticator.util.OTPDatabase;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {

    private static final String KEY_STORE = "AndroidKeyStore";

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

    public static CryptoResult encryptWithResult(CryptoParameters parameters, byte[] bytes, SecretKey key, boolean useIV) throws CryptoException {
        try {
            Cipher cipher = Cipher.getInstance(parameters.getEncryptionAlgorithm());
            GCMParameterSpec spec = new GCMParameterSpec(parameters.getEncryptionGCMTagLength() * 8, parameters.getIV());
            if(useIV) {
                cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            }else {
                cipher.init(Cipher.ENCRYPT_MODE, key);
            }
            return new CryptoResult(cipher.doFinal(bytes), cipher.getIV());
        }catch(NoSuchAlgorithmException | NoSuchPaddingException |
               InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException |
               IllegalBlockSizeException e) {
            throw new CryptoException(e);
        }
    }

    public static byte[] encrypt(CryptoParameters parameters, byte[] bytes, SecretKey key) throws CryptoException {
        return encryptWithResult(parameters, bytes, key, true).getEncrypted();
    }

    public static byte[] decrypt(CryptoParameters parameters, byte[] bytes, SecretKey key, byte[] overrideIV) throws CryptoException {
        try {
            Cipher cipher = Cipher.getInstance(parameters.getEncryptionAlgorithm());
            GCMParameterSpec spec = new GCMParameterSpec(parameters.getEncryptionGCMTagLength() * 8, overrideIV != null ? overrideIV : parameters.getIV());
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            return cipher.doFinal(bytes);
        }catch(NoSuchAlgorithmException | NoSuchPaddingException |
               InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException |
               IllegalBlockSizeException e) {
            throw new CryptoException(e);
        }
    }

    public static byte[] decrypt(CryptoParameters parameters, byte[] bytes, SecretKey key) throws CryptoException {
        return decrypt(parameters, bytes, key, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static BiometricKey createBiometricKey(CryptoParameters parameters) throws CryptoException {
        try {
            KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_STORE);

            String keyID = UUID.randomUUID().toString();
            generator.init(new KeyGenParameterSpec.Builder(keyID,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setKeySize(parameters.getEncryptionAESKeyLength() * 8)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(true)
                    .setUserAuthenticationValidityDurationSeconds(60)
                    .setRandomizedEncryptionRequired(true)
                    .build());

            SecretKey biometricKey = generator.generateKey();
            SecretKey key = OTPDatabase.getLoadedKey();
            CryptoResult encryptedKey = Crypto.encryptWithResult(parameters, key.getEncoded(), biometricKey, false);
            return new BiometricKey(keyID, encryptedKey.getEncrypted(), encryptedKey.getIV());
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            throw new CryptoException(e);
        }
    }

    public static SecretKey getBiometricKey(BiometricKey key) throws CryptoException {
        try {
            KeyStore store = KeyStore.getInstance(KEY_STORE);
            store.load(null);
            return (SecretKey) store.getKey(key.getId(), null);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | IOException e) {
            throw new CryptoException(e);
        }
    }

    public static void deleteBiometricKey(BiometricKey key) throws CryptoException {
        try {
            KeyStore ks = KeyStore.getInstance(KEY_STORE);
            ks.load(null);
            ks.deleteEntry(key.getId());
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
    }

}
