package com.cringe_studios.code_guard.unlock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.cringe_studios.code_guard.BaseActivity;
import com.cringe_studios.code_guard.MainActivity;
import com.cringe_studios.code_guard.R;
import com.cringe_studios.code_guard.crypto.BiometricKey;
import com.cringe_studios.code_guard.crypto.Crypto;
import com.cringe_studios.code_guard.crypto.CryptoException;
import com.cringe_studios.code_guard.databinding.ActivityUnlockBinding;
import com.cringe_studios.code_guard.util.BiometricUtil;
import com.cringe_studios.code_guard.util.DialogUtil;
import com.cringe_studios.code_guard.util.OTPDatabase;
import com.cringe_studios.code_guard.util.OTPDatabaseException;
import com.cringe_studios.code_guard.util.SettingsUtil;
import com.cringe_studios.code_guard.util.ThemeUtil;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class UnlockActivity extends BaseActivity {

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
        ThemeUtil.loadBackground(this);

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
                    DialogUtil.showErrorDialog(this, getString(R.string.error_unlock_other), e);
                }
            };

            binding.unlockBiometrics.setOnClickListener(view -> BiometricUtil.promptBiometricAuth(this, onSuccess, () -> {}));
            BiometricUtil.promptBiometricAuth(this, onSuccess, () -> {});
        }else {
            binding.unlockBiometrics.setVisibility(View.GONE);
        }

        binding.unlockButton.setOnClickListener(view -> {
            if(binding.unlockPassword.getText().length() == 0) {
                DialogUtil.showErrorDialog(this, getString(R.string.error_unlock_no_password));
                return;
            }

            String password = binding.unlockPassword.getText().toString();

            try {
                SecretKey key = Crypto.generateKey(SettingsUtil.getCryptoParameters(this), password);
                OTPDatabase.loadDatabase(this, key);
                success();
            }catch(CryptoException e) {
                DialogUtil.showErrorDialog(this, getString(R.string.error_unlock_crypto), (Runnable) null);
            } catch (OTPDatabaseException e) {
                DialogUtil.showErrorDialog(this, getString(R.string.error_unlock_other), e, this::failure);
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
