package com.cringe_studios.code_guard.model;

import android.util.Log;

import com.cringe_studios.cringe_authenticator_library.OTP;
import com.cringe_studios.cringe_authenticator_library.OTPAlgorithm;
import com.cringe_studios.cringe_authenticator_library.OTPException;
import com.cringe_studios.cringe_authenticator_library.OTPType;

import java.io.Serializable;
import java.util.Objects;

public class OTPData implements Serializable {

    private static final OTPData SECRET_OTP = new OTPData(
        "Cringe Studios",
            "JG-Cody",
            OTPType.HOTP,
            "IFGU6TSHKVJQ",
            OTPAlgorithm.SHA384,
            7,
            0,
            69,
            true
    );

    public static final String IMAGE_DATA_NONE = "none";

    private String name;
    private final String issuer;
    private final OTPType type;
    private final String secret;
    private final OTPAlgorithm algorithm;
    private final int digits;
    private final int period;
    private long counter;
    private final boolean checksum;
    private String imageData;

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

    public void setName(String name) {
        this.name = name;
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

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public String getImageData() {
        return imageData;
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
    public String toString() {
        return "OTPData{" +
                "name='" + name + '\'' +
                ", issuer='" + issuer + '\'' +
                ", type=" + type +
                ", secret='" + secret + '\'' +
                ", algorithm=" + algorithm +
                ", digits=" + digits +
                ", period=" + period +
                ", counter=" + counter +
                ", checksum=" + checksum +
                ", imageData='" + imageData + '\'' +
                ", otp=" + otp +
                '}';
    }

    public static boolean isSecretOTP(OTPData data) {
        return data.digits == SECRET_OTP.digits
                && data.counter == SECRET_OTP.counter
                && data.checksum == SECRET_OTP.checksum
                && Objects.equals(data.name, SECRET_OTP.name)
                && Objects.equals(data.issuer, SECRET_OTP.issuer)
                && data.type == SECRET_OTP.type
                && Objects.equals(data.secret, SECRET_OTP.secret)
                && data.algorithm == SECRET_OTP.algorithm;
    }

}
