package com.cringe_studios.cringe_authenticator.util;

import android.util.Base64;

import com.cringe_studios.cringe_authenticator.crypto.Crypto;
import com.cringe_studios.cringe_authenticator.crypto.CryptoException;
import com.cringe_studios.cringe_authenticator.crypto.CryptoParameters;
import com.cringe_studios.cringe_authenticator.model.OTPData;
import com.google.gson.JsonObject;

import org.bouncycastle.jcajce.provider.symmetric.ARC4;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

public class BackupUtil {

    public static void saveBackup(File backupFile, SecretKey key, CryptoParameters parameters) throws BackupException, CryptoException {
        if(!OTPDatabase.isDatabaseLoaded()) throw new BackupException("Database is not loaded");

        if(!backupFile.exists()) {
            File parent = backupFile.getParentFile();
            if(parent != null && !parent.exists()) parent.mkdirs();
            try {
                backupFile.createNewFile();
            } catch (IOException e) {
                throw new BackupException(e);
            }
        }

        byte[] dbBytes = OTPDatabase.convertToEncryptedBytes(OTPDatabase.getLoadedDatabase(), key, parameters);
        JsonObject object = new JsonObject();
        object.add("parameters", SettingsUtil.GSON.toJsonTree(parameters));
        object.addProperty("database", Base64.encodeToString(dbBytes, Base64.DEFAULT));
        try(FileOutputStream fOut = new FileOutputStream(backupFile)) {
            fOut.write(object.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new BackupException(e);
        }
    }

    public static CryptoParameters loadParametersFromBackup(File backupFile) throws BackupException {
        try {
            byte[] backupBytes = IOUtil.readBytes(backupFile);
            JsonObject object = SettingsUtil.GSON.fromJson(new String(backupBytes, StandardCharsets.UTF_8), JsonObject.class);
            return SettingsUtil.GSON.fromJson(object.get("parameters"), CryptoParameters.class);
        } catch (IOException e) {
            throw new BackupException(e);
        }
    }

    public static OTPDatabase loadBackup(File backupFile, SecretKey key, CryptoParameters parameters) throws BackupException, OTPDatabaseException, CryptoException {
        try {
            byte[] backupBytes = IOUtil.readBytes(backupFile);
            JsonObject object = SettingsUtil.GSON.fromJson(new String(backupBytes, StandardCharsets.UTF_8), JsonObject.class);
            return OTPDatabase.loadFromEncryptedBytes(Base64.decode(object.get("database").getAsString(), Base64.DEFAULT), key, parameters);
        } catch (IOException e) {
            throw new BackupException(e);
        }
    }

}
