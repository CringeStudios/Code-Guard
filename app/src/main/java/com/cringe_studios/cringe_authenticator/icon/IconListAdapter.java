package com.cringe_studios.cringe_authenticator.icon;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import androidx.core.util.Consumer;

import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.databinding.IconListCategoryBinding;
import com.cringe_studios.cringe_authenticator.databinding.IconListIconBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class IconListAdapter extends BaseExpandableListAdapter implements ExpandableListView.OnChildClickListener {

    private Context context;

    private Map<String, List<Icon>> icons;
    private List<String> categories;

    private Map<String, List<Icon>> filteredIcons;

    private Consumer<Icon> selected;

    public IconListAdapter(Context context, Map<String, List<Icon>> icons, Consumer<Icon> selected) {
        this.context = context;
        this.icons = icons;
        this.categories = new ArrayList<>(icons.keySet());
        this.filteredIcons = new TreeMap<>(icons);
        this.selected = selected;
    }

    public void filter(String query) {
        Map<String, List<Icon>> filtered = new TreeMap<>();
        for(String cat : categories) {
            List<Icon> f = new ArrayList<>();
            for(Icon i : icons.get(cat)) {
                if(i.getMetadata().getName().toLowerCase().contains(query.toLowerCase())) {
                    f.add(i);
                }
            }
            filtered.put(cat, f);
        }

        filteredIcons = filtered;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return categories.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return filteredIcons.get(categories.get(groupPosition)).size();
    }

    @Override
    public String getGroup(int groupPosition) {
        return categories.get(groupPosition);
    }

    @Override
    public Icon getChild(int groupPosition, int childPosition) {
        return filteredIcons.get(categories.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        IconListCategoryBinding binding = IconListCategoryBinding.inflate(LayoutInflater.from(context));
        binding.getRoot().setText((String) getGroup(groupPosition));
        return binding.getRoot();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        IconListIconBinding binding = IconListIconBinding.inflate(LayoutInflater.from(context));

        Icon icon = getChild(groupPosition, childPosition);
        binding.iconListIconImage.setImageResource(R.drawable.cringeauth_white);
        IconUtil.loadImage(binding.iconListIconImage, icon.getBytes(), v -> v.setImageDrawable(new ColorDrawable(Color.TRANSPARENT)));
        binding.iconListIconText.setText(icon.getMetadata().getName());
        return binding.getRoot();
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        selected.accept(getChild(groupPosition, childPosition));
        return true;
    }
}
