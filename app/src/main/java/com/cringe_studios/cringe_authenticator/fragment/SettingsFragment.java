package com.cringe_studios.cringe_authenticator.fragment;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.databinding.FragmentSettingsBinding;
import com.cringe_studios.cringe_authenticator.util.FabUtil;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater);

        binding.settingsEnableIntroVideo.setChecked(SettingsUtil.isIntroVideoEnabled(requireContext()));
        binding.settingsEnableIntroVideo.setOnCheckedChangeListener((view, checked) -> SettingsUtil.setEnableIntroVideo(requireContext(), checked));

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

                Log.e("AMOGUS", "REstarting activity");
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
