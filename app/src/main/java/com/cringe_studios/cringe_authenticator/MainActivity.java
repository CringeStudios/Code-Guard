package com.cringe_studios.cringe_authenticator;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.cringe_studios.cringe_authenticator.databinding.ActivityMainBinding;
import com.cringe_studios.cringe_authenticator.databinding.DialogInputCodeChoiceBinding;
import com.cringe_studios.cringe_authenticator.fragment.AboutFragment;
import com.cringe_studios.cringe_authenticator.fragment.GroupFragment;
import com.cringe_studios.cringe_authenticator.fragment.HomeFragment;
import com.cringe_studios.cringe_authenticator.fragment.MenuFragment;
import com.cringe_studios.cringe_authenticator.fragment.NamedFragment;
import com.cringe_studios.cringe_authenticator.fragment.SettingsFragment;
import com.cringe_studios.cringe_authenticator.model.OTPData;
import com.cringe_studios.cringe_authenticator.scanner.QRScanner;
import com.cringe_studios.cringe_authenticator.scanner.QRScannerContract;
import com.cringe_studios.cringe_authenticator.util.DialogUtil;
import com.cringe_studios.cringe_authenticator.util.NavigationUtil;
import com.cringe_studios.cringe_authenticator.util.OTPDatabase;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;
import com.cringe_studios.cringe_authenticator.util.StyledDialogBuilder;
import com.cringe_studios.cringe_authenticator.util.ThemeUtil;
import com.cringe_studios.cringe_authenticator_library.OTPType;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    private ActivityResultLauncher<Void> startQRCodeScan;

    private ActivityResultLauncher<PickVisualMediaRequest> pickQRCodeImage;

    private QRScanner qrScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE); TODO: enable secure flag

        ThemeUtil.loadTheme(this);

        setLocale(SettingsUtil.getLocale(this));

        qrScanner = new QRScanner();

        startQRCodeScan = registerForActivityResult(new QRScannerContract(), obj -> {
            if(obj == null) return; // Cancelled

            if(!obj.isSuccess()) {
                Toast.makeText(this, getString(R.string.qr_scanner_failed, obj.getErrorMessage()), Toast.LENGTH_LONG).show();
                return;
            }

            Fragment fragment = NavigationUtil.getCurrentFragment(this);
            if(fragment instanceof GroupFragment) {
                GroupFragment frag = (GroupFragment) fragment;
                for(OTPData d : obj.getData()) frag.addOTP(d);
            }
        });

        pickQRCodeImage = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), img -> {
            if(img == null) return;

            try {
                InputImage image = InputImage.fromFilePath(this, img);
                qrScanner.scan(image, code -> {
                    if(code == null) {
                        DialogUtil.showErrorDialog(this, "No codes were detected in the provided image");
                        return;
                    }

                    if(code.isMigrationPart()) {
                        new StyledDialogBuilder(this) // TODO: duplicated from QRScannerActivity
                                .setTitle(R.string.qr_scanner_migration_title)
                                .setMessage(R.string.qr_scanner_migration_message)
                                .setPositiveButton(R.string.yes, (d, which) -> {
                                    Fragment fragment = NavigationUtil.getCurrentFragment(this);
                                    if (fragment instanceof GroupFragment) {
                                        GroupFragment frag = (GroupFragment) fragment;
                                        for (OTPData dt : code.getOTPs()) frag.addOTP(dt);
                                    }
                                })
                                .setNegativeButton(R.string.no, (d, which) -> {})
                                .show()
                                .setCanceledOnTouchOutside(false);
                    }else {
                        Fragment fragment = NavigationUtil.getCurrentFragment(this);
                        if (fragment instanceof GroupFragment) {
                            GroupFragment frag = (GroupFragment) fragment;
                            for (OTPData dt : code.getOTPs()) frag.addOTP(dt);
                        }
                    }
                }, error -> DialogUtil.showErrorDialog(this, "Failed to detect code: " + error));
            } catch (IOException e) {
                DialogUtil.showErrorDialog(this, "Failed to read image: " + e);
            }
        });

        OTPDatabase.promptLoadDatabase(this, this::launchApp, this::finishAffinity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qrScanner.close();
    }

    public void setLocale(Locale locale) {
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void launchApp() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.fabMenu.setOnClickListener(view -> NavigationUtil.navigate(this, MenuFragment.class, null));
        binding.fabScan.setOnClickListener(view -> scanCode());
        binding.fabScanImage.setOnClickListener(view -> scanCodeFromImage());
        binding.fabInput.setOnClickListener(view -> inputCode());

        Fragment fragment = NavigationUtil.getCurrentFragment(this);
        if(fragment instanceof NamedFragment) {
            ActionBar bar = getSupportActionBar();
            if(bar != null) bar.setTitle(((NamedFragment) fragment).getName());
        }else {
            NavigationUtil.navigate(this, HomeFragment.class, null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(NavigationUtil.getCurrentFragment(this) instanceof MenuFragment) {
            getMenuInflater().inflate(R.menu.menu_groups, menu);
            return true;
        }

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(!(NavigationUtil.getCurrentFragment(this) instanceof HomeFragment)) {
            NavigationUtil.navigate(this, HomeFragment.class, null);
        }
    }

    public void openSettings(MenuItem item) {
        NavigationUtil.navigate(this, SettingsFragment.class, null);
    }

    public void openAbout(MenuItem item) {
        NavigationUtil.navigate(this, AboutFragment.class, null);
    }

    public void scanCode() {
        startQRCodeScan.launch(null);
    }

    public void scanCodeFromImage() {
        pickQRCodeImage.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    public void inputCode() {
        DialogInputCodeChoiceBinding binding = DialogInputCodeChoiceBinding.inflate(getLayoutInflater());

        String[] options = new String[2];
        options[0] = OTPType.TOTP.getFriendlyName() + " (TOTP)";
        options[1] = OTPType.HOTP.getFriendlyName() + " (HOTP)";

        AlertDialog dialog = new StyledDialogBuilder(this)
                .setTitle(R.string.create_totp_title)
                .setView(binding.getRoot())
                .setNegativeButton(R.string.cancel, (view, which) -> {})
                .create();

        binding.codeTypes.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, options));
        binding.codeTypes.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            switch(position) {
                case 0:
                    showTOTPDialog();
                    break;
                case 1:
                    showHOTPDialog();
                    break;
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void showTOTPDialog() {
        DialogUtil.showTOTPDialog(getLayoutInflater(), null, data -> {
            Fragment fragment = NavigationUtil.getCurrentFragment(this);
            if(!(fragment instanceof GroupFragment)) return;

            ((GroupFragment) fragment).addOTP(data);
        }, () -> inputCode(), false);
    }

    private void showHOTPDialog() {
        DialogUtil.showHOTPDialog(getLayoutInflater(), null, data -> {
            Fragment fragment = NavigationUtil.getCurrentFragment(this);
            if(!(fragment instanceof GroupFragment)) return;

            ((GroupFragment) fragment).addOTP(data);
        }, () -> inputCode(), false);
    }

    public void addGroup(MenuItem item) {
        DialogUtil.showCreateGroupDialog(getLayoutInflater(), null, group -> {
            Fragment frag = NavigationUtil.getCurrentFragment(this);
            if(frag instanceof MenuFragment) {
                ((MenuFragment) frag).addGroup(group);
            }
        }, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}