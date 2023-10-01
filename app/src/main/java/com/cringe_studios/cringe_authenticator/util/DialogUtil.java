package com.cringe_studios.cringe_authenticator.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;

import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.backup.BackupData;
import com.cringe_studios.cringe_authenticator.databinding.DialogCreateGroupBinding;
import com.cringe_studios.cringe_authenticator.databinding.DialogErrorBinding;
import com.cringe_studios.cringe_authenticator.databinding.DialogInputCodeHotpBinding;
import com.cringe_studios.cringe_authenticator.databinding.DialogInputCodeTotpBinding;
import com.cringe_studios.cringe_authenticator.databinding.DialogInputPasswordBinding;
import com.cringe_studios.cringe_authenticator.databinding.DialogSetPasswordBinding;
import com.cringe_studios.cringe_authenticator.model.OTPData;
import com.cringe_studios.cringe_authenticator_library.OTPAlgorithm;
import com.cringe_studios.cringe_authenticator_library.OTPType;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DialogUtil {

    private static final Integer[] DIGITS = new Integer[]{6, 7, 8, 9, 10, 11, 12};

    private static void showCodeDialog(Context context, View view, DialogCallback ok) {
        AlertDialog dialog = new StyledDialogBuilder(context)
                .setTitle(R.string.code_input_title)
                .setView(view)
                .setPositiveButton(R.string.ok, (btnView, which) -> {})
                .setNegativeButton(R.string.cancel, (btnView, which) -> {})
                .create();

        dialog.setOnShowListener(d -> {
            Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            okButton.setOnClickListener(v -> {
                if(ok.callback()) dialog.dismiss();
            });
        });

        dialog.show();
    }

    public static void showErrorDialog(Context context, String errorMessage, String details, Runnable closed) {
        DialogErrorBinding binding = DialogErrorBinding.inflate(LayoutInflater.from(context));

        binding.errorMessage.setText(errorMessage);

        AlertDialog.Builder b = new StyledDialogBuilder(context)
                .setTitle(R.string.failed_title)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.ok, (d, which) -> {})
                .setOnDismissListener(d -> { if(closed != null) closed.run(); });

        if(details != null) {
            binding.errorDetailsText.setText(details);
            b.setNeutralButton("Details", (d, which) -> {});
        }

        AlertDialog dialog = b.create();

        if(details != null) {
            dialog.setOnShowListener(d -> {
                Button detailsButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                detailsButton.setOnClickListener(v -> binding.errorDetails.setVisibility(binding.errorDetails.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
            });
        }

        dialog.show();
    }

    public static void showErrorDialog(Context context, String errorMessage, Runnable closed) {
        showErrorDialog(context, errorMessage, null, closed);
    }

    public static void showErrorDialog(Context context, String errorMessage) {
        showErrorDialog(context, errorMessage, (Runnable) null);
    }

    public static void showErrorDialog(Context context, String errorMessage, Exception exception) {
        showErrorDialog(context, errorMessage, stackTraceToString(exception), null);
    }

    private static String stackTraceToString(Throwable t) {
        StringBuilder b = new StringBuilder();

        b.append(t.toString()).append('\n');
        for(StackTraceElement e : t.getStackTrace()) {
            b.append("    ").append(e.toString()).append('\n');
        }

        if(t.getCause() != null) {
            b.append("Caused by: ").append(stackTraceToString(t.getCause()));
        }

        return b.toString().trim();
    }

    public static void showTOTPDialog(LayoutInflater inflater, OTPData initialData, Consumer<OTPData> callback, boolean view) {
        Context context = inflater.getContext();
        DialogInputCodeTotpBinding binding = DialogInputCodeTotpBinding.inflate(inflater);

        binding.inputAlgorithm.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, OTPAlgorithm.values()));
        binding.inputAlgorithm.setEnabled(!view);

        binding.inputDigits.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, DIGITS));
        binding.inputDigits.setEnabled(!view);

        binding.inputName.setEnabled(!view);
        binding.inputIssuer.setEnabled(!view);
        binding.inputSecret.setEnabled(!view);
        binding.inputPeriod.setEnabled(!view);
        binding.inputChecksum.setEnabled(!view);

        if(initialData != null) {
            binding.inputName.setText(initialData.getName());
            binding.inputIssuer.setText(initialData.getIssuer());
            binding.inputSecret.setText(initialData.getSecret());
            binding.inputAlgorithm.setSelection(initialData.getAlgorithm().ordinal());

            int index = Arrays.asList(DIGITS).indexOf(initialData.getDigits());
            if(index != -1) binding.inputDigits.setSelection(index);

            binding.inputPeriod.setText(String.valueOf(initialData.getPeriod()));
            binding.inputChecksum.setChecked(initialData.hasChecksum());
        }

        showCodeDialog(context, binding.getRoot(), () -> {
            try {
                String name = binding.inputName.getText().toString();
                if(name.trim().isEmpty()) {
                    showErrorDialog(context, context.getString(R.string.otp_add_missing_name));
                    return false;
                }

                String issuer = binding.inputIssuer.getText().toString();
                if(issuer.trim().isEmpty()) {
                    issuer = null;
                }

                String secret = binding.inputSecret.getText().toString();
                OTPAlgorithm algorithm = (OTPAlgorithm) binding.inputAlgorithm.getSelectedItem();
                int digits = (int) binding.inputDigits.getSelectedItem();
                int period = Integer.parseInt(binding.inputPeriod.getText().toString());
                boolean checksum = binding.inputChecksum.isChecked();

                OTPData data = new OTPData(name, issuer, OTPType.TOTP, secret, algorithm, digits, period, 0, checksum);

                String errorMessage = data.validate();
                if(errorMessage != null) {
                    showErrorDialog(context, errorMessage);
                    return false;
                }

                callback.accept(data);
                return true;
            }catch(NumberFormatException e) {
                showErrorDialog(context, context.getString(R.string.input_code_invalid_number));
                return false;
            }
        });
    }

    public static void showHOTPDialog(LayoutInflater inflater, OTPData initialData, Consumer<OTPData> callback, boolean view) {
        Context context = inflater.getContext();
        DialogInputCodeHotpBinding binding = DialogInputCodeHotpBinding.inflate(inflater);

        binding.inputAlgorithm.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, OTPAlgorithm.values()));
        binding.inputAlgorithm.setEnabled(!view);

        binding.inputDigits.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, DIGITS));
        binding.inputDigits.setEnabled(!view);

        binding.inputName.setEnabled(!view);
        binding.inputIssuer.setEnabled(!view);
        binding.inputSecret.setEnabled(!view);
        binding.inputCounter.setEnabled(!view);
        binding.inputChecksum.setEnabled(!view);

        if(initialData != null) {
            binding.inputName.setText(initialData.getName());
            binding.inputIssuer.setText(initialData.getIssuer());
            binding.inputSecret.setText(initialData.getSecret());
            binding.inputAlgorithm.setSelection(initialData.getAlgorithm().ordinal());

            int index = Arrays.asList(DIGITS).indexOf(initialData.getDigits());
            if(index != -1) binding.inputDigits.setSelection(index);

            binding.inputCounter.setText(String.valueOf(initialData.getCounter()));
            binding.inputChecksum.setChecked(initialData.hasChecksum());
        }

        showCodeDialog(context, binding.getRoot(), () -> {
            try {
                String name = binding.inputName.getText().toString();
                if(name.trim().isEmpty()) {
                    showErrorDialog(context, context.getString(R.string.otp_add_missing_name));
                    return false;
                }

                String issuer = binding.inputIssuer.getText().toString();
                if(issuer.trim().isEmpty()) {
                    issuer = null;
                }

                String secret = binding.inputSecret.getText().toString();
                OTPAlgorithm algorithm = (OTPAlgorithm) binding.inputAlgorithm.getSelectedItem();
                int digits = (int) binding.inputDigits.getSelectedItem();
                int counter = Integer.parseInt(binding.inputCounter.getText().toString());
                boolean checksum = binding.inputChecksum.isChecked();

                OTPData data = new OTPData(name, issuer, OTPType.HOTP, secret, algorithm, digits, 0, counter, checksum);

                String errorMessage = data.validate();
                if(errorMessage != null) {
                    showErrorDialog(context, errorMessage);
                    return false;
                }

                callback.accept(data);
                return true;
            }catch(NumberFormatException e) {
                showErrorDialog(context, context.getString(R.string.input_code_invalid_number));
                return false;
            }
        });
    }

    public static void showViewCodeDialog(LayoutInflater inflater, @NonNull OTPData initialData) {
        // TODO: use better dialogs
        switch(initialData.getType()) {
            case HOTP: showHOTPDialog(inflater, initialData, d -> {}, true); break;
            case TOTP: showTOTPDialog(inflater, initialData, d -> {}, true); break;
        }
    }

    public static void showEditCodeDialog(LayoutInflater inflater, @NonNull OTPData initialData, Consumer<OTPData> callback) {
        switch(initialData.getType()) {
            case HOTP: showHOTPDialog(inflater, initialData, callback, false); break;
            case TOTP: showTOTPDialog(inflater, initialData, callback, false); break;
        }
    }

    public static void showCreateGroupDialog(LayoutInflater inflater, String initialName, Consumer<String> callback, Runnable onDismiss) {
        Context context = inflater.getContext();

        DialogCreateGroupBinding binding = DialogCreateGroupBinding.inflate(inflater);
        binding.createGroupName.setText(initialName);

        AlertDialog dialog = new StyledDialogBuilder(context)
                .setTitle(R.string.action_new_group)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.add, (view, which) -> {})
                .setNegativeButton(R.string.cancel, (view, which) -> { if(onDismiss != null) onDismiss.run(); })
                .create();

        dialog.setOnShowListener(d -> {
            Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            okButton.setOnClickListener(v -> {
                if(binding.createGroupName.getText().length() == 0) {
                    DialogUtil.showErrorDialog(context, context.getString(R.string.new_group_missing_title));
                    return;
                }

                dialog.dismiss();
                callback.accept(binding.createGroupName.getText().toString());
                if(onDismiss != null) onDismiss.run();
            });
        });

        dialog.setOnCancelListener(d -> { if(onDismiss != null) onDismiss.run(); });
        dialog.show();
    }

    public static void showChooseGroupDialog(Context context, Consumer<String> callback, Runnable onDismiss) {
        List<String> groups = SettingsUtil.getGroups(context);
        String[] groupNames = new String[groups.size() + 1];

        groupNames[0] = context.getString(R.string.uri_handler_create_group);
        for(int i = 0; i < groups.size(); i++) {
            groupNames[i + 1] = SettingsUtil.getGroupName(context, groups.get(i));
        }

        AlertDialog dialog = new StyledDialogBuilder(context)
                .setTitle(R.string.uri_handler_add_code_title)
                .setItems(groupNames, (d, which) -> {
                    if(which == 0) { // Create New Group
                        DialogUtil.showCreateGroupDialog(LayoutInflater.from(context), null, group -> {
                            String id = UUID.randomUUID().toString();
                            SettingsUtil.addGroup(context, id, group);
                            callback.accept(id);
                        }, onDismiss);
                        return;
                    }

                    callback.accept(groups.get(which - 1));
                    if(onDismiss != null) onDismiss.run();
                })
                .setNegativeButton(R.string.cancel, (d, which) -> { if(onDismiss != null) onDismiss.run(); })
                .setOnCancelListener(d -> { if(onDismiss != null) onDismiss.run(); })
                .create();

        dialog.show();
    }

    public static void showSetPasswordDialog(Context context, Consumer<String> callback, Runnable onCancel) {
        DialogSetPasswordBinding binding = DialogSetPasswordBinding.inflate(LayoutInflater.from(context));

        AlertDialog dialog = new StyledDialogBuilder(context)
                .setTitle(R.string.set_password)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.ok, (d, which) -> {})
                .setNegativeButton(R.string.cancel, (d, which) -> { if(onCancel != null) onCancel.run(); })
                .setOnCancelListener(d -> { if(onCancel != null) onCancel.run(); })
                .create();

        dialog.setOnShowListener(d -> {
            Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            okButton.setOnClickListener(v -> {
                if(binding.setPassword.getText().length() == 0) {
                    DialogUtil.showErrorDialog(context, "You need to enter a password");
                    return;
                }

                String pass = binding.setPassword.getText().toString();
                String confirm = binding.confirmPassword.getText().toString();
                if(!pass.equals(confirm)) {
                    DialogUtil.showErrorDialog(context, "The passwords do not match");
                    return;
                }

                dialog.dismiss();
                callback.accept(pass);
            });
        });

        dialog.show();
    }

    public static void showInputPasswordDialog(Context context, Consumer<String> callback, Runnable onCancel) {
        DialogInputPasswordBinding binding = DialogInputPasswordBinding.inflate(LayoutInflater.from(context));

        AlertDialog dialog = new StyledDialogBuilder(context)
                .setTitle("Input Password")
                .setView(binding.getRoot())
                .setPositiveButton(R.string.ok, (d, which) -> {})
                .setNegativeButton(R.string.cancel, (d, which) -> { if(onCancel != null) onCancel.run(); })
                .setOnCancelListener(d -> { if(onCancel != null) onCancel.run(); })
                .create();

        dialog.setOnShowListener(d -> {
            Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            okButton.setOnClickListener(v -> {
                if(binding.inputPassword.getText().length() == 0) {
                    DialogUtil.showErrorDialog(context, "You need to enter a password");
                    return;
                }

                dialog.dismiss();
                callback.accept(binding.inputPassword.getText().toString());
            });
        });

        dialog.show();
    }

    public static void showYesNo(Context context, @StringRes int title, @StringRes int message, @StringRes int yesText, @StringRes int noText, Runnable yes, Runnable no) {
        new StyledDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(yesText, (d, w) -> {
                    if(yes != null) yes.run();
                })
                .setNegativeButton(noText, (d, w) -> {
                    if(no != null) no.run();
                })
                .show()
                .setCanceledOnTouchOutside(false);
    }

    public static void showYesNo(Context context, @StringRes int title, @StringRes int message, Runnable yes, Runnable no) {
        showYesNo(context, title, message, R.string.yes, R.string.no, yes, no);
    }

    public static void showYesNoCancel(Context context, @StringRes int title, @StringRes int message, @StringRes int yesText, @StringRes int noText, @StringRes int cancelText, Runnable yes, Runnable no, Runnable cancel) {
            new StyledDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(yesText, (d, w) -> {
                    if(yes != null) yes.run();
                })
                .setNegativeButton(noText, (d, w) -> {
                    if(no != null) no.run();
                })
                .setNeutralButton(cancelText, (d, w) -> d.cancel())
                .setOnCancelListener(d -> {
                    if(cancel != null) cancel.run();
                })
                .show();
    }

    public static void showYesNoCancel(Context context, @StringRes int title, @StringRes int message, Runnable yes, Runnable no, Runnable cancel) {
        showYesNoCancel(context, title, message, R.string.yes, R.string.no, R.string.cancel, yes, no, cancel);
    }

    public static void showBackupLoadedDialog(Context context, BackupData data) {
        AlertDialog dialog = new StyledDialogBuilder(context)
                .setTitle("Backup loaded")
                .setMessage(String.format("Successfully loaded %s group(s) from the backup", data.getGroups().length))
                .setPositiveButton(R.string.ok, (d, which) -> {})
                .create();

        dialog.show();
    }

}
