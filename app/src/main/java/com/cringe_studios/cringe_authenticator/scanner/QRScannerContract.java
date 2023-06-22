package com.cringe_studios.cringe_authenticator.scanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cringe_studios.cringe_authenticator.OTPData;

public class QRScannerContract extends ActivityResultContract<Void, OTPData> {

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Void theVoid) {
        return new Intent(context, QRScannerActivity.class);
    }

    @Override
    public @Nullable OTPData parseResult(int result, @Nullable Intent intent) {
        if(result != Activity.RESULT_OK || intent == null) {
            return null;
        }

        return (OTPData) intent.getSerializableExtra("data");
    }

}
