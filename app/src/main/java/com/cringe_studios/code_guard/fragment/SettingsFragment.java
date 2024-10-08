package com.cringe_studios.code_guard.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cringe_studios.code_guard.MainActivity;
import com.cringe_studios.code_guard.R;
import com.cringe_studios.code_guard.backup.BackupData;
import com.cringe_studios.code_guard.backup.BackupUtil;
import com.cringe_studios.code_guard.crypto.BiometricKey;
import com.cringe_studios.code_guard.crypto.Crypto;
import com.cringe_studios.code_guard.crypto.CryptoException;
import com.cringe_studios.code_guard.crypto.CryptoParameters;
import com.cringe_studios.code_guard.databinding.DialogDownloadIconPacksBinding;
import com.cringe_studios.code_guard.databinding.DialogManageIconPacksBinding;
import com.cringe_studios.code_guard.databinding.FragmentSettingsBinding;
import com.cringe_studios.code_guard.icon.DownloadIconPackListAdapter;
import com.cringe_studios.code_guard.icon.DownloadableIconPack;
import com.cringe_studios.code_guard.icon.IconPack;
import com.cringe_studios.code_guard.icon.IconPackListAdapter;
import com.cringe_studios.code_guard.icon.IconUtil;
import com.cringe_studios.code_guard.otplist.HiddenStyle;
import com.cringe_studios.code_guard.util.AppLocale;
import com.cringe_studios.code_guard.util.Appearance;
import com.cringe_studios.code_guard.util.BackupException;
import com.cringe_studios.code_guard.util.BiometricUtil;
import com.cringe_studios.code_guard.util.DialogUtil;
import com.cringe_studios.code_guard.util.OTPDatabase;
import com.cringe_studios.code_guard.util.OTPDatabaseException;
import com.cringe_studios.code_guard.util.SettingsUtil;
import com.cringe_studios.code_guard.util.StyledDialogBuilder;
import com.cringe_studios.code_guard.util.Theme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKey;

public class SettingsFragment extends NamedFragment {

    private FragmentSettingsBinding binding;

    @Override
    public String getName() {
        return requireActivity().getString(R.string.fragment_settings);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater);


        String[] localeNames = new String[AppLocale.values().length];
        for(int i = 0; i < localeNames.length;  i++) {
            localeNames[i] = AppLocale.values()[i].getName(requireContext());
        }

