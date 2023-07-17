package com.cringe_studios.cringe_authenticator.urihandler;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.model.OTPData;
import com.cringe_studios.cringe_authenticator.util.DialogUtil;
import com.cringe_studios.cringe_authenticator.util.OTPParser;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;
import com.cringe_studios.cringe_authenticator.util.StyledDialogBuilder;
import com.cringe_studios.cringe_authenticator.util.ThemeUtil;

public class URIHandlerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeUtil.loadTheme(this);

        Intent intent = getIntent();
        if(intent == null) {
            finish();
            return;
        }

        Uri uri = intent.getData();
        switch(uri.getScheme().toLowerCase()) {
            case "otpauth":
                importCode(uri);
                break;
            case "otpauth-migration":
                importMigration(uri);
                break;
        }
    }

    private void importCode(Uri uri) {
        try {
            OTPData data = OTPParser.parse(uri);
            importCodes(data);
        }catch(IllegalArgumentException e) {
            AlertDialog dialog = new StyledDialogBuilder(this)
                    .setTitle(R.string.uri_handler_failed_title)
                    .setMessage(e.getMessage())
                    .setPositiveButton(R.string.ok, (d, which) -> finish())
                    .create();

            dialog.setOnDismissListener(d -> finish());
            dialog.show();
        }
    }

    private void importMigration(Uri uri) {
        try {
            OTPData[] data = OTPParser.parseMigration(uri);
            importCodes(data);
        }catch(IllegalArgumentException e) {
            AlertDialog dialog = new StyledDialogBuilder(this)
                    .setTitle(R.string.uri_handler_failed_title)
                    .setMessage(e.getMessage())
                    .setPositiveButton(R.string.ok, (d, which) -> finish())
                    .create();

            dialog.setOnDismissListener(d -> finish());
            dialog.show();
        }
    }

    private void importCodes(OTPData... data) {
        DialogUtil.showImportCodeDialog(this, group -> {
            for(OTPData d : data) SettingsUtil.addOTP(this, group, d);
        }, this::finish);
    }

}
