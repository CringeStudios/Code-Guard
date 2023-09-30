package com.cringe_studios.cringe_authenticator.util;


import android.content.Context;
import android.content.res.TypedArray;
import android.view.WindowManager;

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

        TypedArray arr = dialog.getContext().obtainStyledAttributes(new int[] {R.attr.dialogBackground});
        try {
            dialog.getWindow().setBackgroundDrawable(arr.getDrawable(0));

            if(SettingsUtil.isScreenSecurity(getContext())) {
                dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            }
        }finally {
            arr.close();
        }

        return dialog;
    }

}
