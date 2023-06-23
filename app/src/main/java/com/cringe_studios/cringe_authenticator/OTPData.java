package com.cringe_studios.cringe_authenticator;

import androidx.annotation.NonNull;

import com.cringe_studios.cringe_authenticator_library.OTP;
import com.cringe_studios.cringe_authenticator_library.OTPAlgorithm;
import com.cringe_studios.cringe_authenticator_library.OTPType;

import java.io.Serializable;
import java.util.Objects;

public class OTPData implements Serializable {

    private String name;
    private OTPType type;
    private String secret;
    private OTPAlgorithm algorithm;
    private int digits;
    private int period;
    private int counter;

    // Cached
    private OTP otp;

    public OTPData(String name, OTPType type, String secret, OTPAlgorithm algorithm, int digits, int period, int counter) {
        this.name = name;
        this.type = type;
        this.secret = secret;
        this.algorithm = algorithm;
        this.digits = digits;
        this.period = period;
        this.counter = counter;
    }

    public String getName() {
        return name;
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

    public int getCounter() {
        return counter;
    }

    public OTP toOTP() {
        // TODO: checksum
        if(otp != null) return otp;
        return otp = OTP.createNewOTP(type, secret, algorithm, digits, counter, period, false);
    }

    @NonNull
    @Override
    public String toString() {
        return "OTPData{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", secret='" + secret + '\'' +
                ", algorithm=" + algorithm +
                ", digits=" + digits +
                ", period=" + period +
                ", counter=" + counter +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OTPData otpData = (OTPData) o;
        return digits == otpData.digits && period == otpData.period && counter == otpData.counter && Objects.equals(name, otpData.name) && type == otpData.type && Objects.equals(secret, otpData.secret) && algorithm == otpData.algorithm;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, secret, algorithm, digits, period, counter);
    }
}
