package com.cringe_studios.cringe_authenticator.util;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.fragment.NamedFragment;

import kotlin.Suppress;

public class NavigationUtil {

    public static void navigate(AppCompatActivity activity, Class<? extends NamedFragment> fragmentClass, Bundle args) {
        FragmentManager manager = activity.getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager();
        NamedFragment fragment = instantiateFragment(manager, fragmentClass, args);

        ActionBar bar = activity.getSupportActionBar();
        navigate(manager, fragment, () -> {
            if(bar != null) bar.setTitle(fragment.getName());
        });
    }

    public static void navigate(Fragment currentFragment, Class<? extends NamedFragment> fragmentClass, Bundle args) {
        navigate((AppCompatActivity) currentFragment.requireActivity(), fragmentClass, args);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Fragment> T instantiateFragment(FragmentManager manager, Class<? extends T> fragmentClass, Bundle args) {
        T fragment = (T) manager.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), fragmentClass.getName());
        if(args != null) fragment.setArguments(args);
        return fragment;
    }

    private static void navigate(FragmentManager manager, Fragment fragment, Runnable onCommit) {
        manager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.nav_host_fragment_content_main, fragment)
                .runOnCommit(onCommit)
                .commit();
    }

    public static Fragment getCurrentFragment(AppCompatActivity activity) {
        return getCurrentFragment(activity.getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager());
    }

    public static Fragment getCurrentFragment(Fragment currentFragment) {
        return getCurrentFragment(currentFragment.getParentFragment().getChildFragmentManager());
    }

    public static Fragment getCurrentFragment(FragmentManager manager) {
        return manager.findFragmentById(R.id.nav_host_fragment_content_main);
    }

}
