package com.cringe_studios.cringe_authenticator.model;

public class OTPMigrationPart {

    private OTPData[] otps;
    private int batchIndex;
    private int batchSize;

    public OTPMigrationPart(OTPData[] otps, int batchIndex, int batchSize) {
        this.otps = otps;
        this.batchIndex = batchIndex;
        this.batchSize = batchSize;
    }

    public OTPData[] getOTPs() {
        return otps;
    }

    public int getBatchIndex() {
        return batchIndex;
    }

    public int getBatchSize() {
        return batchSize;
    }
}