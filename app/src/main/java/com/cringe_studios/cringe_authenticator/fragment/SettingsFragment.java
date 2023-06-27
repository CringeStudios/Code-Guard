package com.cringe_studios.cringe_authenticator.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cringe_studios.cringe_authenticator.databinding.FragmentSettingsBinding;
import com.cringe_studios.cringe_authenticator.util.FabUtil;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;

public class SettingsFragment extends NamedFragment {

    private FragmentSettingsBinding binding;

    @Override
    public String getName() {
        return "Settings";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater);

        binding.settingsEnableIntroVideo.setChecked(SettingsUtil.isIntroVideoEnabled(requireContext()));
        binding.settingsEnableIntroVideo.setOnCheckedChangeListener((view, checked) -> SettingsUtil.setEnableIntroVideo(requireContext(), checked));

        binding.settingsBiometricLock.setChecked(SettingsUtil.isBiometricLock(requireContext()));
        binding.settingsBiometricLock.setOnCheckedChangeListener((view, checked) -> SettingsUtil.setBiometricLock(requireContext(), checked));

        binding.settingsTheme.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, SettingsUtil.THEME_NAMES.toArray(new String[0])));
        binding.settingsTheme.setSelection(SettingsUtil.THEME_NAMES.indexOf(SettingsUtil.getTheme(requireContext())));
        binding.settingsTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String theme = SettingsUtil.THEME_NAMES.get(position);
                if(theme.equals(SettingsUtil.getTheme(requireContext()))) return;

                SettingsUtil.setTheme(requireContext(), theme);

                Integer themeID = SettingsUtil.THEMES.get(theme);
                if(themeID == null) return;

                requireActivity().setTheme(themeID);
                requireActivity().recreate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        FabUtil.hideFabs(requireActivity());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

}
