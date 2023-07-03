package com.cringe_studios.cringe_authenticator.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;

import com.cringe_studios.cringe_authenticator.OTPData;
import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.databinding.DialogInputCodeHotpBinding;
import com.cringe_studios.cringe_authenticator.databinding.DialogInputCodeTotpBinding;
import com.cringe_studios.cringe_authenticator_library.OTPAlgorithm;
import com.cringe_studios.cringe_authenticator_library.OTPType;

import java.util.Arrays;

public class DialogUtil {

    private static final Integer[] DIGITS = new Integer[]{6, 7, 8, 9, 10, 11, 12};

    private static void showCodeDialog(Context context, View view, DialogCallback ok, Runnable back) {
        AlertDialog dialog = new StyledDialogBuilder(context)
                .setTitle(R.string.code_input_title)
                .setView(view)
                .setPositiveButton(R.string.ok, (btnView, which) -> {})
                .setNeutralButton(R.string.back, (btnView, which) -> back.run())
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

    public static void showErrorDialog(Context context, String errorMessage) {
        new StyledDialogBuilder(context)
                .setTitle(R.string.failed_title)
                .setMessage(errorMessage)
                .setPositiveButton(R.string.ok, (d, which) -> {})
                .show();
    }

    public static void showTOTPDialog(LayoutInflater inflater, OTPData initialData, Consumer<OTPData> callback, Runnable back, boolean view) {
        Context context = inflater.getContext();
        DialogInputCodeTotpBinding binding = DialogInputCodeTotpBinding.inflate(inflater);

        binding.inputAlgorithm.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, OTPAlgorithm.values()));
        binding.inputAlgorithm.setEnabled(!view);

        binding.inputDigits.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, DIGITS));
        binding.inputDigits.setEnabled(!view);

        binding.inputName.setEnabled(!view);
        binding.inputSecret.setEnabled(!view);
        binding.inputPeriod.setEnabled(!view);
        binding.inputChecksum.setEnabled(!view);

        if(initialData != null) {
            binding.inputName.setText(initialData.getName());
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
                String secret = binding.inputSecret.getText().toString();
                OTPAlgorithm algorithm = (OTPAlgorithm) binding.inputAlgorithm.getSelectedItem();
                int digits = (int) binding.inputDigits.getSelectedItem();
                int period = Integer.parseInt(binding.inputPeriod.getText().toString());
                boolean checksum = binding.inputChecksum.isChecked();

                OTPData data = new OTPData(name, OTPType.TOTP, secret, algorithm, digits, period, 0, checksum);

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
        }, back);
    }

    public static void showHOTPDialog(LayoutInflater inflater, OTPData initialData, Consumer<OTPData> callback, Runnable back, boolean view) {
        Context context = inflater.getContext();
        DialogInputCodeHotpBinding binding = DialogInputCodeHotpBinding.inflate(inflater);

        binding.inputAlgorithm.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, OTPAlgorithm.values()));
        binding.inputAlgorithm.setEnabled(!view);

        binding.inputDigits.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, DIGITS));
        binding.inputDigits.setEnabled(!view);

        binding.inputName.setEnabled(!view);
        binding.inputSecret.setEnabled(!view);
        binding.inputCounter.setEnabled(!view);
        binding.inputChecksum.setEnabled(!view);

        if(initialData != null) {
            binding.inputName.setText(initialData.getName());
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
                String secret = binding.inputSecret.getText().toString();
                OTPAlgorithm algorithm = (OTPAlgorithm) binding.inputAlgorithm.getSelectedItem();
                int digits = (int) binding.inputDigits.getSelectedItem();
                int counter = Integer.parseInt(binding.inputCounter.getText().toString());
                boolean checksum = binding.inputChecksum.isChecked();

                OTPData data = new OTPData(name, OTPType.TOTP, secret, algorithm, digits, 0, counter, checksum);

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
        }, back);
    }

    public static void showViewCodeDialog(LayoutInflater inflater, @NonNull OTPData initialData, Runnable back) {
        switch(initialData.getType()) {
            case HOTP: showHOTPDialog(inflater, initialData, d -> {}, back, true); break;
            case TOTP: showTOTPDialog(inflater, initialData, d -> {}, back, true); break;
        }
    }

    public static void showEditCodeDialog(LayoutInflater inflater, @NonNull OTPData initialData, Consumer<OTPData> callback, Runnable back) {
        switch(initialData.getType()) {
            case HOTP: showHOTPDialog(inflater, initialData, callback, back, false); break;
            case TOTP: showTOTPDialog(inflater, initialData, callback, back, false); break;
        }
    }
}
