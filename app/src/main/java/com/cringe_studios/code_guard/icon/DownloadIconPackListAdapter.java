package com.cringe_studios.code_guard.icon;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.cringe_studios.code_guard.R;
import com.cringe_studios.code_guard.databinding.DialogDownloadIconPacksItemBinding;
import com.cringe_studios.code_guard.util.DialogUtil;
import com.cringe_studios.code_guard.util.StyledDialogBuilder;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadIconPackListAdapter extends RecyclerView.Adapter<DownloadIconPackItem> {

    private final Context context;

    private final LayoutInflater inflater;

    private final List<DownloadableIconPack> packs;

    private final Handler handler;

    public DownloadIconPackListAdapter(Context context, List<DownloadableIconPack> packs) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.packs = packs;
        this.handler = new Handler(Looper.getMainLooper());
    }

    @NonNull
    @Override
    public DownloadIconPackItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DownloadIconPackItem(DialogDownloadIconPacksItemBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadIconPackItem holder, int position) {
        DownloadableIconPack pack = packs.get(position);
        holder.setPack(pack);

        holder.getBinding().iconPackName.setText(pack.getName());
        holder.getBinding().iconPackCredit.setText(pack.getCredit());

        IconPack installedPack = null;
        try {
            installedPack = IconUtil.loadIconPack(context, pack.getId());
        } catch (IconPackException ignored) { /* ignored, the user can just download the icon pack again */ }

        if(installedPack != null) {
            holder.getBinding().iconPackDownload.setImageResource(R.drawable.baseline_refresh_24);
            holder.getBinding().iconPackInstalled.setText(context.getString(R.string.icon_pack_version, installedPack.getMetadata().getVersion()));
        }else {
            holder.getBinding().iconPackInstalled.setText(R.string.icon_pack_not_installed);
        }

        holder.getBinding().iconPackDownload.setOnClickListener(view -> {
            AlertDialog dialog = new StyledDialogBuilder(context)
                    .setTitle(R.string.icon_pack_downloading_title)
                    .setMessage(R.string.icon_pack_downloading_message)
                    .setCancelable(false)
                    .show();

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                executor.shutdown();

                File file = null;
                try {
                    file = File.createTempFile("iconpack", ".zip", context.getCacheDir());
                    pack.download(file);

                    IconPackMetadata meta = IconUtil.importIconPack(context, Uri.fromFile(file));

                    handler.post(() -> {
                        dialog.dismiss();
                        notifyItemChanged(position);
                        Toast.makeText(context, context.getString(R.string.icon_pack_imported, meta.getIcons().length), Toast.LENGTH_LONG).show();
                    });
                }catch(Exception e) {
                    handler.post(() -> {
                        dialog.dismiss();
                        DialogUtil.showErrorDialog(context, context.getString(R.string.error_import_icon_pack), e);
                    });
                } finally {
                    if(file != null) file.delete();
                }

                return null;
            });
        });
    }

    @Override
    public int getItemCount() {
        return packs.size();
    }
}
