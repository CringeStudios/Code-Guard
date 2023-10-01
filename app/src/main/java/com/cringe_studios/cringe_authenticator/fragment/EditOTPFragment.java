package com.cringe_studios.cringe_authenticator.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.exifinterface.media.ExifInterface;

import com.cringe_studios.cringe_authenticator.MainActivity;
import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.databinding.FragmentEditOtpBinding;
import com.cringe_studios.cringe_authenticator.icon.IconUtil;
import com.cringe_studios.cringe_authenticator.model.OTPData;
import com.cringe_studios.cringe_authenticator.util.DialogUtil;
import com.cringe_studios.cringe_authenticator.util.IOUtil;
import com.cringe_studios.cringe_authenticator.util.NavigationUtil;
import com.cringe_studios.cringe_authenticator.util.StyledDialogBuilder;
import com.cringe_studios.cringe_authenticator.util.ThemeUtil;
import com.cringe_studios.cringe_authenticator_library.OTPAlgorithm;
import com.cringe_studios.cringe_authenticator_library.OTPType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class EditOTPFragment extends NamedFragment {

    private static final Integer[] DIGITS = new Integer[]{6, 7, 8, 9, 10, 11, 12};

    private static final String[] TYPES = new String[]{
        OTPType.HOTP.getFriendlyName() + " (HOTP)",
        OTPType.TOTP.getFriendlyName() + " (TOTP)"
    };

    private FragmentEditOtpBinding binding;

    private OTPData data;

    private String imageData;

    private boolean view;

    private Consumer<OTPData> callback;

    public EditOTPFragment(OTPData data, boolean view, Consumer<OTPData> callback) {
        this.data = data;
        this.view = view;
        this.callback = callback;
    }

    @Override
    public String getName() {
        return requireActivity().getString(R.string.fragment_edit_otp);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditOtpBinding.inflate(inflater);

        @DrawableRes int bg = ThemeUtil.getBackground(requireContext());
        if(bg != 0) {
            binding.getRoot().setBackgroundResource(bg);
        }

        binding.inputImage.setOnClickListener(v -> {
            new StyledDialogBuilder(requireContext())
                    .setTitle("Choose Image")
                    .setItems(new String[]{"Image from icon pack", "Image from gallery", "No image", "Reset to default image"}, (d, which) -> {
                        switch(which) {
                            case 0:
                                pickImageFromIconPack();
                                break;
                            case 1:
                                pickGalleryImage();
                                break;
                            case 2:
                                setNoImage();
                                break;
                            case 3:
                                setDefaultImage();
                                break;
                        }
                    })
                    .show();
        });

        binding.inputType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, TYPES));
        binding.inputType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                OTPType newType = OTPType.values()[position];
                switch(newType) {
                    case HOTP:
                        binding.textPeriod.setVisibility(View.GONE);
                        binding.inputPeriod.setVisibility(View.GONE);
                        binding.textCounter.setVisibility(View.VISIBLE);
                        binding.inputCounter.setVisibility(View.VISIBLE);
                        break;
                    case TOTP:
                        binding.textCounter.setVisibility(View.GONE);
                        binding.inputCounter.setVisibility(View.GONE);
                        binding.textPeriod.setVisibility(View.VISIBLE);
                        binding.inputPeriod.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        binding.inputType.setEnabled(!view);
        binding.inputType.setSelection(data != null ? data.getType().ordinal() : 0);

        binding.inputAlgorithm.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, OTPAlgorithm.values()));
        binding.inputAlgorithm.setEnabled(!view);

        binding.inputDigits.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, DIGITS));
        binding.inputDigits.setEnabled(!view);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(imageData != null && !imageData.equals(OTPData.IMAGE_DATA_NONE)) return;
                updateImage();
            }
        };

        binding.inputName.setEnabled(!view);
        binding.inputName.addTextChangedListener(watcher);

        binding.inputIssuer.setEnabled(!view);
        binding.inputIssuer.addTextChangedListener(watcher);

        binding.inputSecret.setEnabled(!view);
        binding.inputPeriod.setEnabled(!view);
        binding.inputChecksum.setEnabled(!view);

        if(data != null) {
            imageData = data.getImageData();

            binding.inputName.setText(data.getName());
            binding.inputIssuer.setText(data.getIssuer());
            binding.inputSecret.setText(data.getSecret());
            binding.inputAlgorithm.setSelection(data.getAlgorithm().ordinal());

            int index = Arrays.asList(DIGITS).indexOf(data.getDigits());
            if(index != -1) binding.inputDigits.setSelection(index);
            binding.inputChecksum.setChecked(data.hasChecksum());

            switch(data.getType()) {
                case HOTP:
                    binding.inputCounter.setText(String.valueOf(data.getCounter()));
                    break;
                case TOTP:
                    binding.inputPeriod.setText(String.valueOf(data.getPeriod()));
                    break;
            }
        }

        updateImage();

        return binding.getRoot();
    }

    private void updateImage() {
        IconUtil.loadEffectiveImage(requireContext(), imageData, binding.inputIssuer.getText().toString(), binding.inputName.getText().toString(), binding.inputImage, null);
    }

    private void pickImageFromIconPack() {
        // TODO: check if icon packs installed
        new PickIconDrawerFragment(icon -> {
            imageData = Base64.encodeToString(icon.getBytes(), Base64.DEFAULT);
            updateImage();
        }).show(requireActivity().getSupportFragmentManager(), null);
    }

    private void pickGalleryImage() {
        ((MainActivity) requireActivity()).promptPickIconImage(uri -> {
            if(uri == null) return;

            try(InputStream in = requireActivity().getContentResolver().openInputStream(uri)) {
                if(in == null) return;

                byte[] bytes = IOUtil.readBytes(in);
                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                ExifInterface i = new ExifInterface(new ByteArrayInputStream(bytes));
                int orientation = i.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                Matrix matrix = new Matrix();
                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.postRotate(90);
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.postRotate(90);
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.postRotate(90);
                        bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                }

                bm = IconUtil.cutToIcon(bm);

                ByteArrayOutputStream bOut = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 100, bOut);
                imageData = Base64.encodeToString(bOut.toByteArray(), Base64.DEFAULT);

                binding.inputImage.setImageBitmap(bm);
            }catch(IOException e) {
                DialogUtil.showErrorDialog(requireContext(), "Failed to open image", e);
            }
        });
    }

    private void setNoImage() {
        imageData = OTPData.IMAGE_DATA_NONE;
        updateImage();
    }

    private void setDefaultImage() {
        imageData = null;
        updateImage();
    }

    public boolean isView() {
        return view;
    }

    public void cancel() {
        NavigationUtil.closeOverlay(this);
    }

    public void save() {
        try {
            String name = binding.inputName.getText().toString();
            if(name.trim().isEmpty()) {
                DialogUtil.showErrorDialog(requireContext(), requireContext().getString(R.string.otp_add_missing_name));
                return;
            }

            String issuer = binding.inputIssuer.getText().toString();
            if(issuer.trim().isEmpty()) {
                issuer = null;
            }

            String secret = binding.inputSecret.getText().toString();
            OTPAlgorithm algorithm = (OTPAlgorithm) binding.inputAlgorithm.getSelectedItem();
            int digits = (int) binding.inputDigits.getSelectedItem();
            boolean checksum = binding.inputChecksum.isChecked();

            int period = 0;
            int counter = 0;
            OTPType type = OTPType.values()[binding.inputType.getSelectedItemPosition()];
            switch(type) {
                case HOTP:
                    counter = Integer.parseInt(binding.inputCounter.getText().toString());
                    if(counter < 0) throw new NumberFormatException();
                    break;
                case TOTP:
                    period = Integer.parseInt(binding.inputPeriod.getText().toString());
                    if(period <= 0) throw new NumberFormatException();
                    break;
            }

            OTPData data = new OTPData(name, issuer, type, secret, algorithm, digits, period, counter, checksum);
            data.setImageData(imageData);

            String errorMessage = data.validate();
            if(errorMessage != null) {
                DialogUtil.showErrorDialog(requireContext(), errorMessage);
                return;
            }

            callback.accept(data);
            NavigationUtil.closeOverlay(this);
        }catch(NumberFormatException e) {
            DialogUtil.showErrorDialog(requireContext(), requireContext().getString(R.string.input_code_invalid_number));
        }
    }

}
