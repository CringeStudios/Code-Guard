package com.cringe_studios.cringe_authenticator.util;

import android.content.Context;

import androidx.core.util.Consumer;

import com.cringe_studios.cringe_authenticator.crypto.Crypto;
import com.cringe_studios.cringe_authenticator.crypto.CryptoException;
import com.cringe_studios.cringe_authenticator.crypto.CryptoParameters;
import com.cringe_studios.cringe_authenticator.model.OTPData;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

public class OTPDatabase {

    public static final String DB_FILE_NAME = "db";

    private static OTPDatabase loadedDatabase;
    private static SecretKey loadedKey;

    private final Map<String, List<OTPData>> otps;

    private OTPDatabase(Map<String, List<OTPData>> otps) {
        this.otps = otps;
    }

    public List<OTPData> getOTPs(String groupId) {
        List<OTPData> o = otps.get(groupId);
        if(o == null) return Collections.emptyList();
        return o;
    }

    public void addOTP(String groupId, OTPData o) {
        // TODO: check for code with same name
        List<OTPData> os = new ArrayList<>(getOTPs(groupId));
        os.add(o);
        updateOTPs(groupId, os);
    }

    public void updateOTPs(String groupId, List<OTPData> o) {
        otps.put(groupId, o);
    }

    public void removeOTPs(String groupId) {
        otps.remove(groupId);
    }

    public static void promptLoadDatabase(Context ctx, Runnable success, Runnable failure) {
        if(isDatabaseLoaded()) {
            success.run();
            return;
        }

        if(!SettingsUtil.isDatabaseEncrypted(ctx)) {
            try {
                loadDatabase(ctx, null);
                if(success != null) success.run();
            } catch (OTPDatabaseException | CryptoException e) {
                DialogUtil.showErrorDialog(ctx, "Failed to load database: " + e, failure);
            }
            return;
        }

        DialogUtil.showInputPasswordDialog(ctx, password -> {
            try {
                SecretKey key = Crypto.generateKey(SettingsUtil.getCryptoParameters(ctx), password);
                loadDatabase(ctx, key);
                if (success != null) success.run();
            }catch(CryptoException e) {
                DialogUtil.showErrorDialog(ctx, "Failed to load database: Invalid password or database corrupted", failure);
            } catch (OTPDatabaseException e) {
                DialogUtil.showErrorDialog(ctx, "Failed to load database: " + e, failure);
            }
        }, failure);
    }

    public static OTPDatabase loadDatabase(Context context, SecretKey key) throws OTPDatabaseException, CryptoException {
        File file = new File(context.getFilesDir(), DB_FILE_NAME);
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new OTPDatabaseException(e);
            }
        }

        try(FileInputStream fIn = new FileInputStream(file)) {
            ByteBuffer fileBuffer = ByteBuffer.allocate((int) file.length());
            byte[] buffer = new byte[1024];
            int len;
            while((len = fIn.read(buffer)) > 0) {
                fileBuffer.put(buffer, 0, len);
            }

            if(key == null) {
                loadedDatabase = loadFromBytes(fileBuffer.array());
                loadedKey = null;
                return loadedDatabase;
            }

            loadedDatabase = loadFromBytes(Crypto.decrypt(SettingsUtil.getCryptoParameters(context), fileBuffer.array(), key));
            loadedKey = key;
            return loadedDatabase;
        }catch(IOException e) {
            throw new OTPDatabaseException(e);
        }
    }

    private static OTPDatabase loadFromBytes(byte[] bytes) throws OTPDatabaseException {
        try {
            Map<String, List<OTPData>> data = new HashMap<>();
            if(bytes.length == 0) return new OTPDatabase(data);

            JsonObject object = SettingsUtil.GSON.fromJson(new String(bytes, StandardCharsets.UTF_8), JsonObject.class);
            for(String key : object.keySet()) {
                data.put(key, new ArrayList<>(Arrays.asList(SettingsUtil.GSON.fromJson(object.getAsJsonArray(key), OTPData[].class))));
            }

            return new OTPDatabase(data);
        }catch(JsonSyntaxException e) {
            throw new OTPDatabaseException("Password incorrect or database is corrupted");
        }catch(ClassCastException e) {
            throw new OTPDatabaseException("Invalid database");
        }
    }

    private static byte[] convertToBytes(OTPDatabase db) {
        JsonObject object = new JsonObject();
        for(Map.Entry<String, List<OTPData>> en : db.otps.entrySet()) {
            object.add(en.getKey(), SettingsUtil.GSON.toJsonTree(en.getValue().toArray(new OTPData[0])));
        }
        return object.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static void saveDatabase(Context ctx, CryptoParameters parameters) throws OTPDatabaseException, CryptoException {
        if(!isDatabaseLoaded()) throw new IllegalStateException("Database is not loaded");
        File file = new File(ctx.getFilesDir(), DB_FILE_NAME);

        byte[] dbBytes = convertToBytes(loadedDatabase);

        if(loadedKey != null) {
            dbBytes = Crypto.encrypt(parameters, dbBytes, loadedKey);
        }

        try(FileOutputStream fOut = new FileOutputStream(file)) {
            fOut.write(dbBytes);
        } catch (IOException e) {
            throw new OTPDatabaseException(e);
        }
    }

    public static void unloadDatabase() {
        loadedDatabase = null;
    }

    public static OTPDatabase getLoadedDatabase() {
        return loadedDatabase;
    }

    public static void encrypt(Context ctx, SecretKey key, CryptoParameters parameters) throws OTPDatabaseException, CryptoException {
        if(!isDatabaseLoaded()) throw new IllegalStateException("Database is not loaded");
        loadedKey = key;
        saveDatabase(ctx, parameters);
    }

    public static void decrypt(Context ctx) throws OTPDatabaseException, CryptoException {
        if(!isDatabaseLoaded()) throw new IllegalStateException("Database is not loaded");
        loadedKey = null;
        saveDatabase(ctx, null);
    }

    public static boolean isDatabaseLoaded() {
        return loadedDatabase != null;
    }

}
