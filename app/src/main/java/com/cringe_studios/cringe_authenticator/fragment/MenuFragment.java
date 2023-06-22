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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.databinding.FragmentMenuBinding;
import com.cringe_studios.cringe_authenticator.databinding.MenuItemBinding;
import com.cringe_studios.cringe_authenticator.util.FabUtil;
import com.cringe_studios.cringe_authenticator.util.NavigationUtil;

public class MenuFragment extends Fragment {

    private FragmentMenuBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMenuBinding.inflate(inflater);

        SharedPreferences pr = getContext().getSharedPreferences("menu", Context.MODE_PRIVATE);

        String[] items = {"a", "b"};

        for(String item : items) {
            MenuItemBinding itemBinding = MenuItemBinding.inflate(inflater);
            itemBinding.button.setText(item);
            itemBinding.button.setOnClickListener(view -> {
                Bundle bundle = new Bundle();
                bundle.putString(DynamicFragment.BUNDLE_GROUP, item);
                NavigationUtil.navigate(this, DynamicFragment.class, bundle);
            });
            itemBinding.button.setOnLongClickListener(view -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete?")
                        .setMessage("Delete this?")
                        .setPositiveButton("Yes", (dialog, which) -> itemBinding.button.setVisibility(View.GONE))
                        .setNegativeButton("No", (dialog, which) -> {})
                        .show();
                // TODO: better method?
                // TODO: actually delete
                return true;
            });
            binding.menuItems.addView(itemBinding.getRoot());
        }

        binding.editSwitch.setOnCheckedChangeListener((view, checked) -> {
            // TODO: edit mode
        });

        FabUtil.hideFabs(getActivity());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

}
