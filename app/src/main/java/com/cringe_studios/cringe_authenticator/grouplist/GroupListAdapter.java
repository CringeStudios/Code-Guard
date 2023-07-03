package com.cringe_studios.cringe_authenticator.grouplist;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.cringe_studios.cringe_authenticator.databinding.MenuItemBinding;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;

import java.util.ArrayList;
import java.util.List;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListItem> {

    private Context context;

    private LayoutInflater inflater;

    private List<String> items;

    private Handler handler;

    private Consumer<String> navigateToGroup;

    private Consumer<String> showMenuCallback;

    public GroupListAdapter(Context context, Consumer<String> navigateToGroup, Consumer<String> showMenuCallback) {
        this.context = context;
        this.navigateToGroup = navigateToGroup;
        this.showMenuCallback = showMenuCallback;
        this.inflater = LayoutInflater.from(context);
        this.items = new ArrayList<>();
        this.handler = new Handler(Looper.getMainLooper());
    }

    @NonNull
    @Override
    public GroupListItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MenuItemBinding binding = MenuItemBinding.inflate(inflater, parent, false);
        return new GroupListItem(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupListItem holder, int position) {
        String group = items.get(position);

        holder.getBinding().button.setText(SettingsUtil.getGroupName(context, group));

        holder.getBinding().button.setOnClickListener(view -> navigateToGroup.accept(group));
        holder.getBinding().button.setOnLongClickListener(view -> {
            showMenuCallback.accept(group);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void add(String group) {
        items.add(group);
        notifyItemInserted(items.size() - 1);
    }

    public void remove(String group) {
        int index = items.indexOf(group);
        if(index == -1) return;
        items.remove(group);
        notifyItemRemoved(index);
    }

    public void update(String group) {
        int index = items.indexOf(group);
        if(index == -1) return;
        notifyItemChanged(index);
    }

}
