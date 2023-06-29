package com.cringe_studios.cringe_authenticator.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cringe_studios.cringe_authenticator.OTPData;
import com.cringe_studios.cringe_authenticator.databinding.FragmentGroupBinding;
import com.cringe_studios.cringe_authenticator.otplist.OTPListAdapter;
import com.cringe_studios.cringe_authenticator.otplist.OTPListItem;
import com.cringe_studios.cringe_authenticator.util.FabUtil;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;
import com.cringe_studios.cringe_authenticator_library.OTPType;

import java.util.List;

public class GroupFragment extends NamedFragment {

    public static final String BUNDLE_GROUP = "group";

    private String groupName;

    private FragmentGroupBinding binding;

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
        binding = FragmentGroupBinding.inflate(inflater, container, false);

        groupName = requireArguments().getString(GroupFragment.BUNDLE_GROUP);

        FabUtil.showFabs(requireActivity());

        otpListAdapter = new OTPListAdapter(getContext());
        binding.itemList.setAdapter(otpListAdapter);

        loadOTPs();

        handler = new Handler(Looper.getMainLooper());
        refreshCodes = () -> {
            refreshCodes();
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

    public void refreshCodes() {
        for(int i = 0; i < binding.itemList.getChildCount(); i++) {
            OTPListItem vh = (OTPListItem) binding.itemList.findViewHolderForAdapterPosition(i);
            if(vh == null) continue;
            vh.getBinding().otpCode.setText(vh.getOTPData().getPin());

            if(vh.getOTPData().getType() == OTPType.TOTP) {
                long timeDiff = vh.getOTPData().getNextDueTime() - System.currentTimeMillis() / 1000;
                vh.getBinding().progress.setProgress((int) ((1 - ((double) timeDiff / vh.getOTPData().getPeriod())) * 100));
            }
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

}
