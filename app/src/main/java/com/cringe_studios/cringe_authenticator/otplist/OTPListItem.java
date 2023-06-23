package com.cringe_studios.cringe_authenticator.otplist;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cringe_studios.cringe_authenticator.OTPData;
import com.cringe_studios.cringe_authenticator.databinding.OtpCodeBinding;

public class OTPListItem extends RecyclerView.ViewHolder {

    private OtpCodeBinding binding;

    private OTPData otpData;

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

}
