package com.cringe_studios.code_guard.grouplist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.cringe_studios.code_guard.databinding.MenuItemBinding;
import com.cringe_studios.code_guard.util.SettingsUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListItem> {

    private final Context context;

    private final RecyclerView recyclerView;

    private final LayoutInflater inflater;

    private final List<String> items;

    private final Consumer<String> navigateToGroup;

    private final Runnable saveGroups;

    private final Runnable updateToolbarOptions;

    private boolean editing;

    public GroupListAdapter(Context context, RecyclerView recyclerView, Consumer<String> navigateToGroup, Runnable saveGroups, Runnable updateToolbarOptions) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.navigateToGroup = navigateToGroup;
        this.saveGroups = saveGroups;
        this.updateToolbarOptions = updateToolbarOptions;
        this.inflater = LayoutInflater.from(context);
        this.items = new ArrayList<>();

        attachTouchHelper(recyclerView);
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

        holder.setGroupId(group);
        holder.setSelected(false);

        holder.getBinding().button.setText(SettingsUtil.getGroupName(context, group));

        holder.getBinding().button.setOnClickListener(view -> {
            if(!editing) {
                navigateToGroup.accept(group);
            }else {
                holder.setSelected(!holder.isSelected());
                if(getSelectedGroups().isEmpty()) editing = false;
                updateToolbarOptions.run();
            }
        });

        holder.getBinding().button.setOnLongClickListener(view -> {
            if(editing) return true;

            holder.setSelected(true);
            editing = true;
            updateToolbarOptions.run();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<String> getItems() {
        return items;
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

    public boolean isEditing() {
        return editing;
    }

    public void finishEditing() {
        if(!editing) return;

        editing = false;
        for(GroupListItem item : getSelectedGroups()) {
            item.setSelected(false);
        }

        updateToolbarOptions.run();
    }

    public List<GroupListItem> getSelectedGroups() {
        List<GroupListItem> selected = new ArrayList<>();
        for(int i = 0; i < items.size(); i++) {
            GroupListItem vh = (GroupListItem) recyclerView.findViewHolderForAdapterPosition(i);
            if(vh == null) continue;
            if(vh.isSelected()) selected.add(vh);
        }
        return selected;
    }

    private void attachTouchHelper(RecyclerView view) {
        new ItemTouchHelper(new TouchHelperCallback()).attachToRecyclerView(view);
    }

    private class TouchHelperCallback extends ItemTouchHelper.Callback {

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            Collections.swap(items, viewHolder.getAdapterPosition(), target.getAdapterPosition());
            notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            saveGroups.run();
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

        @Override
        public boolean isLongPressDragEnabled() {
            return editing;
        }
    }

}
