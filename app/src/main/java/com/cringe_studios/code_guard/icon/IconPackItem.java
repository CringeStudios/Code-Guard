package com.cringe_studios.code_guard.icon;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cringe_studios.code_guard.databinding.DialogManageIconPacksItemBinding;

public class IconPackItem extends RecyclerView.ViewHolder {

    private final DialogManageIconPacksItemBinding binding;

    private IconPack pack;

    public IconPackItem(@NonNull DialogManageIconPacksItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public DialogManageIconPacksItemBinding getBinding() {
        return binding;
    }

    public void setPack(IconPack pack) {
        this.pack = pack;
    }

    public IconPack getPack() {
        return pack;
    }

}
