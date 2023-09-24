package com.cringe_studios.cringe_authenticator.util;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import com.cringe_studios.cringe_authenticator.crypto.CryptoException;
import com.cringe_studios.cringe_authenticator.crypto.CryptoParameters;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.crypto.SecretKey;

public class BackupUtil {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH);

    public static String getBackupName() {
        return "backup_" + FORMAT.format(new Date()); // TODO: indicate Cringe Authenticator
    }

    public static void saveBackup(Context context, Uri backupFile, SecretKey key, CryptoParameters parameters) throws BackupException, CryptoException {
        if(!OTPDatabase.isDatabaseLoaded()) throw new BackupException("Database is not loaded");

        byte[] dbBytes = OTPDatabase.convertToEncryptedBytes(OTPDatabase.getLoadedDatabase(), key, parameters);
        JsonObject object = new JsonObject();
        object.add("parameters", SettingsUtil.GSON.toJsonTree(parameters));
        object.addProperty("database", Base64.encodeToString(dbBytes, Base64.DEFAULT));
        try(OutputStream out = context.getContentResolver().openOutputStream(backupFile)) {
            if(out == null) throw new BackupException("Failed to write backup");
            out.write(object.toString().getBytes(StandardCharsets.UTF_8));
        }catch(IOException e) {
            throw new BackupException(e);
        }
    }

    public static CryptoParameters loadParametersFromBackup(Context context, Uri backupFile) throws BackupException {
        try(InputStream in = context.getContentResolver().openInputStream(backupFile)) {
            if(in == null) throw new BackupException("Failed to read backup file");
            byte[] backupBytes = IOUtil.readBytes(in);
            JsonObject object = SettingsUtil.GSON.fromJson(new String(backupBytes, StandardCharsets.UTF_8), JsonObject.class);
            return SettingsUtil.GSON.fromJson(object.get("parameters"), CryptoParameters.class); // TODO: check if params are valid
        } catch (IOException e) {
            throw new BackupException(e);
        }
    }

    public static OTPDatabase loadBackup(Context context, Uri backupFile, SecretKey key, CryptoParameters parameters) throws BackupException, OTPDatabaseException, CryptoException {
        try(InputStream in = context.getContentResolver().openInputStream(backupFile)) {
            if (in == null) throw new BackupException("Failed to read backup file");
            byte[] backupBytes = IOUtil.readBytes(in);
            JsonObject object = SettingsUtil.GSON.fromJson(new String(backupBytes, StandardCharsets.UTF_8), JsonObject.class);
            JsonElement db = object.get("database");
            if(db == null) throw new BackupException("Invalid backup file");
            return OTPDatabase.loadFromEncryptedBytes(Base64.decode(db.getAsString(), Base64.DEFAULT), key, parameters);
        } catch(JsonSyntaxException e) {
            throw new BackupException("Invalid JSON", e);
        } catch (IOException e) {
            throw new BackupException(e);
        }
    }

}
