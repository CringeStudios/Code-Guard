package com.cringe_studios.code_guard.urihandler;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.cringe_studios.code_guard.BaseActivity;
import com.cringe_studios.code_guard.R;
import com.cringe_studios.code_guard.crypto.CryptoException;
import com.cringe_studios.code_guard.model.OTPData;
import com.cringe_studios.code_guard.util.DialogUtil;
import com.cringe_studios.code_guard.util.OTPDatabase;
import com.cringe_studios.code_guard.util.OTPDatabaseException;
import com.cringe_studios.code_guard.util.OTPParser;
import com.cringe_studios.code_guard.util.SettingsUtil;
import com.cringe_studios.code_guard.util.StyledDialogBuilder;

public class URIHandlerActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if(intent == null) {
            finishAndRemoveTask();
            return;
        }

        try {
            Uri uri = intent.getData();
            if(uri == null || uri.getScheme() == null) {
                finishAndRemoveTask();
                return;
            }

            switch(uri.getScheme().toLowerCase()) {
                case "otpauth":
                    importCodes(OTPParser.parse(uri));
                    break;
                case "otpauth-migration":
                    importCodes(OTPParser.parseMigration(uri).getOTPs());
                    // TODO: notify user if there are multiple codes?
                    break;
            }
        }catch(IllegalArgumentException e) {
            AlertDialog dialog = new StyledDialogBuilder(this)
                    .setTitle(R.string.uri_handler_failed_title)
                    .setMessage(e.getMessage())
                    .setPositiveButton(R.string.ok, (d, which) -> finishAndRemoveTask())
                    .create();

            dialog.setOnDismissListener(d -> finishAndRemoveTask());
            dialog.show();
        }
    }

    private void importCodes(OTPData... data) {
        OTPDatabase.promptLoadDatabase(this, () -> {
            DialogUtil.showChooseGroupDialog(this, group -> {
                for(OTPData d : data) {
                    OTPDatabase.getLoadedDatabase().addOTP(group, d);
                    try {
                        OTPDatabase.saveDatabase(this, SettingsUtil.getCryptoParameters(this));
                    } catch (OTPDatabaseException | CryptoException e) {
                        DialogUtil.showErrorDialog(this, e.toString());
                    }
                }
                Toast.makeText(this, R.string.uri_handler_code_added, Toast.LENGTH_SHORT).show();
            }, this::finishAndRemoveTask);
        }, null);
    }

}
