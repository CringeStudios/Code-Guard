package com.cringe_studios.cringe_authenticator.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.databinding.FragmentMenuDrawerBinding;
import com.cringe_studios.cringe_authenticator.grouplist.GroupListAdapter;
import com.cringe_studios.cringe_authenticator.grouplist.GroupListItem;
import com.cringe_studios.cringe_authenticator.util.DialogUtil;
import com.cringe_studios.cringe_authenticator.util.FabUtil;
import com.cringe_studios.cringe_authenticator.util.NavigationUtil;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;
import com.cringe_studios.cringe_authenticator.util.StyledDialogBuilder;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;
import java.util.UUID;

public class MenuDrawerFragment extends BottomSheetDialogFragment {

    private FragmentMenuDrawerBinding binding;

    private GroupListAdapter groupListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMenuDrawerBinding.inflate(inflater);

        groupListAdapter = new GroupListAdapter(requireContext(), binding.menuItems, group -> {
            Bundle bundle = new Bundle();
            bundle.putString(GroupFragment.BUNDLE_GROUP, group);
            NavigationUtil.navigate(this, GroupFragment.class, bundle);
            getParentFragmentManager().beginTransaction().remove(this).commit();
        }, () -> SettingsUtil.setGroups(requireContext(), groupListAdapter.getItems()), this::updateToolbarOptions);
        binding.menuItems.setAdapter(groupListAdapter);

        binding.menuAdd.setOnClickListener(view -> this.addGroup());
        binding.menuEdit.setOnClickListener(view -> this.editGroup());
        binding.menuDelete.setOnClickListener(view -> this.removeSelectedGroups());

        loadGroups();
        updateToolbarOptions();

        return binding.getRoot();
    }

    private void updateToolbarOptions() {
        binding.menuEdit.setVisibility(isEditing() && !hasSelectedMultipleItems() ? View.VISIBLE : View.GONE);
        binding.menuDelete.setVisibility(isEditing() ? View.VISIBLE : View.GONE);
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
                .setMessage(R.string.group_delete_message)
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
