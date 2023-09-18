package com.cringe_studios.cringe_authenticator.unlock;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.content.Intent;
import android.os.Bundle;
import android.security.KeyStoreParameter;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProtection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.cringe_studios.cringe_authenticator.MainActivity;
import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.crypto.Crypto;
import com.cringe_studios.cringe_authenticator.crypto.CryptoException;
import com.cringe_studios.cringe_authenticator.databinding.ActivityUnlockBinding;
import com.cringe_studios.cringe_authenticator.util.DialogUtil;
import com.cringe_studios.cringe_authenticator.util.OTPDatabase;
import com.cringe_studios.cringe_authenticator.util.OTPDatabaseException;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;
import com.cringe_studios.cringe_authenticator.util.ThemeUtil;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executor;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class UnlockActivity extends AppCompatActivity {

    private static final long LOCK_TIMEOUT = 10000;

    private ActivityUnlockBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeUtil.loadTheme(this);

        if(!SettingsUtil.isDatabaseEncrypted(this)) {
            success();
            return;
        }

        binding = ActivityUnlockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            SecretKey key = KeyGenerator.getInstance("AES").generateKey();
            ks.setEntry("amogus", new KeyStore.SecretKeyEntry(key), new KeyStoreParameter.Builder(this).build());
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        if(SettingsUtil.isBiometricLock(this)) {
            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt prompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    failure();
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    success();
                }
            });

            boolean supportsBiometricAuth = BiometricManager.from(this).canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS;
            boolean recentlyUnlocked = savedInstanceState != null && (System.currentTimeMillis() - savedInstanceState.getLong("pauseTime", 0L) < LOCK_TIMEOUT);

            if(!recentlyUnlocked && SettingsUtil.isBiometricLock(this) && supportsBiometricAuth) {
                BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle(getString(R.string.app_name))
                        .setSubtitle(getString(R.string.biometric_lock_subtitle))
                        .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                        .build();

                //prompt.authenticate(info);

                binding.unlockBiometrics.setOnClickListener(view -> {
                    //prompt.authenticate(info);
                });
            }else {
                success();
            }
        }

        binding.unlockButton.setOnClickListener(view -> {
            if(binding.unlockPassword.getText().length() == 0) {
                DialogUtil.showErrorDialog(this, "You need to enter a password");
                return;
            }

            String password = binding.unlockPassword.getText().toString();

            try {
                SecretKey key = Crypto.generateKey(SettingsUtil.getCryptoParameters(this), password);
                OTPDatabase.loadDatabase(this, key);
                success();
            }catch(CryptoException e) {
                DialogUtil.showErrorDialog(this, "Failed to load database: Invalid password or database corrupted", this::failure);
            } catch (OTPDatabaseException e) {
                DialogUtil.showErrorDialog(this, "Failed to load database: " + e, this::failure);
            }
        });
    }

    private void success() {
        if(getIntent() != null && getIntent().hasExtra("contract")) {
            setResult(RESULT_OK);
            finish();
            return;
        }

        Intent m = new Intent(getApplicationContext(), MainActivity.class);
        m.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(m);
        finish();
    }

    private void failure() {
        setResult(RESULT_CANCELED);
        finish();
    }

}
