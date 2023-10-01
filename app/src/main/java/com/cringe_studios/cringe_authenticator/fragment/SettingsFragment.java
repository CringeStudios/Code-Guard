package com.cringe_studios.cringe_authenticator.fragment;

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

import com.cringe_studios.cringe_authenticator.MainActivity;
import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.backup.BackupData;
import com.cringe_studios.cringe_authenticator.backup.BackupUtil;
import com.cringe_studios.cringe_authenticator.crypto.BiometricKey;
import com.cringe_studios.cringe_authenticator.crypto.Crypto;
import com.cringe_studios.cringe_authenticator.crypto.CryptoException;
import com.cringe_studios.cringe_authenticator.crypto.CryptoParameters;
import com.cringe_studios.cringe_authenticator.databinding.DialogManageIconPacksBinding;
import com.cringe_studios.cringe_authenticator.databinding.FragmentSettingsBinding;
import com.cringe_studios.cringe_authenticator.icon.IconPack;
import com.cringe_studios.cringe_authenticator.icon.IconPackListAdapter;
import com.cringe_studios.cringe_authenticator.icon.IconUtil;
import com.cringe_studios.cringe_authenticator.util.Appearance;
import com.cringe_studios.cringe_authenticator.util.BackupException;
import com.cringe_studios.cringe_authenticator.util.BiometricUtil;
import com.cringe_studios.cringe_authenticator.util.DialogUtil;
import com.cringe_studios.cringe_authenticator.util.OTPDatabase;
import com.cringe_studios.cringe_authenticator.util.OTPDatabaseException;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;
import com.cringe_studios.cringe_authenticator.util.StyledDialogBuilder;
import com.cringe_studios.cringe_authenticator.util.Theme;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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

        Locale[] locales = new Locale[] {Locale.ENGLISH, Locale.GERMAN};

        String[] localeNames = new String[locales.length];
        for(int i = 0; i < locales.length; i++) {
            localeNames[i] = locales[i].getDisplayName(locales[i]);
        }

        binding.settingsLanguage.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, localeNames));
        binding.settingsLanguage.setSelection(Arrays.asList(locales).indexOf(SettingsUtil.getLocale(requireContext())));
        binding.settingsLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Locale locale = locales[position];
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
                        DialogUtil.showErrorDialog(requireContext(), "Failed to enable encryption", e);
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
                        DialogUtil.showErrorDialog(requireContext(), "Failed to disable encryption", e);
                    }
                }, () -> view.setChecked(true));
            }
        });

        boolean biometricSupported = BiometricUtil.isSupported(requireContext());
        if(SettingsUtil.isDatabaseEncrypted(requireContext()) && biometricSupported) {
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

        binding.settingsScreenSecurity.setChecked(SettingsUtil.isScreenSecurity(requireContext()));
        binding.settingsScreenSecurity.setOnCheckedChangeListener((view, checked) -> {
            SettingsUtil.setScreenSecurity(requireContext(), checked);
            requireActivity().recreate();
        });

        binding.settingsHideCodes.setChecked(SettingsUtil.isHideCodes(requireContext()));
        binding.settingsHideCodes.setOnCheckedChangeListener((view, checked) -> SettingsUtil.setHideCodes(requireContext(), checked));

        binding.settingsShowImages.setChecked(SettingsUtil.isShowImages(requireContext()));
        binding.settingsShowImages.setOnCheckedChangeListener((view, checked) -> SettingsUtil.setShowImages(requireContext(), checked));

        String[] themeNames = new String[Theme.values().length];
        for(int i = 0; i < Theme.values().length; i++) {
            themeNames[i] = getResources().getString(Theme.values()[i].getName());
        }

        binding.settingsEnableIntroVideo.setChecked(SettingsUtil.isIntroVideoEnabled(requireContext()));
        binding.settingsEnableIntroVideo.setOnCheckedChangeListener((view, checked) -> SettingsUtil.setEnableIntroVideo(requireContext(), checked));

        binding.settingsEnableThemedBackground.setChecked(SettingsUtil.isThemedBackgroundEnabled(requireContext()));
        binding.settingsEnableThemedBackground.setOnCheckedChangeListener((view, checked) -> {
            SettingsUtil.setEnableThemedBackground(requireContext(), checked);
            requireActivity().recreate();
        });

        binding.settingsTheme.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, themeNames));
        binding.settingsTheme.setSelection(SettingsUtil.getTheme(requireContext()).ordinal());
        binding.settingsTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Theme theme = Theme.values()[position];
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

        binding.settingsCreateBackup.setOnClickListener(view -> {
            new StyledDialogBuilder(requireContext())
                    .setTitle(R.string.create_backup)
                    .setItems(R.array.backup_create, (d, which) -> {
                        switch(which) {
                            case 0:
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

        binding.settingsLoadIconPack.setOnClickListener(v -> ((MainActivity) requireActivity()).promptPickIconPackLoad());

        binding.settingsManageIconPacks.setOnClickListener(v -> {
            List<IconPack> packs = IconUtil.loadAllIconPacks(requireContext());
            if(packs.isEmpty()) {
                Toast.makeText(requireContext(), R.string.no_icon_packs_installed, Toast.LENGTH_LONG).show();
                return;
            }

            DialogManageIconPacksBinding binding = DialogManageIconPacksBinding.inflate(getLayoutInflater());

            binding.manageIconPacksList.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.manageIconPacksList.setAdapter(new IconPackListAdapter(requireContext(), IconUtil.loadAllIconPacks(requireContext())));

            new StyledDialogBuilder(requireContext())
                    .setTitle("Manage icon packs")
                    .setView(binding.getRoot())
                    .setPositiveButton(R.string.ok, (d, which) -> {})
                    .show();
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
                        DialogUtil.showErrorDialog(requireContext(), "Failed to load backup", e);
                        return;
                    }
                }

                DialogUtil.showInputPasswordDialog(requireContext(), password -> {
                    try {
                        CryptoParameters parameters = BackupUtil.loadParametersFromBackup(requireContext(), uri);
                        SecretKey key = Crypto.generateKey(parameters, password);
                        loadBackup(uri, key, parameters);
                    } catch (CryptoException e) {
                        DialogUtil.showErrorDialog(requireContext(), "Failed to load backup. Make sure the password is valid", e);
                    } catch (BackupException | OTPDatabaseException e) {
                        DialogUtil.showErrorDialog(requireContext(), "Failed to load backup", e);
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
