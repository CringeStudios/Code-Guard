package com.cringe_studios.cringe_authenticator.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.crypto.CryptoException;
import com.cringe_studios.cringe_authenticator.databinding.FragmentGroupBinding;
import com.cringe_studios.cringe_authenticator.model.OTPData;
import com.cringe_studios.cringe_authenticator.otplist.OTPListAdapter;
import com.cringe_studios.cringe_authenticator.otplist.OTPListItem;
import com.cringe_studios.cringe_authenticator.util.DialogUtil;
import com.cringe_studios.cringe_authenticator.util.FabUtil;
import com.cringe_studios.cringe_authenticator.util.OTPDatabase;
import com.cringe_studios.cringe_authenticator.util.OTPDatabaseException;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;
import com.cringe_studios.cringe_authenticator.util.StyledDialogBuilder;
import com.cringe_studios.cringe_authenticator_library.OTPException;
import com.cringe_studios.cringe_authenticator_library.OTPType;

import java.util.List;

public class GroupFragment extends NamedFragment {

    public static final String BUNDLE_GROUP = "group";

    private String groupID;

    private FragmentGroupBinding binding;

    private Handler handler;

    private Runnable refreshCodes;

    private OTPListAdapter otpListAdapter;

    @Override
    public String getName() {
        return SettingsUtil.getGroupName(requireContext(), groupID);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGroupBinding.inflate(inflater, container, false);

        groupID = requireArguments().getString(GroupFragment.BUNDLE_GROUP);

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
        new StyledDialogBuilder(requireContext())
                .setTitle(R.string.edit_otp_title)
                .setItems(R.array.view_edit_move_delete, (dialog, which) -> {
                    switch(which) {
                        case 0:
                            DialogUtil.showViewCodeDialog(getLayoutInflater(), data, () -> showOTPDialog(data));
                            break;
                        case 1:
                            DialogUtil.showEditCodeDialog(getLayoutInflater(), data, newData -> {
                                otpListAdapter.replace(data, newData);
                                saveOTPs();
                            }, () -> showOTPDialog(data));
                            break;
                        case 2:
                            DialogUtil.showChooseGroupDialog(requireContext(), group -> {
                                OTPDatabase.promptLoadDatabase(requireActivity(), () -> {
                                    try {
                                        OTPDatabase.getLoadedDatabase().addOTP(group, data);
                                        OTPDatabase.saveDatabase(requireContext(), SettingsUtil.getCryptoParameters(requireContext()));
                                        otpListAdapter.remove(data);
                                        saveOTPs();
                                    } catch (OTPDatabaseException | CryptoException e) {
                                        DialogUtil.showErrorDialog(requireContext(), e.toString());
                                    }
                                }, null);
                                saveOTPs();
                            }, null);
                            break;
                        case 3:
                            new StyledDialogBuilder(requireContext())
                                    .setTitle(R.string.otp_delete_title)
                                    .setMessage(R.string.otp_delete_message)
                                    .setPositiveButton(R.string.yes, (d, w) -> {
                                        otpListAdapter.remove(data);
                                        saveOTPs();
                                    })
                                    .setNegativeButton(R.string.no, (d, w) -> {})
                                    .show();
                            break;
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {})
                .show();
    }

    private void saveOTPs() {
        OTPDatabase.promptLoadDatabase(requireActivity(), () -> {
            try {
                OTPDatabase.getLoadedDatabase().updateOTPs(groupID, otpListAdapter.getItems());
                OTPDatabase.saveDatabase(requireContext(), SettingsUtil.getCryptoParameters(requireContext()));
                refreshCodes();
            } catch (OTPDatabaseException | CryptoException e) {
                DialogUtil.showErrorDialog(requireContext(), e.toString());
            }
        }, null);
    }

    private void loadOTPs() {
        OTPDatabase.promptLoadDatabase(requireActivity(), () -> {
            List<OTPData> data = OTPDatabase.getLoadedDatabase().getOTPs(groupID);

            for(OTPData otp : data) {
                otpListAdapter.add(otp);
            }
        }, null);
    }

    public void addOTP(OTPData... data) {
        OTPDatabase.promptLoadDatabase(requireActivity(), () -> {
            try {
                for(OTPData d : data) OTPDatabase.getLoadedDatabase().addOTP(groupID, d);
                OTPDatabase.saveDatabase(requireContext(), SettingsUtil.getCryptoParameters(requireContext()));
                for(OTPData d : data) otpListAdapter.add(d);
            } catch (OTPDatabaseException | CryptoException e) {
                DialogUtil.showErrorDialog(requireContext(), "Failed to save database: " + e);
            }
        }, null);
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
                double progress = 1 - ((double) timeDiff / vh.getOTPData().getPeriod());
                vh.getBinding().progress.setImageLevel((int) (progress * 10_000));
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
