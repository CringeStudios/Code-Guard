package com.cringe_studios.code_guard.scanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cringe_studios.code_guard.model.OTPData;

public class ScannerResult {

    private OTPData[] data;
    private String errorMessage;

    public ScannerResult(@NonNull OTPData[] data) {
        this.data = data;
    }

    public ScannerResult(@Nullable String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return data != null;
    }

    public OTPData[] getData() {
        return data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
