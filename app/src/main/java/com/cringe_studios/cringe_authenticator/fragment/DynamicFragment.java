package com.cringe_studios.cringe_authenticator.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.databinding.AuthenticateTotpBinding;
import com.cringe_studios.cringe_authenticator.databinding.FragmentDynamicBinding;
import com.cringe_studios.cringe_authenticator.util.FabUtil;
import com.cringe_studios.cringe_authenticator.util.NavigationUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DynamicFragment extends Fragment {

    private FragmentDynamicBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDynamicBinding.inflate(inflater, container, false);

        String tab = requireArguments().getString("tab");

        String[] totps = new String[]{"Code 1", "Code 2", tab};
        for(String totp : totps) {
            AuthenticateTotpBinding itemBinding = AuthenticateTotpBinding.inflate(inflater);
            itemBinding.displayName.setText(totp);
            binding.itemList.addView(itemBinding.getRoot());
        }

        FabUtil.showFabs(getActivity());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }
}
