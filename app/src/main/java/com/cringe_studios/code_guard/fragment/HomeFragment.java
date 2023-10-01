package com.cringe_studios.code_guard.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.cringe_studios.code_guard.R;
import com.cringe_studios.code_guard.databinding.FragmentHomeBinding;

public class HomeFragment extends NamedFragment {

    private FragmentHomeBinding binding;

    @Override
    public String getName() {
        return requireActivity().getString(R.string.fragment_home);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}