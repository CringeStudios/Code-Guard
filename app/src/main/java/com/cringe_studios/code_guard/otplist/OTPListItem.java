package com.cringe_studios.code_guard.otplist;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cringe_studios.code_guard.R;
import com.cringe_studios.code_guard.databinding.OtpCodeBinding;
import com.cringe_studios.code_guard.model.OTPData;
import com.cringe_studios.code_guard.util.SettingsUtil;
import com.cringe_studios.cringe_authenticator_library.OTPException;
import com.cringe_studios.cringe_authenticator_library.OTPType;

public class OTPListItem extends RecyclerView.ViewHolder {

    private final OtpCodeBinding binding;

    private OTPData otpData;

    private boolean selected;

    private boolean codeShown;

    public OTPListItem(OtpCodeBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        this.codeShown = !SettingsUtil.isHideCodes(binding.getRoot().getContext());
    }

    public @NonNull OtpCodeBinding getBinding() {
        return binding;
    }

    public void setOTPData(OTPData otpData) {
        this.otpData = otpData;
    }

    public OTPData getOTPData() {
        return otpData;
    }

    public void setCodeShown(boolean codeShown) {
        this.codeShown = codeShown;
    }

    public boolean isCodeShown() {
        return codeShown;
    }

    public void refresh() throws OTPException {
        binding.otpCode.setText(formatCode(otpData.getPin()));
        binding.nextOtpCode.setText(formatCode(otpData.getNextPin()));

        if(otpData.getType() == OTPType.TOTP) {
            long timeDiff = otpData.getNextDueTime() - System.currentTimeMillis() / 1000;
            double progress = ((double) timeDiff / otpData.getPeriod());
            binding.progress.setImageLevel((int) (-progress * 10_000));
        }
    }

    private String formatCode(String code) {
        Context ctx = itemView.getContext();

        int groupSize = SettingsUtil.getDigitGroupSize(ctx);
        HiddenStyle hiddenStyle = SettingsUtil.getHiddenStyle(ctx);

        StringBuilder b = new StringBuilder();
        for(int i = 0; i < code.length(); i++) {
            if(groupSize > 1 && i != 0 && i % groupSize == 0) {
                b.append(' ');
            }

            char c;
            if(codeShown) {
                c = code.charAt(i);
            }else {
                c = hiddenStyle.getHiddenChar();
            }
            b.append(c);
        }
        return b.toString();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;

        if(selected) {
            TypedArray array = binding.getRoot().getContext().getTheme().obtainStyledAttributes(new int[] { R.attr.colorTheme1 });
            try {
                int color = array.getColor(0, 0xFFFF00FF);
                color = Color.argb(0x55, Color.red(color), Color.green(color), Color.blue(color));
                binding.otpCodeBackground.setBackground(new ColorDrawable(color));
            } finally {
                array.close();
            }
        }else {
            binding.otpCodeBackground.setBackground(null);
        }
    }

    public boolean isSelected() {
        return selected;
    }

}