        binding.settingsLanguage.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, localeNames));
        binding.settingsLanguage.setSelection(Arrays.asList(AppLocale.values()).indexOf(SettingsUtil.getLocale(requireContext())));
        binding.settingsLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppLocale locale = AppLocale.values()[position];
                if(locale.equals(SettingsUtil.getLocale(requireContext()))) return;

                SettingsUtil.setLocale(requireContext(), locale);
                requireActivity().recreate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.settingsEnableEncryption.setChecked(SettingsUtil.isDatabaseEncrypted(requireContext()));
        binding.settingsEnableEncryption.setOnCheckedChangeListener((view, checked) -> {
            if(checked) {
                if(SettingsUtil.isDatabaseEncrypted(requireContext())) return;

                DialogUtil.showSetPasswordDialog(requireContext(), password -> {
                    CryptoParameters params = CryptoParameters.createNew();
                    Log.d("Crypto", "Created new crypto params");

                    try {
                        SecretKey key = Crypto.generateKey(params, password);
                        Log.d("Crypto", "Generated key");
                        OTPDatabase.encrypt(requireContext(), key, params);
                        SettingsUtil.enableEncryption(requireContext(), params);
                        Log.d("Crypto", "DB encryption enabled");

                        if(BiometricUtil.isSupported(requireContext())) {
                            binding.settingsBiometricLock.setEnabled(true);
                        }
                    } catch (CryptoException | OTPDatabaseException e) {
                        DialogUtil.showErrorDialog(requireContext(), getString(R.string.error_enable_encryption), e);
                    }
                }, () -> view.setChecked(false));
            }else {
                if(!SettingsUtil.isDatabaseEncrypted(requireContext())) return;

                DialogUtil.showYesNo(requireContext(), R.string.disable_encryption_title, R.string.disable_encryption_message, () -> {
                    try {
                        OTPDatabase.decrypt(requireContext());
                        SettingsUtil.disableEncryption(requireContext());
                        Log.d("Crypto", "DB encryption disabled");

                        binding.settingsBiometricLock.setChecked(false);
                        binding.settingsBiometricLock.setEnabled(false);
                    } catch (OTPDatabaseException | CryptoException e) {
                        DialogUtil.showErrorDialog(requireContext(), getString(R.string.error_disable_encryption), e);
                    }
                }, () -> view.setChecked(true));
            }
        });

        boolean biometricSupported = BiometricUtil.isSupported(requireContext());
        binding.settingsBiometricLock.setEnabled(SettingsUtil.isDatabaseEncrypted(requireContext()) && biometricSupported);
        binding.settingsBiometricLock.setChecked(SettingsUtil.isBiometricEncryption(requireContext()));

        if(!biometricSupported) {
            binding.settingsBiometricLockInfo.setVisibility(View.VISIBLE);
            binding.settingsBiometricLockInfo.setText(R.string.biometric_encryption_unavailable);
        }

        if(biometricSupported) {
            binding.settingsBiometricLock.setOnCheckedChangeListener((view, checked) -> {
                if(checked) {
                    OTPDatabase.promptLoadDatabase(requireActivity(), () -> {
                        BiometricUtil.promptBiometricAuth(requireActivity(), () -> {
                            try {
                                BiometricKey biometricKey = Crypto.createBiometricKey(SettingsUtil.getCryptoParameters(requireContext()));
                                SettingsUtil.enableBiometricEncryption(requireContext(), biometricKey);
                            } catch (CryptoException e) {
                                DialogUtil.showErrorDialog(requireContext(), getString(R.string.error_biometric_encryption_enable), e);
                            }
                        }, () -> view.setChecked(false));
                    }, null);
                }else {
                    try {
                        BiometricKey key = SettingsUtil.getBiometricKey(requireContext());
                        if(key != null) Crypto.deleteBiometricKey(key);
                    } catch (CryptoException e) {
                        DialogUtil.showErrorDialog(requireContext(), getString(R.string.error_biometric_encryption_disable), e);
                    }

                    SettingsUtil.disableBiometricEncryption(requireContext());
                }
            });
        }

        binding.settingsScreenSecurity.setChecked(SettingsUtil.isScreenSecurity(requireContext()));
        binding.settingsScreenSecurity.setOnCheckedChangeListener((view, checked) -> {
            SettingsUtil.setScreenSecurity(requireContext(), checked);
            requireActivity().recreate();
        });

        binding.settingsHideCodes.setChecked(SettingsUtil.isHideCodes(requireContext()));
        binding.settingsHideCodes.setOnCheckedChangeListener((view, checked) -> SettingsUtil.setHideCodes(requireContext(), checked));

        String[] styleNames = new String[HiddenStyle.values().length];
        for(int i = 0; i < HiddenStyle.values().length; i++) {
            styleNames[i] = getResources().getString(HiddenStyle.values()[i].getName());
        }

        binding.settingsHiddenStyle.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, styleNames));
        binding.settingsHiddenStyle.setSelection(SettingsUtil.getHiddenStyle(requireContext()).ordinal());
        binding.settingsHiddenStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                HiddenStyle style = HiddenStyle.values()[position];
                if(style == SettingsUtil.getHiddenStyle(requireContext())) return;

                SettingsUtil.setHiddenStyle(requireContext(), style);
                //requireActivity().recreate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.settingsShowImages.setChecked(SettingsUtil.isShowImages(requireContext()));
        binding.settingsShowImages.setOnCheckedChangeListener((view, checked) -> SettingsUtil.setShowImages(requireContext(), checked));

        binding.settingsEnableIntroVideo.setChecked(SettingsUtil.isIntroVideoEnabled(requireContext()));
        binding.settingsEnableIntroVideo.setOnCheckedChangeListener((view, checked) -> SettingsUtil.setEnableIntroVideo(requireContext(), checked));

        binding.settingsEnableThemedBackground.setChecked(SettingsUtil.isThemedBackgroundEnabled(requireContext()));
        binding.settingsEnableThemedBackground.setOnCheckedChangeListener((view, checked) -> {
            SettingsUtil.setEnableThemedBackground(requireContext(), checked);
            requireActivity().recreate();
        });

        List<Theme> themes = new ArrayList<>(Arrays.asList(Theme.values()));
        if(!SettingsUtil.isSuperSecretSettingsEnabled(requireContext())) themes.remove(Theme.KETCHUP_MUSTARD);
        int selectedIndex = themes.indexOf(SettingsUtil.getTheme(requireContext()));

        String[] themeNames = new String[themes.size()];
        for(int i = 0; i < themes.size(); i++) {
            themeNames[i] = getResources().getString(themes.get(i).getName());
        }

        binding.settingsTheme.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, themeNames));
        binding.settingsTheme.setSelection(selectedIndex == -1 ? 0 : selectedIndex);
        binding.settingsTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Theme theme = themes.get(position);
                if(theme == SettingsUtil.getTheme(requireContext())) return;

                SettingsUtil.setTheme(requireContext(), theme);
                requireActivity().recreate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.settingsEnableMinimalistTheme.setChecked(SettingsUtil.isMinimalistThemeEnabled(requireContext()));
        binding.settingsEnableMinimalistTheme.setOnCheckedChangeListener((view, checked) -> {
            SettingsUtil.setEnableMinimalistTheme(requireContext(), checked);
            requireActivity().recreate();
        });

        String[] appearanceNames = new String[Appearance.values().length];
        for(int i = 0; i < Appearance.values().length; i++) {
            appearanceNames[i] = getResources().getString(Appearance.values()[i].getName());
        }

        binding.settingsAppearance.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, appearanceNames));
        binding.settingsAppearance.setSelection(SettingsUtil.getAppearance(requireContext()).ordinal());
        binding.settingsAppearance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Appearance appearance = Appearance.values()[position];
                if(appearance == SettingsUtil.getAppearance(requireContext())) return;

                SettingsUtil.setAppearance(requireContext(), appearance);
                requireActivity().recreate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.settingsSearchEverywhere.setChecked(SettingsUtil.isSearchEverywhere(requireContext()));
        binding.settingsSearchEverywhere.setOnCheckedChangeListener((view, checked) -> SettingsUtil.setSearchEverywhere(requireContext(), checked));

        binding.settingsGroupSize.setValue(SettingsUtil.getDigitGroupSize(requireContext()));
        binding.settingsGroupSize.addOnChangeListener((view, value, fromUser) -> SettingsUtil.setDigitGroupSize(requireContext(), (int) value));

        binding.settingsShowNextCode.setChecked(SettingsUtil.isShowNextCode(requireContext()));
        binding.settingsShowNextCode.setOnCheckedChangeListener((view, checked) -> SettingsUtil.setShowNextCode(requireContext(), checked));

        binding.settingsCreateBackup.setOnClickListener(view -> {
            new StyledDialogBuilder(requireContext())
                    .setTitle(R.string.create_backup)
                    .setItems(R.array.backup_create, (d, which) -> {
                        switch(which) {
                            case 0:
                                if(!SettingsUtil.isDatabaseEncrypted(requireContext())) {
                                    DialogUtil.showErrorDialog(requireContext(), getString(R.string.error_backup_database_not_encrypted));
                                    return;
                                }

                                OTPDatabase.promptLoadDatabase(requireActivity(), () -> {
                                    SecretKey key = OTPDatabase.getLoadedKey();
                                    CryptoParameters parameters = SettingsUtil.getCryptoParameters(requireContext());
                                    createBackup(key, parameters);
                                }, null);
                                break;
                            case 1:
                                DialogUtil.showSetPasswordDialog(requireContext(), password -> {
                                    CryptoParameters parameters = CryptoParameters.createNew();
                                    try {
                                        SecretKey key = Crypto.generateKey(parameters, password);
                                        createBackup(key, parameters);
                                    } catch (CryptoException e) {
                                        DialogUtil.showErrorDialog(requireContext(), e.toString());
                                    }
                                }, null);
                                break;
                        }
                    })
                    .setNegativeButton(R.string.cancel, (d, which) -> {})
                    .show();
        });

        binding.settingsLoadBackup.setOnClickListener(view -> {
            ((MainActivity) requireActivity()).promptPickBackupFileLoad(uri -> {
                if(uri == null || uri.getPath() == null) return;

                loadBackup(uri);
            });
        });

        binding.settingsLoadIconPack.setOnClickListener(v -> ((MainActivity) requireActivity()).promptPickIconPackFile());

        binding.settingsDownloadIconPacks.setOnClickListener(v -> {
            DialogDownloadIconPacksBinding binding = DialogDownloadIconPacksBinding.inflate(getLayoutInflater());

            binding.downloadIconPacksList.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.downloadIconPacksList.setAdapter(new DownloadIconPackListAdapter(requireContext(), Arrays.asList(DownloadableIconPack.values())));

            new StyledDialogBuilder(requireContext())
                    .setTitle(R.string.download_icon_packs_title)
                    .setView(binding.getRoot())
                    .setPositiveButton(R.string.ok, (d, which) -> {})
                    .show();
        });

        binding.settingsManageIconPacks.setOnClickListener(v -> {
            List<String> brokenPacks = new ArrayList<>();
            List<IconPack> packs = IconUtil.loadAllIconPacks(requireContext(), brokenPacks::add);

            if(!brokenPacks.isEmpty()) {
                DialogUtil.showYesNo(requireContext(), R.string.broken_icon_packs_title, R.string.broken_icon_packs_message, () -> {
                    for(String pack : brokenPacks) {
                        IconUtil.removeIconPack(requireContext(), pack);
                    }
                }, null);
            }

            if(packs.isEmpty()) {
                Toast.makeText(requireContext(), R.string.no_icon_packs_installed, Toast.LENGTH_LONG).show();
                return;
            }

            DialogManageIconPacksBinding binding = DialogManageIconPacksBinding.inflate(getLayoutInflater());

            binding.manageIconPacksList.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.manageIconPacksList.setAdapter(new IconPackListAdapter(requireContext(), IconUtil.loadAllIconPacks(requireContext())));

            new StyledDialogBuilder(requireContext())
                    .setTitle(R.string.manage_icon_packs_title)
                    .setView(binding.getRoot())
                    .setPositiveButton(R.string.ok, (d, which) -> {})
                    .show();
        });

        binding.settingsSuperSecretSettings.setVisibility(SettingsUtil.isSuperSecretSettingsEnabled(requireContext()) ? View.VISIBLE : View.GONE);

        binding.settingsHamburgerMode.setChecked(SettingsUtil.isHamburgerModeEnabled(requireContext()));
        binding.settingsHamburgerMode.setOnCheckedChangeListener((view, checked) -> {
            SettingsUtil.setEnableHamburgerMode(requireContext(), checked);
            requireActivity().recreate();
        });

        binding.settingsUseCringeIcon.setChecked(SettingsUtil.isCringeIconEnabled(requireContext()));
        binding.settingsUseCringeIcon.setOnCheckedChangeListener((view, checked) -> {
            SettingsUtil.setEnableCringeIcon(requireContext(), checked);
            ((MainActivity) requireActivity()).updateIcon();
        });

        return binding.getRoot();
    }

    private void createBackup(SecretKey key, CryptoParameters parameters) {
        ((MainActivity) requireActivity()).promptPickBackupFileSave(BackupUtil.getBackupName(), uri -> {
            if(uri == null || uri.getPath() == null) return;

            try {
                BackupUtil.saveBackup(requireContext(), uri, key, parameters);
            } catch (BackupException | CryptoException e) {
                DialogUtil.showErrorDialog(requireContext(), e.toString());
            }
        });
    }

    private void loadBackup(Uri uri) {
        DialogUtil.showYesNo(requireContext(), R.string.load_backup_title, R.string.backup_load_message, () -> {
            OTPDatabase.promptLoadDatabase(requireActivity(), () -> {
                if(SettingsUtil.isDatabaseEncrypted(requireContext())) {
                    try {
                        SecretKey key = OTPDatabase.getLoadedKey();
                        CryptoParameters parameters = SettingsUtil.getCryptoParameters(requireContext());
                        loadBackup(uri, key, parameters);
                        return;
                    } catch (CryptoException ignored) { // Load with password
                    } catch (BackupException | OTPDatabaseException e) {
                        DialogUtil.showErrorDialog(requireContext(), getString(R.string.error_backup_load_other), e);
                        return;
                    }
                }

                DialogUtil.showInputPasswordDialog(requireContext(), password -> {
                    try {
                        CryptoParameters parameters = BackupUtil.loadParametersFromBackup(requireContext(), uri);
                        SecretKey key = Crypto.generateKey(parameters, password);
                        loadBackup(uri, key, parameters);
                    } catch (CryptoException e) {
                        DialogUtil.showErrorDialog(requireContext(), getString(R.string.error_backup_load_crypto), e);
                    } catch (BackupException | OTPDatabaseException e) {
                        DialogUtil.showErrorDialog(requireContext(), getString(R.string.error_backup_load_other), e);
                    }
                }, null);
            }, null);
        }, null);
    }

    private void loadBackup(Uri uri, SecretKey key, CryptoParameters parameters) throws BackupException, OTPDatabaseException, CryptoException {
        BackupData data = BackupUtil.loadBackup(requireContext(), uri);
        OTPDatabase db = data.loadDatabase(key, parameters);
        OTPDatabase.promptLoadDatabase(requireActivity(), () -> {
            OTPDatabase oldDatabase = OTPDatabase.getLoadedDatabase();
            try {
                SettingsUtil.restoreGroups(requireContext(), data.getGroups());
                OTPDatabase.setLoadedDatabase(db);
                OTPDatabase.saveDatabase(requireContext(), SettingsUtil.getCryptoParameters(requireContext()));

                DialogUtil.showBackupLoadedDialog(requireContext(), data);
            } catch (OTPDatabaseException | CryptoException e) {
                OTPDatabase.setLoadedDatabase(oldDatabase);
                throw new RuntimeException(e);
            }
        }, null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

}
