package com.cringe_studios.cringe_authenticator.util;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.cringe_studios.cringe_authenticator.R;

import java.util.concurrent.Executor;

public class BiometricUtil {

    public static boolean isSupported(Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static void promptBiometricAuth(FragmentActivity context, Runnable success, Runnable failure) {
        if(!isSupported(context)) {
            failure.run();
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(context);
        BiometricPrompt prompt = new BiometricPrompt(context, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                failure.run();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                success.run();
            }
        });

        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(context.getString(R.string.app_name))
                .setSubtitle(context.getString(R.string.biometric_lock_subtitle))
                .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                .build();

        prompt.authenticate(info);
    }

}
