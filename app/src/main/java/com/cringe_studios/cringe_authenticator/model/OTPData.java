package com.cringe_studios.cringe_authenticator.model;

import com.cringe_studios.cringe_authenticator_library.OTP;
import com.cringe_studios.cringe_authenticator_library.OTPAlgorithm;
import com.cringe_studios.cringe_authenticator_library.OTPException;
import com.cringe_studios.cringe_authenticator_library.OTPType;

import java.io.Serializable;
import java.util.Objects;

public class OTPData implements Serializable {

    private String name;
    private String issuer;
    private OTPType type;
    private String secret;
    private OTPAlgorithm algorithm;
    private int digits;
    private int period;
    private long counter;
    private boolean checksum;

    // Cached
    private transient OTP otp;

    public OTPData(String name, String issuer, OTPType type, String secret, OTPAlgorithm algorithm, int digits, int period, long counter, boolean checksum) {
        this.name = name;
        this.issuer = issuer;
        this.type = type;
        this.secret = secret;
        this.algorithm = algorithm;
        this.digits = digits;
        this.period = period;
        this.counter = counter;
        this.checksum = checksum;
    }

    public String getName() {
        return name;
    }

    public String getIssuer() {
        return issuer;
    }

    public OTPType getType() {
        return type;
    }

    public String getSecret() {
        return secret;
    }

    public OTPAlgorithm getAlgorithm() {
        return algorithm;
    }

    public int getDigits() {
        return digits;
    }

    public int getPeriod() {
        return period;
    }

    public long getCounter() {
        return counter;
    }

    public boolean hasChecksum() {
        return checksum;
    }

    public String getPin() throws OTPException {
        return getOTP().getPin();
    }

    public void incrementCounter() {
        getOTP().incrementCounter();
        this.counter = getOTP().getCounter();
    }

    public long getNextDueTime() {
        return getOTP().getNextDueTime();
    }

    public String validate() {
        try {
            getOTP();
            return null;
        }catch(RuntimeException e) {
            return e.getMessage() != null ? e.getMessage() : e.toString();
        }
    }

    private OTP getOTP() {
        if(otp != null) return otp;
        try {
            return otp = OTP.createNewOTP(type, secret, algorithm, digits, counter, period, checksum);
        } catch (OTPException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OTPData otpData = (OTPData) o;
        return digits == otpData.digits && period == otpData.period && counter == otpData.counter && checksum == otpData.checksum && Objects.equals(name, otpData.name) && Objects.equals(issuer, otpData.issuer) && type == otpData.type && Objects.equals(secret, otpData.secret) && algorithm == otpData.algorithm;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, issuer, type, secret, algorithm, digits, period, counter, checksum);
    }

}
