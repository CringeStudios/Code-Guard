package com.cringe_studios.cringe_authenticator.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cringe_studios.cringe_authenticator.OTPData;
import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.databinding.AuthenticateTotpBinding;
import com.cringe_studios.cringe_authenticator.databinding.FragmentDynamicBinding;
import com.cringe_studios.cringe_authenticator.util.FabUtil;
import com.cringe_studios.cringe_authenticator.util.NavigationUtil;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class DynamicFragment extends Fragment {

    private String groupName;

    private FragmentDynamicBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDynamicBinding.inflate(inflater, container, false);

        groupName = requireArguments().getString("group");

        /*String[] totps = new String[]{"Code 1", "Code 2", groupName};
        for(String totp : totps) {
            AuthenticateTotpBinding itemBinding = AuthenticateTotpBinding.inflate(inflater);
            itemBinding.displayName.setText(totp);
            binding.itemList.addView(itemBinding.getRoot());
        }*/

        loadOTPs();

        FabUtil.showFabs(getActivity());

        return binding.getRoot();
    }

    public void loadOTPs() {
        SharedPreferences prefs = getActivity().getSharedPreferences("groups", Context.MODE_PRIVATE);
        List<OTPData> data = SettingsUtil.getOTPs(prefs, groupName);
        Log.i("AMOGUS", "OTPS: " + data);

        for(OTPData otp : data) {
            AuthenticateTotpBinding itemBinding = AuthenticateTotpBinding.inflate(getLayoutInflater());
            itemBinding.displayName.setText(otp.getName());
            itemBinding.totpCode.setText(otp.toOTP().getPin());
            binding.itemList.addView(itemBinding.getRoot());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

    public String getGroupName() {
        return groupName;
    }

}
