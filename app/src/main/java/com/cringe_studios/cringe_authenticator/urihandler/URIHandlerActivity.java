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
import com.cringe_studios.cringe_authenticator.util.StyledDialogBuilder;

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
            // TODO: choose group, add code
            Toast.makeText(this, "Code received", Toast.LENGTH_LONG).show();
            finish();
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
