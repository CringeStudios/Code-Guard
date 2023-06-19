package com.cringe_studios.cringe_authenticator.util;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.cringe_studios.cringe_authenticator.R;

public class NavigationUtil {

    public static void navigate(AppCompatActivity activity, Class<? extends Fragment> fragmentClass, Bundle args) {
        activity.getSupportActionBar().setTitle(fragmentClass.getSimpleName());
        navigate(activity.getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager(), fragmentClass, args);
    }

    public static void navigate(Fragment currentFragment, Class<? extends Fragment> fragmentClass, Bundle args) {
        ((AppCompatActivity) currentFragment.getActivity()).getSupportActionBar().setTitle(fragmentClass.getSimpleName());
        navigate(currentFragment.getParentFragment().getChildFragmentManager(), fragmentClass, args);
    }

    private static void navigate(FragmentManager manager, Class<? extends Fragment> fragmentClass, Bundle args) {
        manager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.nav_host_fragment_content_main, fragmentClass, args)
                .commit();
    }

}
