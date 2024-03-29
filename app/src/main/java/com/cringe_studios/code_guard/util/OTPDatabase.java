package com.cringe_studios.code_guard.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.cringe_studios.code_guard.BaseActivity;
import com.cringe_studios.code_guard.R;
import com.cringe_studios.code_guard.crypto.Crypto;
import com.cringe_studios.code_guard.crypto.CryptoException;
import com.cringe_studios.code_guard.crypto.CryptoParameters;
import com.cringe_studios.code_guard.model.OTPData;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public void promptAddOTPs(Context context, String groupId, Runnable done, OTPData... data) {
        List<OTPData> os = new ArrayList<>(getOTPs(groupId));

        boolean anyDuplicates = false;
        boolean[] duplicates = new boolean[data.length];
        for(int i = 0; i < data.length; i++) {
            OTPData o = data[i];

            if(OTPData.isSecretOTP(o) && !SettingsUtil.isSuperSecretSettingsEnabled(context)) {
                SettingsUtil.setEnableSuperSecretSettings(context, true);
                Toast.makeText(context, R.string.super_secret_settings_unlocked, Toast.LENGTH_LONG).show();
            }

            for (OTPData o2 : os) {
                if (Objects.equals(o.getName(), o2.getName()) && Objects.equals(o.getIssuer(), o2.getIssuer())) {
                    anyDuplicates = true;
                    duplicates[i] = true;
                    break;
                }
            }
        }

        Log.d("ADD", "Any dupes?" + anyDuplicates);

        if (anyDuplicates) {
            DialogUtil.showYesNoCancel(context, R.string.error_duplicate_otp_title, R.string.error_duplicate_otp_message, () -> {
                for(int i = 0; i < data.length; i++) {
                    OTPData o = data[i];
                    if(duplicates[i]) {
                        String oldName = o.getName();
                        int count = 1;
                        boolean hasDuplicates;
                        do {
                            o.setName(oldName + " - " + (count++));
                            hasDuplicates = false;
                            for(OTPData o2 : os) {
                                if(Objects.equals(o.getName(), o2.getName()) && Objects.equals(o.getIssuer(), o2.getIssuer())) {
                                    hasDuplicates = true;
                                    break;
                                }
                            }
                        }while(hasDuplicates);
                    }
                }

                os.addAll(Arrays.asList(data));
                updateOTPs(groupId, os);
                if (done != null) done.run();
            }, () -> {
                os.addAll(Arrays.asList(data));
                updateOTPs(groupId, os);
                if (done != null) done.run();
            }, null);

            return;
        }

        os.addAll(Arrays.asList(data));
        updateOTPs(groupId, os);
        if(done != null) done.run();
    }

    public void updateOTPs(String groupId, List<OTPData> o) {
        otps.put(groupId, o);
    }

    public void removeOTPs(String groupId) {
        otps.remove(groupId);
    }

    public static void promptLoadDatabase(Activity ctx, Runnable success, Runnable failure) {
        if(isDatabaseLoaded()) {
            if(success != null) success.run();
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

        ((BaseActivity) ctx).promptUnlock(success, failure);
    }

    public static void loadDatabase(Context context, SecretKey key) throws OTPDatabaseException, CryptoException {
        File file = new File(context.getFilesDir(), DB_FILE_NAME);
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new OTPDatabaseException(e);
            }
        }

        try {
            byte[] bytes = IOUtil.readBytes(file);

            loadedDatabase = loadFromEncryptedBytes(bytes, key, SettingsUtil.getCryptoParameters(context));
            loadedKey = key;
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

    public static OTPDatabase loadFromEncryptedBytes(byte[] bytes, SecretKey key, CryptoParameters parameters) throws CryptoException, OTPDatabaseException {
        if(key != null) {
            bytes = Crypto.decrypt(parameters, bytes, key);
        }

        return loadFromBytes(bytes);
    }

    private static byte[] convertToBytes(OTPDatabase db) {
        JsonObject object = new JsonObject();
        for(Map.Entry<String, List<OTPData>> en : db.otps.entrySet()) {
            object.add(en.getKey(), SettingsUtil.GSON.toJsonTree(en.getValue().toArray(new OTPData[0])));
        }
        return object.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] convertToEncryptedBytes(OTPDatabase db, SecretKey key, CryptoParameters parameters) throws CryptoException {
        byte[] dbBytes = convertToBytes(loadedDatabase);

        if(key != null) {
            dbBytes = Crypto.encrypt(parameters, dbBytes, key);
        }

        return dbBytes;
    }

    public static void saveDatabase(Context ctx, CryptoParameters parameters) throws OTPDatabaseException, CryptoException {
        if(!isDatabaseLoaded()) throw new OTPDatabaseException("Database is not loaded");
        File file = new File(ctx.getFilesDir(), DB_FILE_NAME);

        byte[] dbBytes = convertToEncryptedBytes(loadedDatabase, loadedKey, parameters);

        try(OutputStream fOut = new BufferedOutputStream(new FileOutputStream(file))) {
            fOut.write(dbBytes);
        } catch (IOException e) {
            throw new OTPDatabaseException(e);
        }
    }

    public static void unloadDatabase() {
        loadedDatabase = null;
    }

    public static void setLoadedDatabase(OTPDatabase loadedDatabase) {
        OTPDatabase.loadedDatabase = loadedDatabase;
    }

    public static OTPDatabase getLoadedDatabase() {
        return loadedDatabase;
    }

    public static SecretKey getLoadedKey() {
        return loadedKey;
    }

    public static void encrypt(Context ctx, SecretKey key, CryptoParameters parameters) throws OTPDatabaseException, CryptoException {
        if(!isDatabaseLoaded()) throw new OTPDatabaseException("Database is not loaded");
        loadedKey = key;
        saveDatabase(ctx, parameters);
    }

    public static void decrypt(Context ctx) throws OTPDatabaseException, CryptoException {
        if(!isDatabaseLoaded()) throw new OTPDatabaseException("Database is not loaded");
        loadedKey = null;
        saveDatabase(ctx, null);
    }

    public static boolean isDatabaseLoaded() {
        return loadedDatabase != null;
    }

}
