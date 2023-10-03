package com.cringe_studios.code_guard.fragment;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cringe_studios.code_guard.R;
import com.cringe_studios.code_guard.databinding.FragmentAboutBinding;

public class AboutFragment extends NamedFragment {

    private FragmentAboutBinding binding;

    @Override
    public String getName() {
        return requireActivity().getString(R.string.fragment_about);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater);
        try {
            PackageManager manager = requireContext().getPackageManager();
            PackageInfo info = null;
            info = manager.getPackageInfo(requireContext().getPackageName(), 0);
            String version = info.versionName;
            binding.appVersion.setText(version);
        } catch (PackageManager.NameNotFoundException ignored) {}
        return binding.getRoot();
    }
}
