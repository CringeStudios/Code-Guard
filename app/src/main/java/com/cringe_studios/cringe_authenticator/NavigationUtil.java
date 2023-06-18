package com.cringe_studios.cringe_authenticator;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class NavigationUtil {

    public static void navigate(AppCompatActivity activity, Class<? extends Fragment> fragmentClass, Bundle args) {
        navigate(activity.getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager(), fragmentClass, args);
    }

    public static void navigate(Fragment currentFragment, Class<? extends Fragment> fragmentClass, Bundle args) {
        navigate(currentFragment.getParentFragment().getChildFragmentManager(), fragmentClass, args);
    }

    private static void navigate(FragmentManager manager, Class<? extends Fragment> fragmentClass, Bundle args) {
        manager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.nav_host_fragment_content_main, fragmentClass, args)
                .commit();
    }

}
