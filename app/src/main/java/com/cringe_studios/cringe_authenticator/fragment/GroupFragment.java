package com.cringe_studios.cringe_authenticator.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.cringe_studios.cringe_authenticator.OTPData;
import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.databinding.FragmentGroupBinding;
import com.cringe_studios.cringe_authenticator.otplist.OTPListAdapter;
import com.cringe_studios.cringe_authenticator.otplist.OTPListItem;
import com.cringe_studios.cringe_authenticator.util.DialogUtil;
import com.cringe_studios.cringe_authenticator.util.FabUtil;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;
import com.cringe_studios.cringe_authenticator_library.OTPException;
import com.cringe_studios.cringe_authenticator_library.OTPType;

import java.util.List;

public class GroupFragment extends NamedFragment {

    public static final String BUNDLE_GROUP = "group";

    private String groupName;

    private FragmentGroupBinding binding;

    private Handler handler;

    private Runnable refreshCodes;

    private OTPListAdapter otpListAdapter;

    @Override
    public String getName() {
        return groupName;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGroupBinding.inflate(inflater, container, false);

        groupName = requireArguments().getString(GroupFragment.BUNDLE_GROUP);

        FabUtil.showFabs(requireActivity());

        otpListAdapter = new OTPListAdapter(requireContext(), data -> showOTPDialog(data));

        binding.itemList.setAdapter(otpListAdapter);

        loadOTPs();

        handler = new Handler(Looper.getMainLooper());
        refreshCodes = () -> {
            refreshCodes();
            handler.postDelayed(refreshCodes, 1000L);
        };

        handler.post(refreshCodes);

        return binding.getRoot();
    }

    private void showOTPDialog(OTPData data) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.edit_otp_title)
                .setItems(R.array.view_edit_delete, (dialog, which) -> {
                    switch(which) {
                        case 0:
                            DialogUtil.showViewCodeDialog(getLayoutInflater(), data, newData -> {
                                otpListAdapter.replace(data, newData);
                                saveOTPs();
                            }, () -> showOTPDialog(data));
                            break;
                        case 1:
                            DialogUtil.showEditCodeDialog(getLayoutInflater(), data, newData -> {
                            otpListAdapter.replace(data, newData);
                                saveOTPs();
                            }, () -> showOTPDialog(data));
                            break;
                        case 2: break;
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {})
                .show();

        /*switch(data.getType()) {
            case HOTP:
                DialogUtil.showHOTPDialog(getLayoutInflater(), data, newData -> {
                    otpListAdapter.replace(data, newData);
                    saveOTPs();
                });
                break;
            case TOTP:
                DialogUtil.showTOTPDialog(getLayoutInflater(), data, newData -> {
                    otpListAdapter.replace(data, newData);
                    saveOTPs();
                });
                break;
        }*/
    }

    private void saveOTPs() {
        SettingsUtil.updateOTPs(requireContext(), groupName, otpListAdapter.getItems());
        refreshCodes();
    }

    private void loadOTPs() {
        List<OTPData> data = SettingsUtil.getOTPs(requireContext(), groupName);

        for(OTPData otp : data) {
            otpListAdapter.add(otp);
        }
    }

    public void addOTP(OTPData data) {
        SettingsUtil.addOTP(requireContext(), groupName, data);
        otpListAdapter.add(data);
    }

    public void refreshCodes() {
        for(int i = 0; i < binding.itemList.getChildCount(); i++) {
            OTPListItem vh = (OTPListItem) binding.itemList.findViewHolderForAdapterPosition(i);
            if(vh == null) continue;
            try {
                vh.getBinding().otpCode.setText(vh.getOTPData().getPin());
            } catch (OTPException e) {
                DialogUtil.showErrorDialog(requireContext(), e.getMessage() == null ? "An error occurred while refreshing the code" : e.getMessage());
            }

            if(vh.getOTPData().getType() == OTPType.TOTP) {
                long timeDiff = vh.getOTPData().getNextDueTime() - System.currentTimeMillis() / 1000;
                vh.getBinding().progress.setProgress((int) ((1 - ((double) timeDiff / vh.getOTPData().getPeriod())) * 100));
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(refreshCodes);
        super.onDestroy();
    }

}
