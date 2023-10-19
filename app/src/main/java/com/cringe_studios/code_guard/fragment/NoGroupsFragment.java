package com.cringe_studios.code_guard.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cringe_studios.code_guard.R;
import com.cringe_studios.code_guard.databinding.FragmentNoGroupsBinding;

public class NoGroupsFragment extends NamedFragment {

    @Override
    public String getName() {
        return requireActivity().getString(R.string.fragment_no_groups);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentNoGroupsBinding binding = FragmentNoGroupsBinding.inflate(inflater);
        return binding.getRoot();
    }
}
