package com.cringe_studios.cringe_authenticator.scanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cringe_studios.cringe_authenticator.model.OTPData;

public class QRScannerContract extends ActivityResultContract<Void, ScannerResult> {

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Void theVoid) {
        return new Intent(context, QRScannerActivity.class);
    }

    @Override
    public @Nullable ScannerResult parseResult(int result, @Nullable Intent intent) {
        if(result != Activity.RESULT_OK || intent == null) {
            if(intent != null) {
                return new ScannerResult(intent.getStringExtra("error"));
            }

            return null;
        }

        return new ScannerResult((OTPData[]) intent.getSerializableExtra("data"));
    }

}
