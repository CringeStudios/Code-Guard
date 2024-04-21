package com.cringe_studios.code_guard.icon;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cringe_studios.code_guard.databinding.DialogDownloadIconPacksItemBinding;

public class DownloadIconPackItem extends RecyclerView.ViewHolder {

    private final DialogDownloadIconPacksItemBinding binding;

    private DownloadableIconPack pack;

    public DownloadIconPackItem(@NonNull DialogDownloadIconPacksItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public DialogDownloadIconPacksItemBinding getBinding() {
        return binding;
    }

    public void setPack(DownloadableIconPack pack) {
        this.pack = pack;
    }

    public DownloadableIconPack getPack() {
        return pack;
    }

}
