package com.cringe_studios.cringe_authenticator.backup;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import com.cringe_studios.cringe_authenticator.crypto.CryptoException;
import com.cringe_studios.cringe_authenticator.crypto.CryptoParameters;
import com.cringe_studios.cringe_authenticator.util.BackupException;
import com.cringe_studios.cringe_authenticator.util.IOUtil;
import com.cringe_studios.cringe_authenticator.util.OTPDatabase;
import com.cringe_studios.cringe_authenticator.util.OTPDatabaseException;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
        String database = Base64.encodeToString(dbBytes, Base64.DEFAULT);

        List<String> groups = SettingsUtil.getGroups(context);
        BackupGroup[] groupsArray = new BackupGroup[groups.size()];
        for(int i = 0; i < groups.size(); i++) {
            String group = groups.get(i);
            groupsArray[i] = new BackupGroup(group, SettingsUtil.getGroupName(context, group));
        }

        BackupData data = new BackupData(parameters, database, groupsArray);

        try(OutputStream out = context.getContentResolver().openOutputStream(backupFile)) {
            if(out == null) throw new BackupException("Failed to write backup");
            out.write(SettingsUtil.GSON.toJson(data).getBytes(StandardCharsets.UTF_8));
        }catch(IOException e) {
            throw new BackupException(e);
        }
    }

    public static CryptoParameters loadParametersFromBackup(Context context, Uri backupFile) throws BackupException {
        try(InputStream in = context.getContentResolver().openInputStream(backupFile)) {
            if(in == null) throw new BackupException("Failed to read backup file");
            byte[] backupBytes = IOUtil.readBytes(in);
            BackupData data = SettingsUtil.GSON.fromJson(new String(backupBytes, StandardCharsets.UTF_8), BackupData.class);
            if(!data.getParameters().isValid()) throw new BackupException("Invalid crypto parameters");
            return data.getParameters();
        } catch (JsonSyntaxException e) {
            throw new BackupException("Invalid JSON", e);
        } catch (IOException e) {
            throw new BackupException(e);
        }
    }

    public static BackupData loadBackup(Context context, Uri backupFile) throws BackupException {
        try (InputStream in = context.getContentResolver().openInputStream(backupFile)) {
            if (in == null) throw new BackupException("Failed to read backup file");
            byte[] backupBytes = IOUtil.readBytes(in);
            BackupData data = SettingsUtil.GSON.fromJson(new String(backupBytes, StandardCharsets.UTF_8), BackupData.class);
            if(!data.isValid()) throw new BackupException("Invalid backup data"); // TODO: more info on backup errors
            return data;
        } catch (JsonSyntaxException e) {
            throw new BackupException("Invalid JSON", e);
        } catch (IOException e) {
            throw new BackupException(e);
        }
    }

}
