package com.cringe_studios.code_guard.fragment;

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
        return binding.getRoot();
    }
}
