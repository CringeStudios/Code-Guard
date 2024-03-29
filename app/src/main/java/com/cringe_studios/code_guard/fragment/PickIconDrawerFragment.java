package com.cringe_studios.code_guard.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import com.cringe_studios.code_guard.databinding.FragmentPickIconBinding;
import com.cringe_studios.code_guard.icon.Icon;
import com.cringe_studios.code_guard.icon.IconListAdapter;
import com.cringe_studios.code_guard.icon.IconUtil;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PickIconDrawerFragment extends BottomSheetDialogFragment {

    private final Consumer<Icon> selected;

    public PickIconDrawerFragment(Consumer<Icon> selected) {
        this.selected = selected;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentPickIconBinding binding = FragmentPickIconBinding.inflate(inflater);

        IconListAdapter adapter = new IconListAdapter(requireContext(), IconUtil.loadAllIcons(requireContext()), icon -> {
            selected.accept(icon);
            getParentFragmentManager().beginTransaction().remove(this).commit();
        });

        binding.pickIconSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.pickIconList.setAdapter(adapter);
        binding.pickIconList.setOnChildClickListener(adapter);

        return binding.getRoot();
    }

}
