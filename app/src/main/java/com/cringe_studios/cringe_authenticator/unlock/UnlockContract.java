package com.cringe_studios.cringe_authenticator.unlock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UnlockContract extends ActivityResultContract<Void, Boolean> {
    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Void unused) {
        return new Intent(context, UnlockActivity.class).putExtra("contract", true);
    }

    @Override
    public Boolean parseResult(int i, @Nullable Intent intent) {
        return i == Activity.RESULT_OK;
    }
}
