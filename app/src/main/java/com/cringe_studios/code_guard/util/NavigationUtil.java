package com.cringe_studios.code_guard.util;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.cringe_studios.code_guard.R;
import com.cringe_studios.code_guard.fragment.MenuDrawerFragment;
import com.cringe_studios.code_guard.fragment.NamedFragment;

public class NavigationUtil {

    private static void updateActivity(AppCompatActivity activity, NamedFragment newFragment) {
        ActionBar bar = activity.getSupportActionBar();
        if(newFragment == null) newFragment = (NamedFragment) getCurrentFragment(activity.getSupportFragmentManager());
        if(bar != null) bar.setTitle(newFragment.getName());
        activity.invalidateMenu();
    }

    public static void navigate(AppCompatActivity activity, NamedFragment fragment) {
        FragmentManager manager = activity.getSupportFragmentManager();
        navigate(manager, fragment, () -> updateActivity(activity, fragment));
    }

    public static void navigate(AppCompatActivity activity, Class<? extends NamedFragment> fragmentClass, Bundle args) {
        FragmentManager manager = activity.getSupportFragmentManager();
        NamedFragment fragment = instantiateFragment(manager, fragmentClass, args);
        navigate(activity, fragment);
    }

    public static void openMenu(AppCompatActivity activity, Bundle args) {
        FragmentManager manager = activity.getSupportFragmentManager();
        MenuDrawerFragment fragment = instantiateFragment(manager, MenuDrawerFragment.class, args);

        fragment.show(manager, null);
    }

    public static void navigate(Fragment currentFragment, Class<? extends NamedFragment> fragmentClass, Bundle args) {
        navigate((AppCompatActivity) currentFragment.requireActivity(), fragmentClass, args);
    }

    public static void openOverlay(Fragment currentFragment, NamedFragment overlay) {
        AppCompatActivity activity = (AppCompatActivity) currentFragment.requireActivity();
        FragmentManager manager = activity.getSupportFragmentManager();
        manager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.nav_host_fragment_content_main, overlay)
                .runOnCommit(() -> updateActivity(activity, overlay))
                .commit();
    }

    public static void closeOverlay(Fragment currentFragment) {
        AppCompatActivity activity = (AppCompatActivity) currentFragment.requireActivity();
        FragmentManager manager = activity.getSupportFragmentManager();
        manager.beginTransaction()
                .setReorderingAllowed(true)
                .remove(currentFragment)
                .runOnCommit(() -> updateActivity(activity, null))
                .commit();
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
        return getCurrentFragment(activity.getSupportFragmentManager());
    }

    public static Fragment getCurrentFragment(FragmentManager manager) {
        return manager.findFragmentById(R.id.nav_host_fragment_content_main);
    }

}
