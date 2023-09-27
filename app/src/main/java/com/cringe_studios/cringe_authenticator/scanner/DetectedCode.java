package com.cringe_studios.cringe_authenticator.scanner;

import com.cringe_studios.cringe_authenticator.model.OTPData;
import com.cringe_studios.cringe_authenticator.model.OTPMigrationPart;

public class DetectedCode {

    private OTPData data;
    private OTPMigrationPart migrationPart;

    public DetectedCode(OTPData data) {
        this.data = data;
    }

    public DetectedCode(OTPMigrationPart migrationPart) {
        this.migrationPart = migrationPart;
    }

    public boolean isMigrationPart() {
        return migrationPart != null;
    }

    public OTPData getData() {
        return data;
    }

    public OTPData[] getOTPs() {
        return data != null ? new OTPData[] { data } : migrationPart.getOTPs();
    }

    public OTPMigrationPart getMigrationPart() {
        return migrationPart;
    }

}
