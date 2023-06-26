package com.cringe_studios.cringe_authenticator.grouplist;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cringe_studios.cringe_authenticator.databinding.MenuItemBinding;

public class GroupListItem extends RecyclerView.ViewHolder {

    private MenuItemBinding binding;

    public GroupListItem(@NonNull MenuItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public MenuItemBinding getBinding() {
        return binding;
    }
}
