package com.cringe_studios.code_guard;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;

import com.cringe_studios.code_guard.databinding.ActivityMainBinding;
import com.cringe_studios.code_guard.databinding.DialogIconPackExistsBinding;
import com.cringe_studios.code_guard.fragment.AboutFragment;
import com.cringe_studios.code_guard.fragment.EditOTPFragment;
import com.cringe_studios.code_guard.fragment.GroupFragment;
import com.cringe_studios.code_guard.fragment.NamedFragment;
import com.cringe_studios.code_guard.fragment.NoGroupsFragment;
import com.cringe_studios.code_guard.fragment.SettingsFragment;
import com.cringe_studios.code_guard.icon.IconPack;
import com.cringe_studios.code_guard.icon.IconPackException;
import com.cringe_studios.code_guard.icon.IconPackMetadata;
import com.cringe_studios.code_guard.icon.IconUtil;
import com.cringe_studios.code_guard.model.OTPData;
import com.cringe_studios.code_guard.scanner.QRScanner;
import com.cringe_studios.code_guard.scanner.QRScannerContract;
import com.cringe_studios.code_guard.util.DialogUtil;
import com.cringe_studios.code_guard.util.NavigationUtil;
import com.cringe_studios.code_guard.util.OTPDatabase;
import com.cringe_studios.code_guard.util.SettingsUtil;
import com.cringe_studios.code_guard.util.StyledDialogBuilder;
import com.cringe_studios.code_guard.util.ThemeUtil;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class MainActivity extends BaseActivity {

    private static final long BACK_BUTTON_DELAY = 500;

    private ActivityResultLauncher<Void> startQRCodeScan;

    private ActivityResultLauncher<PickVisualMediaRequest> pickQRCodeImage;

    private ActivityResultLauncher<String> pickBackupFileSave;

    private Consumer<Uri> pickBackupFileSaveCallback;

    private ActivityResultLauncher<String[]> pickBackupFileLoad;

    private Consumer<Uri> pickBackupFileLoadCallback;

    private ActivityResultLauncher<String[]> pickIconPackFile;

    private ActivityResultLauncher<PickVisualMediaRequest> pickIconImage;

    private Consumer<Uri> pickIconImageCallback;

    private QRScanner qrScanner;

    private boolean fullyLaunched;

    private boolean lockOnStop = true;

    private long backLastPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        qrScanner = new QRScanner();

        startQRCodeScan = registerForActivityResult(new QRScannerContract(), obj -> {
            lockOnStop = true;

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
                        DialogUtil.showErrorDialog(this, getString(R.string.error_qr_scan_not_detected));
                        return;
                    }

                    if(code.isMigrationPart()) {
                        DialogUtil.showYesNo(this, R.string.qr_scanner_migration_title, R.string.qr_scanner_migration_message, () -> {
                            Fragment fragment = NavigationUtil.getCurrentFragment(this);
                            if (fragment instanceof GroupFragment) {
                                GroupFragment frag = (GroupFragment) fragment;
                                for (OTPData dt : code.getOTPs()) frag.addOTP(dt);
                            }
                        }, null);
                    }else {
                        Fragment fragment = NavigationUtil.getCurrentFragment(this);
                        if (fragment instanceof GroupFragment) {
                            GroupFragment frag = (GroupFragment) fragment;
                            for (OTPData dt : code.getOTPs()) frag.addOTP(dt);
                        }
                    }
                }, error -> DialogUtil.showErrorDialog(this, getString(R.string.error_qr_scan_failed), error, null));
            } catch (IOException e) {
                DialogUtil.showErrorDialog(this, getString(R.string.error_qr_scan_image_failed), e);
            }
        });

        pickBackupFileSave = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), doc -> {
            if(pickBackupFileSaveCallback != null) {
                pickBackupFileSaveCallback.accept(doc);
                pickBackupFileSaveCallback = null;
            }
        });

        pickBackupFileLoad = registerForActivityResult(new ActivityResultContracts.OpenDocument(), doc -> {
            if(pickBackupFileLoadCallback != null) {
                pickBackupFileLoadCallback.accept(doc);
                pickBackupFileLoadCallback = null;
            }
        });

        pickIconPackFile = registerForActivityResult(new ActivityResultContracts.OpenDocument(), doc -> {
            lockOnStop = true;

            try {
                if(doc == null) return;
                IconPackMetadata meta = IconUtil.loadPackMetadata(this, doc);

                IconPack existingIconPack = IconUtil.loadIconPack(this, meta.getUuid());

                if(!meta.validate()) {
                    DialogUtil.showErrorDialog(this, getString(R.string.error_icon_pack_invalid));
                    return;
                }

                if(meta.getIcons().length == 0) {
                    DialogUtil.showErrorDialog(this, getString(R.string.error_icon_pack_empty));
                    return;
                }

                if(existingIconPack != null) {
                    DialogIconPackExistsBinding binding = DialogIconPackExistsBinding.inflate(getLayoutInflater());
                    binding.iconPackExistsText.setText(getString(R.string.error_icon_pack_exists, meta.getName(), meta.getVersion(), existingIconPack.getMetadata().getName(), existingIconPack.getMetadata().getVersion()));
                    binding.iconPackExistsChoices.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.error_icon_pack_exists_choices)));


                    AlertDialog dialog = new StyledDialogBuilder(this)
                            .setTitle(R.string.icon_pack_exists_title)
                            .setView(binding.getRoot())
                            .setNeutralButton(R.string.cancel, (d, which) -> {})
                            .create();

                    binding.iconPackExistsChoices.setOnItemClickListener((parent, view, position, id) -> {
                        switch(position) {
                            case 0: // Override
                                try {
                                    IconUtil.importIconPack(this, doc);
                                    Toast.makeText(this, getString(R.string.icon_pack_imported, meta.getIcons().length), Toast.LENGTH_LONG).show();
                                } catch (IconPackException e) {
                                    DialogUtil.showErrorDialog(this, getString(R.string.error_import_icon_pack), e);
                                }
                                break;
                            case 1: // Rename existing
                                try {
                                    IconUtil.renameIconPack(this, existingIconPack, existingIconPack.getMetadata().getName() + " (" + existingIconPack.getMetadata().getVersion() + ")", UUID.randomUUID().toString());
                                    IconUtil.importIconPack(this, doc);
                                    Toast.makeText(this, getString(R.string.icon_pack_imported, meta.getIcons().length), Toast.LENGTH_LONG).show();
                                } catch (IconPackException e) {
                                    DialogUtil.showErrorDialog(this, getString(R.string.error_import_icon_pack), e);
                                }
                                break;
                            case 2: // Rename imported
                                try {
                                    IconUtil.importIconPack(this, doc, meta.getName() + " (" + meta.getVersion() + ")", UUID.randomUUID().toString());
                                    Toast.makeText(this, getString(R.string.icon_pack_imported, meta.getIcons().length), Toast.LENGTH_LONG).show();
                                } catch (IconPackException e) {
                                    DialogUtil.showErrorDialog(this, getString(R.string.error_import_icon_pack), e);
                                }
                                break;
                        }

                        dialog.dismiss();
                    });

                    dialog.show();

                    return;
                }

                IconUtil.importIconPack(this, doc);
                Toast.makeText(this, getString(R.string.icon_pack_imported, meta.getIcons().length), Toast.LENGTH_LONG).show();
            } catch (IconPackException e) {
                DialogUtil.showErrorDialog(this, getString(R.string.error_import_icon_pack), e);
            }
        });

        pickIconImage = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), img -> {
            if(pickIconImageCallback != null) {
                pickIconImageCallback.accept(img);
                pickIconImageCallback = null;
            }
        });

        OTPDatabase.promptLoadDatabase(this, this::launchApp, this::finishAffinity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qrScanner.close();
    }

    private void launchApp() {
        fullyLaunched = true;
        lockOnStop = true;

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ThemeUtil.loadBackground(this);

        setSupportActionBar(binding.toolbar);

        if(SettingsUtil.isHamburgerModeEnabled(this)) {
            binding.fabMenu.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_hamburger));
        }

        binding.fabMenu.setOnClickListener(view -> NavigationUtil.openMenu(this, null));

        if(SettingsUtil.isFirstLaunch(this) && SettingsUtil.getGroups(this).isEmpty()) {
            SettingsUtil.addGroup(this, UUID.randomUUID().toString(), "My Codes");
            DialogUtil.showYesNo(this, R.string.enable_encryption_title, R.string.enable_encryption_message, () -> NavigationUtil.navigate(this, SettingsFragment.class, null), null);
            SettingsUtil.setEnableIntroVideo(this, false);
            SettingsUtil.setFirstLaunch(this, false);
        }

        Fragment fragment = NavigationUtil.getCurrentFragment(this);
        if(fragment instanceof NamedFragment) {
            ActionBar bar = getSupportActionBar();
            if(bar != null) bar.setTitle(((NamedFragment) fragment).getName());
        }else {
            navigateToMainGroup();
        }
    }

    public void updateIcon() {
        boolean cringeIcon = SettingsUtil.isCringeIconEnabled(this);

        getPackageManager().setComponentEnabledSetting(
                new ComponentName(this, "com.cringe_studios.code_guard.IntroActivity"),
                cringeIcon ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        getPackageManager().setComponentEnabledSetting(
                new ComponentName(this, "com.cringe_studios.code_guard.IntroActivityCringe"),
                !cringeIcon ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void navigateToMainGroup() {
        List<String> groups = SettingsUtil.getGroups(this);
        if(!groups.isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putString(GroupFragment.BUNDLE_GROUP, SettingsUtil.getGroups(this).get(0));
            NavigationUtil.navigate(this, GroupFragment.class, bundle);
        }else {
            ActionBar bar = getSupportActionBar();
            if(bar != null) bar.setTitle(R.string.app_name);
            NavigationUtil.navigate(this, NoGroupsFragment.class, null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Fragment fragment = NavigationUtil.getCurrentFragment(this);
        if(fragment instanceof GroupFragment) {
            GroupFragment frag = (GroupFragment) fragment;
            getMenuInflater().inflate(frag.isEditing() ? R.menu.menu_otps_edit : R.menu.menu_otps, menu);

            if(!SettingsUtil.isDatabaseEncrypted(this)) {
                menu.removeItem(R.id.action_lock);
            }

            if(frag.isEditing() && frag.hasSelectedMultipleItems()) {
                menu.removeItem(R.id.action_view_otp);
                menu.removeItem(R.id.action_edit_otp);
            }

            if(!frag.isEditing()) {
                MenuItem search = menu.findItem(R.id.action_search);
                SearchView v = (SearchView) search.getActionView();
                v.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        frag.filter(newText);
                        return true;
                    }
                });
            }
            return true;
        }

        if(fragment instanceof EditOTPFragment) {
            EditOTPFragment frag = (EditOTPFragment) fragment;
            getMenuInflater().inflate(frag.isView() ? R.menu.menu_view_otp : R.menu.menu_edit_otp, menu);
            return true;
        }

        getMenuInflater().inflate(R.menu.menu_main, menu);

        if(!SettingsUtil.isDatabaseEncrypted(this)) {
            menu.removeItem(R.id.action_lock);
        }

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
        Fragment fragment = NavigationUtil.getCurrentFragment(this);
        if(fragment instanceof GroupFragment) {
            GroupFragment groupFragment = (GroupFragment) fragment;
            if(groupFragment.isEditing()) {
                groupFragment.finishEditing();
                return;
            }
        }

        if(fragment instanceof EditOTPFragment) {
            ((EditOTPFragment) fragment).cancel();
            return;
        }

        if(!(fragment instanceof GroupFragment)) {
            navigateToMainGroup();
            return;
        }

        if(System.currentTimeMillis() - backLastPressed < BACK_BUTTON_DELAY) {
            finishAffinity();
        }else {
            backLastPressed = System.currentTimeMillis();
            Toast.makeText(this, R.string.back_pressed, Toast.LENGTH_LONG).show();
        }
    }

    public void openSettings(MenuItem item) {
        NavigationUtil.navigate(this, SettingsFragment.class, null);
    }

    public void openAbout(MenuItem item) {
        NavigationUtil.navigate(this, AboutFragment.class, null);
    }

    public void scanCode(MenuItem item) {
        lockOnStop = false;
        startQRCodeScan.launch(null);
    }

    public void scanCodeFromImage(MenuItem item) {
        pickQRCodeImage.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    public void inputCode(MenuItem item) {
        Fragment fragment = NavigationUtil.getCurrentFragment(this);
        if(!(fragment instanceof GroupFragment)) return;

        GroupFragment f = (GroupFragment) fragment;

        NavigationUtil.openOverlay(this, new EditOTPFragment(null, false, data -> {
            f.addOTP(data);
        }));
    }

    public void addOTP(MenuItem item) {
        Fragment frag = NavigationUtil.getCurrentFragment(this);
        if(frag instanceof GroupFragment) {
            ((GroupFragment) frag).addOTP();
        }
    }

    public void viewOTP(MenuItem item) {
        Fragment frag = NavigationUtil.getCurrentFragment(this);
        if(frag instanceof GroupFragment) {
            ((GroupFragment) frag).viewOTP();
        }
    }

    public void editOTP(MenuItem item) {
        Fragment frag = NavigationUtil.getCurrentFragment(this);
        if(frag instanceof GroupFragment) {
            ((GroupFragment) frag).editOTP();
        }
    }

    public void moveOTP(MenuItem item) {
        Fragment frag = NavigationUtil.getCurrentFragment(this);
        if(frag instanceof GroupFragment) {
            ((GroupFragment) frag).moveOTP();
        }
    }

    public void deleteOTP(MenuItem item) {
        Fragment frag = NavigationUtil.getCurrentFragment(this);
        if(frag instanceof GroupFragment) {
            ((GroupFragment) frag).deleteOTP();
        }
    }

    public void saveOTP(MenuItem item) {
        Fragment frag = NavigationUtil.getCurrentFragment(this);
        if(frag instanceof EditOTPFragment) {
            ((EditOTPFragment) frag).save();
        }
    }

    public void cancelEditingOTP(MenuItem item) {
        Fragment frag = NavigationUtil.getCurrentFragment(this);
        if(frag instanceof EditOTPFragment) {
            ((EditOTPFragment) frag).cancel();
        }
    }

    public void lockApp(MenuItem item) {
        OTPDatabase.unloadDatabase();
        OTPDatabase.promptLoadDatabase(this, () -> {}, () -> {});
    }

    public void promptPickBackupFileSave(String name, Consumer<Uri> callback) {
        this.lockOnStop = false;
        this.pickBackupFileSaveCallback = uri -> {
            lockOnStop = true;
            callback.accept(uri);
        };
        pickBackupFileSave.launch(name);
    }

    public void promptPickBackupFileLoad(Consumer<Uri> callback) {
        this.lockOnStop = false;
        this.pickBackupFileLoadCallback = uri -> {
            lockOnStop = true;
            callback.accept(uri);
        };
        pickBackupFileLoad.launch(new String[]{"application/json", "*/*"});
    }

    public void promptPickIconPackFile() {
        this.lockOnStop = false;
        pickIconPackFile.launch(new String[]{"application/zip", "*/*"});
    }

    public void promptPickIconImage(Consumer<Uri> callback) {
        this.lockOnStop = false;
        this.pickIconImageCallback = uri -> {
            lockOnStop = true;
            callback.accept(uri);
        };
        pickIconImage.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    @Override
    public void recreate() {
        lockOnStop = false;
        super.recreate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(lockOnStop) OTPDatabase.unloadDatabase();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(fullyLaunched) OTPDatabase.promptLoadDatabase(this, null, null);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}