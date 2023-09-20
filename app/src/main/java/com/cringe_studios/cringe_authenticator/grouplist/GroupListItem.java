package com.cringe_studios.cringe_authenticator.grouplist;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.databinding.MenuItemBinding;

public class GroupListItem extends RecyclerView.ViewHolder {

    private MenuItemBinding binding;

    private String groupId;

    private boolean selected;

    public GroupListItem(@NonNull MenuItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public MenuItemBinding getBinding() {
        return binding;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;

        if(selected) {
            binding.menuItemBackground.setBackground(new ColorDrawable(binding.getRoot().getContext().getResources().getColor(R.color.selected_highlight)));
        }else {
            binding.menuItemBackground.setBackground(null);
        }
    }

    public boolean isSelected() {
        return selected;
    }

}
