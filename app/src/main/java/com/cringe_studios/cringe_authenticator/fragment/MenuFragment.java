package com.cringe_studios.cringe_authenticator.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.databinding.FragmentMenuBinding;
import com.cringe_studios.cringe_authenticator.grouplist.GroupListAdapter;
import com.cringe_studios.cringe_authenticator.grouplist.GroupListItem;
import com.cringe_studios.cringe_authenticator.util.DialogUtil;
import com.cringe_studios.cringe_authenticator.util.FabUtil;
import com.cringe_studios.cringe_authenticator.util.NavigationUtil;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;
import com.cringe_studios.cringe_authenticator.util.StyledDialogBuilder;

import java.util.List;
import java.util.UUID;

public class MenuFragment extends NamedFragment {

    private FragmentMenuBinding binding;

    private GroupListAdapter groupListAdapter;

    @Override
    public String getName() {
        return "Menu";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMenuBinding.inflate(inflater);

        groupListAdapter = new GroupListAdapter(requireContext(), binding.menuItems, group -> {
            Bundle bundle = new Bundle();
            bundle.putString(GroupFragment.BUNDLE_GROUP, group);
            NavigationUtil.navigate(this, GroupFragment.class, bundle);
        }, () -> SettingsUtil.setGroups(requireContext(), groupListAdapter.getItems()));
        binding.menuItems.setAdapter(groupListAdapter);

        loadGroups();

        FabUtil.hideFabs(requireActivity());

        return binding.getRoot();
    }

    private void showGroupDialog(String group) {
        new StyledDialogBuilder(requireContext())
                .setTitle(R.string.edit_group_title)
                .setItems(R.array.rename_delete, (dialog, which) -> {
                    switch(which) {
                        case 0:
                            DialogUtil.showCreateGroupDialog(getLayoutInflater(), SettingsUtil.getGroupName(requireContext(), group), newName -> {
                                renameGroup(group, newName);
                            }, null);

                            break;
                        case 1:
                            new StyledDialogBuilder(requireContext())
                                    .setTitle(R.string.group_delete_title)
                                    .setMessage(R.string.group_delete_message)
                                    .setPositiveButton(R.string.yes, (d, w) -> removeGroup(group))
                                    .setNegativeButton(R.string.no, (d, w) -> {})
                                    .show();
                            break;
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {})
                .show();
    }

    private void loadGroups() {
        List<String> items = SettingsUtil.getGroups(requireContext());

        for(String item : items) {
            groupListAdapter.add(item);
        }
    }

    public void addGroup() {
        DialogUtil.showCreateGroupDialog(getLayoutInflater(), null, groupName -> {
            String id = UUID.randomUUID().toString();
            SettingsUtil.addGroup(requireContext(), id, groupName);
            groupListAdapter.add(id);
        }, null);
    }

    public void editGroup() {
        if(!groupListAdapter.isEditing()) return;

        List<GroupListItem> items = groupListAdapter.getSelectedGroups();
        if(items.size() != 1) return;

        String group = items.get(0).getGroupId();

        DialogUtil.showCreateGroupDialog(getLayoutInflater(), SettingsUtil.getGroupName(requireContext(), group), newName -> { // TODO: edit group dialog (with "Edit Group" title)
            renameGroup(group, newName);
            groupListAdapter.finishEditing();
        }, null);
    }

    public void removeSelectedGroups() {
        if(!groupListAdapter.isEditing()) return;

        new StyledDialogBuilder(requireContext())
                .setTitle(R.string.group_delete_title)
                .setMessage("Delete selected groups?")
                .setPositiveButton(R.string.yes, (d, w) -> {
                    for(GroupListItem item : groupListAdapter.getSelectedGroups()) {
                        removeGroup(item.getGroupId());
                    }

                    groupListAdapter.finishEditing();
                })
                .setNegativeButton(R.string.no, (d, w) -> {})
                .show();
    }

    public void removeGroup(String group) {
        SettingsUtil.removeGroup(requireContext(), group);
        groupListAdapter.remove(group);
    }

    public void renameGroup(String group, String newName) {
        SettingsUtil.setGroupName(requireContext(), group, newName);
        groupListAdapter.update(group);
    }

    public boolean isEditing() {
        return groupListAdapter.isEditing();
    }

    public void finishEditing() {
        groupListAdapter.finishEditing();
    }

    public boolean hasSelectedMultipleItems() {
        return groupListAdapter.getSelectedGroups().size() > 1;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

}
