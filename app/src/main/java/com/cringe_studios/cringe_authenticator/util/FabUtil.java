package com.cringe_studios.cringe_authenticator.util;

import android.app.Activity;
import android.view.View;

import com.cringe_studios.cringe_authenticator.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FabUtil {

    public static void showFabs(Activity activity) {
        FloatingActionButton fabScan = activity.findViewById(R.id.fab_scan);
        if(fabScan != null) {
            fabScan.setVisibility(View.VISIBLE);
            fabScan.setClickable(true);
            fabScan.animate().translationX(-activity.getResources().getDimension(R.dimen.fab1_offset));
        }

        FloatingActionButton fabScanImage = activity.findViewById(R.id.fab_scan_image);
        if(fabScanImage != null) {
            fabScanImage.setVisibility(View.VISIBLE);
            fabScanImage.setClickable(true);
            fabScanImage.animate().translationX(-activity.getResources().getDimension(R.dimen.fab2_offset));
        }

        FloatingActionButton fabInput = activity.findViewById(R.id.fab_input);
        if(fabInput != null) {
            fabInput.setVisibility(View.VISIBLE);
            fabInput.setClickable(true);
            fabInput.animate().translationX(-activity.getResources().getDimension(R.dimen.fab3_offset));
        }
    }

    public static void hideFabs(Activity activity) {
        FloatingActionButton fabScan = activity.findViewById(R.id.fab_scan);
        if(fabScan != null) {
            fabScan.setClickable(false);
            fabScan.animate().translationX(0).withEndAction(() -> fabScan.setVisibility(View.GONE));
        }

        FloatingActionButton fabScanImage = activity.findViewById(R.id.fab_scan_image);
        if(fabScan != null) {
            fabScanImage.setClickable(false);
            fabScanImage.animate().translationX(0).withEndAction(() -> fabScanImage.setVisibility(View.GONE));
        }

        FloatingActionButton fabInput = activity.findViewById(R.id.fab_input);
        if(fabInput != null) {
            fabInput.setClickable(false);
            fabInput.animate().translationX(0).withEndAction(() -> fabInput.setVisibility(View.GONE));
        }
    }

}
