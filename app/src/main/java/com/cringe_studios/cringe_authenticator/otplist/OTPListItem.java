package com.cringe_studios.cringe_authenticator.otplist;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.databinding.OtpCodeBinding;
import com.cringe_studios.cringe_authenticator.model.OTPData;

public class OTPListItem extends RecyclerView.ViewHolder {

    private OtpCodeBinding binding;

    private OTPData otpData;

    private boolean selected;

    public OTPListItem(OtpCodeBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
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

    public static String formatCode(String code) {
        // TODO: add setting for group size (and enable/disable grouping)
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < code.length(); i++) {
            if(i != 0 && i % 3 == 0) {
                b.append(' ');
            }

            char c = code.charAt(i);
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
