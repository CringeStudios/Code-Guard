package com.cringe_studios.cringe_authenticator.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cringe_studios.cringe_authenticator.databinding.FragmentMenuBinding;
import com.cringe_studios.cringe_authenticator.grouplist.GroupListAdapter;
import com.cringe_studios.cringe_authenticator.util.FabUtil;
import com.cringe_studios.cringe_authenticator.util.NavigationUtil;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;

import java.util.List;

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

        groupListAdapter = new GroupListAdapter(requireContext(), group -> {
            Bundle bundle = new Bundle();
            bundle.putString(GroupFragment.BUNDLE_GROUP, group);
            NavigationUtil.navigate(this, GroupFragment.class, bundle);
        }, this::removeGroup);

        binding.menuItems.setAdapter(groupListAdapter);

        loadGroups();

        /*binding.editSwitch.setOnCheckedChangeListener((view, checked) -> {
            // TODO: edit mode
        });*/

        FabUtil.hideFabs(requireActivity());

        return binding.getRoot();
    }

    private void loadGroups() {
        List<String> items = SettingsUtil.getGroups(requireContext());
        Log.i("AMOGUS", "items: " + items);

        for(String item : items) {
            groupListAdapter.add(item);
        }
    }

    public void addGroup(String group) {
        SettingsUtil.addGroup(requireContext(), group);
        groupListAdapter.add(group);
    }

    public void removeGroup(String group) {
        SettingsUtil.removeGroup(requireContext(), group);
        groupListAdapter.remove(group);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

}
