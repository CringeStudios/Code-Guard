package com.cringe_studios.cringe_authenticator.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cringe_studios.cringe_authenticator.OTPData;
import com.cringe_studios.cringe_authenticator.databinding.FragmentDynamicBinding;
import com.cringe_studios.cringe_authenticator.otplist.OTPListAdapter;
import com.cringe_studios.cringe_authenticator.otplist.OTPListItem;
import com.cringe_studios.cringe_authenticator.util.FabUtil;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;

import java.util.List;

public class DynamicFragment extends NamedFragment {

    public static final String BUNDLE_GROUP = "group";

    private String groupName;

    private FragmentDynamicBinding binding;

    private Handler handler;

    private Runnable refreshCodes;

    private OTPListAdapter otpListAdapter;

    @Override
    public String getName() {
        return groupName;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDynamicBinding.inflate(inflater, container, false);

        groupName = requireArguments().getString(DynamicFragment.BUNDLE_GROUP);

        FabUtil.showFabs(requireActivity());

        otpListAdapter = new OTPListAdapter(getContext());
        binding.itemList.setAdapter(otpListAdapter);

        loadOTPs();

        handler = new Handler(Looper.getMainLooper());
        refreshCodes = () -> {
            for(int i = 0; i < binding.itemList.getChildCount(); i++) {
                OTPListItem vh = (OTPListItem) binding.itemList.findViewHolderForAdapterPosition(i);
                if(vh == null) continue;
                vh.getBinding().otpCode.setText(vh.getOTPData().getPin());
            }

            handler.postDelayed(refreshCodes, 1000L);
        };

        handler.post(refreshCodes);

        return binding.getRoot();
    }

    private void loadOTPs() {
        List<OTPData> data = SettingsUtil.getOTPs(requireContext(), groupName);

        for(OTPData otp : data) {
            otpListAdapter.add(otp);
        }
    }

    public void addOTP(OTPData data) {
        SettingsUtil.addOTP(requireContext(), groupName, data);
        otpListAdapter.add(data);
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

}
