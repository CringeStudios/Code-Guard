package com.cringe_studios.code_guard.util;

public class BackupException extends Exception {
    public BackupException() {
    }

    public BackupException(String message) {
        super(message);
    }

    public BackupException(String message, Throwable cause) {
        super(message, cause);
    }

    public BackupException(Throwable cause) {
        super(cause);
    }
}
