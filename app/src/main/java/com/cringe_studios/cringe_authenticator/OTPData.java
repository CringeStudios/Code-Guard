package com.cringe_studios.cringe_authenticator;

import com.cringe_studios.cringe_authenticator_library.OTP;
import com.cringe_studios.cringe_authenticator_library.OTPAlgorithm;
import com.cringe_studios.cringe_authenticator_library.OTPType;

import java.io.Serializable;

public class OTPData implements Serializable {

    private String name;
    private OTPType type;
    private String secret;
    private OTPAlgorithm algorithm;
    private int digits;
    private int period;
    private int counter;

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
        return OTP.createNewOTP(type, secret, algorithm, digits, counter, period, false);
    }

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
}
