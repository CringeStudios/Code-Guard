package com.cringe_studios.cringe_authenticator.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cringe_studios.cringe_authenticator.OTPData;
import com.cringe_studios.cringe_authenticator.databinding.FragmentDynamicBinding;
import com.cringe_studios.cringe_authenticator.databinding.OtpCodeBinding;
import com.cringe_studios.cringe_authenticator.util.FabUtil;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;
import com.cringe_studios.cringe_authenticator_library.OTP;

import java.util.List;

public class DynamicFragment extends Fragment {

    public static final String BUNDLE_GROUP = "group";

    private String groupName;

    private FragmentDynamicBinding binding;

    private Handler handler;

    private Runnable refreshCodes;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDynamicBinding.inflate(inflater, container, false);

        groupName = requireArguments().getString(DynamicFragment.BUNDLE_GROUP);

        /*String[] totps = new String[]{"Code 1", "Code 2", groupName};
        for(String totp : totps) {
            AuthenticateTotpBinding itemBinding = AuthenticateTotpBinding.inflate(inflater);
            itemBinding.displayName.setText(totp);
            binding.itemList.addView(itemBinding.getRoot());
        }*/

        loadOTPs();

        FabUtil.showFabs(getActivity());

        handler = new Handler(Looper.getMainLooper());
        refreshCodes = () -> {
            for(int i = 0; i < binding.itemList.getChildCount(); i++) {
                View v = binding.itemList.getChildAt(i);
                OTP otp = (OTP) v.getTag();
                otp.getPin();
            }
            handler.postDelayed(refreshCodes, 1000L);
        };

        handler.post(refreshCodes);

        return binding.getRoot();
    }

    public void loadOTPs() {
        SharedPreferences prefs = getActivity().getSharedPreferences(SettingsUtil.GROUPS_PREFS_NAME, Context.MODE_PRIVATE);
        List<OTPData> data = SettingsUtil.getOTPs(prefs, groupName);
        Log.i("AMOGUS", "OTPS: " + data);

        binding.itemList.removeAllViews();
        for(OTPData otp : data) {
            OtpCodeBinding itemBinding = OtpCodeBinding.inflate(getLayoutInflater());
            itemBinding.label.setText(otp.getName());
            itemBinding.otpCode.setText(otp.toOTP().getPin());
            itemBinding.getRoot().setTag(otp.toOTP());
            binding.itemList.addView(itemBinding.getRoot());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(refreshCodes);
        super.onDestroy();
    }

    public String getGroupName() {
        return groupName;
    }

}
