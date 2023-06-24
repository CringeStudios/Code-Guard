package com.cringe_studios.cringe_authenticator;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.cringe_studios.cringe_authenticator.databinding.ActivityMainBinding;
import com.cringe_studios.cringe_authenticator.databinding.DialogInputCodeChoiceBinding;
import com.cringe_studios.cringe_authenticator.databinding.DialogInputCodeTotpBinding;
import com.cringe_studios.cringe_authenticator.fragment.DynamicFragment;
import com.cringe_studios.cringe_authenticator.fragment.HomeFragment;
import com.cringe_studios.cringe_authenticator.fragment.MenuFragment;
import com.cringe_studios.cringe_authenticator.fragment.SettingsFragment;
import com.cringe_studios.cringe_authenticator.scanner.QRScannerActivity;
import com.cringe_studios.cringe_authenticator.scanner.QRScannerContract;
import com.cringe_studios.cringe_authenticator.util.NavigationUtil;
import com.cringe_studios.cringe_authenticator_library.OTPAlgorithm;
import com.cringe_studios.cringe_authenticator_library.OTPType;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private ActivityResultLauncher<Void> startQRCodeScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: load configured theme
        setTheme(R.style.Theme_CringeAuthenticator_Blue_Green);

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

        /*BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                .build();

        prompt.authenticate(info);*/

        launchApp();

        startQRCodeScan = registerForActivityResult(new QRScannerContract(), obj -> {
            if(obj == null) { // Got some error TODO: show error message
                Toast.makeText(this, "Failed to scan code", Toast.LENGTH_LONG).show();
                return;
            }

            Fragment fragment = NavigationUtil.getCurrentFragment(this);
            if(fragment instanceof DynamicFragment) {
                DynamicFragment frag = (DynamicFragment) fragment;
                frag.addOTP(obj);
            }
            Log.i("AMOGUS", "Actually got something bruh" + obj);
        });
    }

    private void launchApp() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fabMenu.setOnClickListener(view -> NavigationUtil.navigate(this, MenuFragment.class, null));
        binding.fabScan.setOnClickListener(view -> scanCode());
        binding.fabInput.setOnClickListener(view -> inputCode());
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

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
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
            // TODO: handle input
            Fragment fragment = NavigationUtil.getCurrentFragment(this);
            if(!(fragment instanceof DynamicFragment)) return;

            try {
                String name = binding.inputName.getText().toString();
                String secret = binding.inputSecret.getText().toString();
                OTPAlgorithm algorithm = (OTPAlgorithm) binding.inputAlgorithm.getSelectedItem();
                int digits = (int) binding.inputDigits.getSelectedItem();
                int period = Integer.parseInt(binding.inputPeriod.getText().toString());

                OTPData data = new OTPData(name, OTPType.TOTP, secret, algorithm, digits, period, 0);
                if(!data.validate()) {
                    // TODO: error
                    return;
                }

                ((DynamicFragment) fragment).addOTP(data);
            }catch(NumberFormatException e) {
                // TODO: error
                return;
            }
        });
    }

    private void showHOTPDialog() {
        DialogInputCodeTotpBinding binding = DialogInputCodeTotpBinding.inflate(getLayoutInflater());
        showCodeDialog(binding.getRoot(), () -> {});
    }

    private void showCodeDialog(View view, Runnable ok) {
        new AlertDialog.Builder(this)
                .setTitle("Input Code")
                .setView(view)
                .setPositiveButton("Ok", (btnView, which) -> ok.run())
                .setNegativeButton("Cancel", (btnView, which) -> {})
                .show();
    }

    public void addGroup(MenuItem item) {
        EditText t = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("New Group")
                .setView(t)
                .setPositiveButton("Add", (view, which) -> {})
                .setNegativeButton("Cancel", (view, which) -> {})
                .show();
    }

}