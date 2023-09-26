package com.cringe_studios.cringe_authenticator.backup;

import android.util.Base64;
import android.util.Log;

import com.cringe_studios.cringe_authenticator.crypto.CryptoException;
import com.cringe_studios.cringe_authenticator.crypto.CryptoParameters;
import com.cringe_studios.cringe_authenticator.model.OTPData;
import com.cringe_studios.cringe_authenticator.proto.OTPMigration;
import com.cringe_studios.cringe_authenticator.util.BackupException;
import com.cringe_studios.cringe_authenticator.util.OTPDatabase;
import com.cringe_studios.cringe_authenticator.util.OTPDatabaseException;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;

import javax.crypto.SecretKey;

public class BackupData {

    private CryptoParameters parameters;

    private String database;

    private BackupGroup[] groups;

    private BackupData() {}

    public BackupData(CryptoParameters parameters, String database, BackupGroup[] groups) {
        this.parameters = parameters;
        this.database = database;
        this.groups = groups;
    }

    public CryptoParameters getParameters() {
        return parameters;
    }

    public String getDatabase() {
        return database;
    }

    public OTPDatabase loadDatabase(SecretKey key, CryptoParameters parameters) throws BackupException, OTPDatabaseException, CryptoException {
        try {
            return OTPDatabase.loadFromEncryptedBytes(Base64.decode(database, Base64.DEFAULT), key, parameters);
        }catch(IllegalArgumentException e) {
            throw new BackupException(e);
        }
    }

    public BackupGroup[] getGroups() {
        return groups;
    }

    public boolean isValid() {
        if(parameters == null || database == null || groups == null) return false;

        if(!parameters.isValid()) return false;

        for(BackupGroup group : groups) {
            if(!group.isValid()) return false;
        }

        return true;
    }

}
