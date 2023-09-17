package com.cringe_studios.cringe_authenticator.util;

public class OTPDatabaseException extends Exception {
    public OTPDatabaseException() {
    }

    public OTPDatabaseException(String message) {
        super(message);
    }

    public OTPDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public OTPDatabaseException(Throwable cause) {
        super(cause);
    }
}
