package com.cringe_studios.cringe_authenticator.fragment;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;

import com.cringe_studios.cringe_authenticator.MainActivity;
import com.cringe_studios.cringe_authenticator.crypto.BiometricKey;
import com.cringe_studios.cringe_authenticator.crypto.Crypto;
import com.cringe_studios.cringe_authenticator.crypto.CryptoException;
import com.cringe_studios.cringe_authenticator.crypto.CryptoParameters;
import com.cringe_studios.cringe_authenticator.databinding.FragmentSettingsBinding;
import com.cringe_studios.cringe_authenticator.util.BiometricUtil;
import com.cringe_studios.cringe_authenticator.util.DialogUtil;
import com.cringe_studios.cringe_authenticator.util.FabUtil;
import com.cringe_studios.cringe_authenticator.util.OTPDatabase;
import com.cringe_studios.cringe_authenticator.util.OTPDatabaseException;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import javax.crypto.SecretKey;

public class SettingsFragment extends NamedFragment {

    private FragmentSettingsBinding binding;

    @Override
    public String getName() {
        return "Settings";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater);

        Locale[] locales = new Locale[] {Locale.ENGLISH, Locale.GERMAN};

        String[] localeNames = new String[locales.length];
        for(int i = 0; i < locales.length; i++) {
            localeNames[i] = locales[i].getDisplayName(locales[i]);
        }

        binding.settingsLanguage.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, localeNames));
        binding.settingsLanguage.setSelection(Arrays.asList(locales).indexOf(SettingsUtil.getLocale(requireContext())));
        binding.settingsLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Locale locale = locales[position];
                if(locale.equals(SettingsUtil.getLocale(requireContext()))) return;

                SettingsUtil.setLocale(requireContext(), locale);
                ((MainActivity) requireActivity()).setLocale(locale);
                requireActivity().recreate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.settingsEnableEncryption.setChecked(SettingsUtil.isDatabaseEncrypted(requireContext()));
        binding.settingsEnableEncryption.setOnCheckedChangeListener((view, checked) -> {
            if(!OTPDatabase.isDatabaseLoaded()) {
                // TODO: prompt user
            }

            if(checked) {
                DialogUtil.showInputPasswordDialog(requireContext(), password -> {
                    CryptoParameters params = CryptoParameters.createNew();
                    Log.d("Crypto", "Created new crypto params");

                    try {
                        SecretKey key = Crypto.generateKey(params, password);
                        Log.d("Crypto", "Generated key");
                        OTPDatabase.encrypt(requireContext(), key, params);
                        SettingsUtil.enableEncryption(requireContext(), params);
                        Log.d("Crypto", "DB encryption enabled");
                    } catch (CryptoException | OTPDatabaseException e) {
                        throw new RuntimeException(e); // TODO
                    }
                }, null);
            }else {
                try {
                    OTPDatabase.decrypt(requireContext());
                    SettingsUtil.disableEncryption(requireContext());
                    Log.d("Crypto", "DB encryption disabled");
                } catch (OTPDatabaseException | CryptoException e) {
                    throw new RuntimeException(e); // TODO
                }
            }
        });

        binding.settingsEnableIntroVideo.setChecked(SettingsUtil.isIntroVideoEnabled(requireContext()));
        binding.settingsEnableIntroVideo.setOnCheckedChangeListener((view, checked) -> SettingsUtil.setEnableIntroVideo(requireContext(), checked));

        if(SettingsUtil.isDatabaseEncrypted(requireContext())
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && BiometricManager.from(requireContext()).canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS) {
            binding.settingsBiometricLock.setChecked(SettingsUtil.isBiometricEncryption(requireContext()));
            binding.settingsBiometricLock.setOnCheckedChangeListener((view, checked) -> {
                if(checked) {
                    OTPDatabase.promptLoadDatabase(requireActivity(), () -> {
                        BiometricUtil.promptBiometricAuth(requireActivity(), () -> {
                            try {
                                BiometricKey biometricKey = Crypto.createBiometricKey(SettingsUtil.getCryptoParameters(requireContext()));
                                SettingsUtil.enableBiometricEncryption(requireContext(), biometricKey);
                            } catch (CryptoException e) {
                                DialogUtil.showErrorDialog(requireContext(), "Failed to enable: " + e);
                            }
                        }, () -> view.setChecked(false));
                    }, null);
                }else {
                    try {
                        BiometricKey key = SettingsUtil.getBiometricKey(requireContext());
                        if(key != null) Crypto.deleteBiometricKey(key);
                    } catch (CryptoException e) {
                        DialogUtil.showErrorDialog(requireContext(), "Failed to delete key: " + e);
                    }

                    SettingsUtil.disableBiometricEncryption(requireContext());
                }
            });
        }else {
            binding.settingsBiometricLock.setChecked(false);
            binding.settingsBiometricLock.setEnabled(false);
            // TODO: inform user
        }

        binding.settingsTheme.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, SettingsUtil.THEME_NAMES.toArray(new String[0])));
        binding.settingsTheme.setSelection(SettingsUtil.THEME_NAMES.indexOf(SettingsUtil.getTheme(requireContext())));
        binding.settingsTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String theme = SettingsUtil.THEME_NAMES.get(position);
                if(theme.equals(SettingsUtil.getTheme(requireContext()))) return;

                SettingsUtil.setTheme(requireContext(), theme);

                Integer themeID = SettingsUtil.THEMES.get(theme);
                if(themeID == null) return;

                requireActivity().setTheme(themeID);
                requireActivity().recreate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        FabUtil.hideFabs(requireActivity());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

}
