package com.cringe_studios.cringe_authenticator.unlock;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.content.Intent;
import android.os.Bundle;
import android.security.KeyStoreParameter;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProtection;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.cringe_studios.cringe_authenticator.BaseActivity;
import com.cringe_studios.cringe_authenticator.MainActivity;
import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.crypto.BiometricKey;
import com.cringe_studios.cringe_authenticator.crypto.Crypto;
import com.cringe_studios.cringe_authenticator.crypto.CryptoException;
import com.cringe_studios.cringe_authenticator.databinding.ActivityUnlockBinding;
import com.cringe_studios.cringe_authenticator.util.BiometricUtil;
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
import javax.crypto.spec.SecretKeySpec;

public class UnlockActivity extends BaseActivity {

    private static final long LOCK_TIMEOUT = 10000;

    private ActivityUnlockBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!SettingsUtil.isDatabaseEncrypted(this)) {
            success();
            return;
        }

        binding = ActivityUnlockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if(SettingsUtil.isBiometricEncryption(this) && BiometricUtil.isSupported(this)) {
            Runnable onSuccess = () -> {
                BiometricKey biometricKey = SettingsUtil.getBiometricKey(this);
                try {
                    SecretKey biometricSecretKey = Crypto.getBiometricKey(biometricKey);
                    byte[] keyBytes = Crypto.decrypt(SettingsUtil.getCryptoParameters(this), biometricKey.getEncryptedKey(), biometricSecretKey, biometricKey.getIV());
                    SecretKey key = new SecretKeySpec(keyBytes, "AES");
                    OTPDatabase.loadDatabase(this, key);
                    success();
                } catch (CryptoException | OTPDatabaseException e) {
                    DialogUtil.showErrorDialog(this, "Failed to load database: " + e);
                }
            };

            binding.unlockBiometrics.setOnClickListener(view -> BiometricUtil.promptBiometricAuth(this, onSuccess, () -> {}));
            BiometricUtil.promptBiometricAuth(this, onSuccess, () -> {});
        }else {
            binding.unlockBiometrics.setVisibility(View.GONE);
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
