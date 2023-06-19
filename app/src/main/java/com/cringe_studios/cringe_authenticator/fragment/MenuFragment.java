package com.cringe_studios.cringe_authenticator.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.databinding.FragmentMenuBinding;
import com.cringe_studios.cringe_authenticator.databinding.MenuItemBinding;
import com.cringe_studios.cringe_authenticator.util.NavigationUtil;

public class MenuFragment extends Fragment {

    private FragmentMenuBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMenuBinding.inflate(inflater);

        String[] items = {"a", "b"};

        SharedPreferences pr = getContext().getSharedPreferences("menu", Context.MODE_PRIVATE);

        for(String item : items) {
            MenuItemBinding itemBinding = MenuItemBinding.inflate(inflater);
            itemBinding.button.setText(item);
            itemBinding.button.setOnClickListener(view -> {
                Bundle bundle = new Bundle();
                bundle.putString("tab", item);
                NavigationUtil.navigate(this, DynamicFragment.class, bundle);
            });
            binding.menuItems.addView(itemBinding.getRoot(), 0);
        }

        binding.editSwitch.setOnCheckedChangeListener((view, checked) -> {
            // TODO: edit mode
        });

        return binding.getRoot();
    }

}
