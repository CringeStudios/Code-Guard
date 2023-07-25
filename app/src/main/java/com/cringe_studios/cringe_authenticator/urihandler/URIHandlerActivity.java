package com.cringe_studios.cringe_authenticator.urihandler;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

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
            finishAndRemoveTask();
            return;
        }

        try {
            Uri uri = intent.getData();
            switch(uri.getScheme().toLowerCase()) {
                case "otpauth":
                    importCodes(OTPParser.parse(uri));
                    break;
                case "otpauth-migration":
                    importCodes(OTPParser.parseMigration(uri));
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
        DialogUtil.showChooseGroupDialog(this, group -> {
            for(OTPData d : data) SettingsUtil.addOTP(this, group, d);
            Toast.makeText(this, R.string.uri_handler_code_added, Toast.LENGTH_SHORT).show();
        }, this::finishAndRemoveTask);
    }

}
