package com.cringe_studios.cringe_authenticator;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cringe_studios.cringe_authenticator.databinding.FragmentDynamicBinding;
import com.cringe_studios.cringe_authenticator.databinding.FragmentFirstBinding;

public class DynamicFragment extends Fragment {

    private FragmentDynamicBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("AMOGUS", requireArguments().getString("sus"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDynamicBinding.inflate(inflater, container, false);
        binding.buttonSecond.setText(requireArguments().getString("sus"));
        return binding.getRoot();
    }
}
