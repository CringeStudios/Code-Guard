package com.cringe_studios.cringe_authenticator.urihandler;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.model.OTPData;
import com.cringe_studios.cringe_authenticator.util.OTPParser;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;
import com.cringe_studios.cringe_authenticator.util.StyledDialogBuilder;

import java.util.List;

public class URIHandlerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if(intent == null) {
            finish();
            return;
        }

        try {
            OTPData data = OTPParser.parse(intent.getData());
            List<String> groups = SettingsUtil.getGroups(this);
            String[] groupNames = new String[groups.size()];
            for(int i = 0; i < groups.size(); i++) {
                groupNames[i] = SettingsUtil.getGroupName(this, groups.get(i));
            }

            // TODO: add option to create new group?
            AlertDialog dialog = new StyledDialogBuilder(this)
                    .setTitle(R.string.uri_handler_add_code_title)
                    .setItems(groupNames, (d, which) -> {
                        SettingsUtil.addOTP(this, groups.get(which), data);
                        Toast.makeText(this, R.string.uri_handler_code_added, Toast.LENGTH_SHORT).show();
                    })
                    .setPositiveButton(R.string.ok, (d, which) -> finish())
                    .create();

            dialog.setOnDismissListener(d -> finish());
            dialog.show();
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

}
