package com.cringe_studios.code_guard.grouplist;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cringe_studios.code_guard.R;
import com.cringe_studios.code_guard.databinding.MenuItemBinding;

public class GroupListItem extends RecyclerView.ViewHolder {

    private final MenuItemBinding binding;

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
            TypedArray array = binding.getRoot().getContext().getTheme().obtainStyledAttributes(new int[] { R.attr.colorTheme1 });
            try {
                int color = array.getColor(0, 0xFFFF00FF);
                color = Color.argb(0x55, Color.red(color), Color.green(color), Color.blue(color));
                binding.menuItemBackground.setBackground(new ColorDrawable(color));
            } finally {
                array.close();
            }
        }else {
            binding.menuItemBackground.setBackground(null);
        }
    }

    public boolean isSelected() {
        return selected;
    }

}
