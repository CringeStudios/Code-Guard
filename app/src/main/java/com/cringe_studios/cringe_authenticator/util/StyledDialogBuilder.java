package com.cringe_studios.cringe_authenticator.util;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.cringe_studios.cringe_authenticator.R;

public class StyledDialogBuilder extends AlertDialog.Builder {

    public StyledDialogBuilder(Context context) {
        super(context);
    }

    public StyledDialogBuilder(Context context, int themeResId) {
        super(context, themeResId);
    }

    @NonNull
    @Override
    public AlertDialog create() {
        AlertDialog dialog = super.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_themed);
        return dialog;
    }

}
