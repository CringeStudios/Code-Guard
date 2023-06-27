package com.cringe_studios.cringe_authenticator;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.cringe_studios.cringe_authenticator.databinding.ActivityMainBinding;
import com.cringe_studios.cringe_authenticator.databinding.DialogInputCodeChoiceBinding;
import com.cringe_studios.cringe_authenticator.databinding.DialogInputCodeHotpBinding;
import com.cringe_studios.cringe_authenticator.databinding.DialogInputCodeTotpBinding;
import com.cringe_studios.cringe_authenticator.fragment.DynamicFragment;
import com.cringe_studios.cringe_authenticator.fragment.HomeFragment;
import com.cringe_studios.cringe_authenticator.fragment.MenuFragment;
import com.cringe_studios.cringe_authenticator.fragment.SettingsFragment;
import com.cringe_studios.cringe_authenticator.scanner.QRScannerContract;
import com.cringe_studios.cringe_authenticator.util.DialogCallback;
import com.cringe_studios.cringe_authenticator.util.NavigationUtil;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;
import com.cringe_studios.cringe_authenticator_library.OTPAlgorithm;
import com.cringe_studios.cringe_authenticator_library.OTPType;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private ActivityResultLauncher<Void> startQRCodeScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        Integer themeID = SettingsUtil.THEMES.get(SettingsUtil.getTheme(this));
        if(themeID != null) {
            setTheme(themeID);
        }else {
            setTheme(R.style.Theme_CringeAuthenticator_Blue_Green);
        }

        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt prompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                finishAffinity();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                launchApp();
            }
        });

        if(SettingsUtil.isBiometricLock(this)) {
            BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Cringe Authenticator")
                    .setSubtitle("Unlock the authenticator")
                    .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                    .build();

            prompt.authenticate(info);
        }else {
            launchApp();
        }

        startQRCodeScan = registerForActivityResult(new QRScannerContract(), obj -> {
            if(obj == null) return; // Cancelled

            if(!obj.isSuccess()) {
                Toast.makeText(this, "Failed to scan code: " + obj.getErrorMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            Fragment fragment = NavigationUtil.getCurrentFragment(this);
            if(fragment instanceof DynamicFragment) {
                DynamicFragment frag = (DynamicFragment) fragment;
                frag.addOTP(obj.getData());
            }
            Log.i("AMOGUS", "Actually got something bruh" + obj);
        });
    }

    private void launchApp() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.fabMenu.setOnClickListener(view -> NavigationUtil.navigate(this, MenuFragment.class, null));
        binding.fabScan.setOnClickListener(view -> scanCode());
        binding.fabInput.setOnClickListener(view -> inputCode());

        NavigationUtil.navigate(this, HomeFragment.class, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        /*if(NavigationUtil.getCurrentFragment(this) instanceof DynamicFragment) { TODO: vs. fabs?
            getMenuInflater().inflate(R.menu.menu_dynamic, menu);
            return true;
        }*/

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

    public void scanCode() {
        startQRCodeScan.launch(null);
    }

    public void inputCode() {
        DialogInputCodeChoiceBinding binding = DialogInputCodeChoiceBinding.inflate(getLayoutInflater());

        String[] options = new String[2];
        options[0] = OTPType.TOTP.getFriendlyName() + " (TOTP)";
        options[1] = OTPType.HOTP.getFriendlyName() + " (HOTP)";

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select Code Type")
                .setView(binding.getRoot())
                .setNegativeButton("Cancel", (view, which) -> {})
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
        DialogInputCodeTotpBinding binding = DialogInputCodeTotpBinding.inflate(getLayoutInflater());
        binding.inputAlgorithm.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, OTPAlgorithm.values()));
        binding.inputDigits.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new Integer[]{6, 7, 8, 9, 10, 11, 12}));
        showCodeDialog(binding.getRoot(), () -> {
            Fragment fragment = NavigationUtil.getCurrentFragment(this);
            if(!(fragment instanceof DynamicFragment)) return true;

            try {
                String name = binding.inputName.getText().toString();
                String secret = binding.inputSecret.getText().toString();
                OTPAlgorithm algorithm = (OTPAlgorithm) binding.inputAlgorithm.getSelectedItem();
                int digits = (int) binding.inputDigits.getSelectedItem();
                int period = Integer.parseInt(binding.inputPeriod.getText().toString());
                boolean checksum = binding.inputChecksum.isChecked();

                OTPData data = new OTPData(name, OTPType.TOTP, secret, algorithm, digits, period, 0, checksum);

                String errorMessage = data.validate();
                if(errorMessage != null) {
                    showErrorDialog(errorMessage);
                    return false;
                }

                ((DynamicFragment) fragment).addOTP(data);
                return true;
            }catch(NumberFormatException e) {
                showErrorDialog("Invalid number entered");
                return false;
            }
        });
    }

    private void showHOTPDialog() {
        DialogInputCodeHotpBinding binding = DialogInputCodeHotpBinding.inflate(getLayoutInflater());
        binding.inputAlgorithm.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, OTPAlgorithm.values()));
        binding.inputDigits.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new Integer[]{6, 7, 8, 9, 10, 11, 12}));
        showCodeDialog(binding.getRoot(), () -> {
            Fragment fragment = NavigationUtil.getCurrentFragment(this);
            if(!(fragment instanceof DynamicFragment)) return true;

            try {
                String name = binding.inputName.getText().toString();
                String secret = binding.inputSecret.getText().toString();
                OTPAlgorithm algorithm = (OTPAlgorithm) binding.inputAlgorithm.getSelectedItem();
                int digits = (int) binding.inputDigits.getSelectedItem();
                int counter = Integer.parseInt(binding.inputCounter.getText().toString());
                boolean checksum = binding.inputChecksum.isChecked();

                OTPData data = new OTPData(name, OTPType.TOTP, secret, algorithm, digits, 0, counter, checksum);

                String errorMessage = data.validate();
                if(errorMessage != null) {
                    showErrorDialog(errorMessage);
                    return false;
                }

                ((DynamicFragment) fragment).addOTP(data);
                return true;
            }catch(NumberFormatException e) {
                showErrorDialog("Invalid number entered");
                return false;
            }
        });
    }

    private void showCodeDialog(View view, DialogCallback ok) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Input Code")
                .setView(view)
                .setPositiveButton("Ok", (btnView, which) -> {})
                .setNegativeButton("Cancel", (btnView, which) -> {})
                .create();

        dialog.setOnShowListener(d -> {
            Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            okButton.setOnClickListener(v -> {
                if(ok.callback()) dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showErrorDialog(String errorMessage) {
        new AlertDialog.Builder(this)
                .setTitle("Failed to add code")
                .setMessage(errorMessage)
                .setPositiveButton("Ok", (dialog, which) -> {})
                .show();
    }

    public void addGroup(MenuItem item) {
        EditText t = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("New Group")
                .setView(t)
                .setPositiveButton("Add", (view, which) -> {
                    if(t.getText().length() == 0) {
                        showErrorDialog("You need to input a name");
                        return;
                    }

                    Fragment frag = NavigationUtil.getCurrentFragment(this);
                    if(frag instanceof MenuFragment) {
                        ((MenuFragment) frag).addGroup(t.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", (view, which) -> {})
                .show();
    }

}