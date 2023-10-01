package com.cringe_studios.cringe_authenticator.icon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.databinding.DialogManageIconPacksItemBinding;
import com.cringe_studios.cringe_authenticator.util.DialogUtil;

import java.util.List;

public class IconPackListAdapter extends RecyclerView.Adapter<IconPackItem> {

    private Context context;

    private LayoutInflater inflater;

    private List<IconPack> packs;

    public IconPackListAdapter(Context context, List<IconPack> packs) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.packs = packs;
    }

    @NonNull
    @Override
    public IconPackItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new IconPackItem(DialogManageIconPacksItemBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull IconPackItem holder, int position) {
        IconPack pack = packs.get(position);
        holder.setPack(pack);

        holder.getBinding().iconPackName.setText(pack.getMetadata().getName());

        holder.getBinding().iconPackDelete.setOnClickListener(view -> {
            DialogUtil.showYesNo(context, R.string.delete_pack_title, R.string.delete_pack_message, () -> IconUtil.removeIconPack(context, pack.getMetadata().getUuid()), null);
        });
    }

    @Override
    public int getItemCount() {
        return packs.size();
    }
}
