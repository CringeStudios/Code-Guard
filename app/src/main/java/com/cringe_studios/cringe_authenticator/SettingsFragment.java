package com.cringe_studios.cringe_authenticator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.cringe_studios.cringe_authenticator.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater);

        binding.switch1.setOnCheckedChangeListener((view, checked) -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            controller.navigate(R.id.FirstFragment);
        });

        return binding.getRoot();
    }

}
