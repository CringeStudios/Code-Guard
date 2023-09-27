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
import com.cringe_studios.cringe_authenticator.util.OTPDatabase;
import com.cringe_studios.cringe_authenticator.util.OTPDatabaseException;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;

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

        otpListAdapter = new OTPListAdapter(requireContext(), binding.itemList, this::saveOTPs);
        binding.itemList.setAdapter(otpListAdapter);

        loadOTPs();

        handler = new Handler(Looper.getMainLooper());
        refreshCodes = () -> {
            refreshCodes();
            handler.postDelayed(refreshCodes, 1000L);
        };

        handler.postDelayed(refreshCodes, 1000L);

        return binding.getRoot();
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
                vh.refresh();
            } catch (Exception e) {
                DialogUtil.showErrorDialog(requireContext(), e.getMessage() == null ? "An error occurred while refreshing the code" : e.getMessage());
            }
        }
    }

    public void viewOTP() {
        if(!otpListAdapter.isEditing()) return;

        List<OTPListItem> items = otpListAdapter.getSelectedCodes();
        if(items.size() != 1) return;

        OTPData data = items.get(0).getOTPData();
        DialogUtil.showViewCodeDialog(getLayoutInflater(), data);
    }

    public void editOTP() {
        if(!otpListAdapter.isEditing()) return;

        List<OTPListItem> items = otpListAdapter.getSelectedCodes();
        if(items.size() != 1) return;

        OTPData data = items.get(0).getOTPData();
        DialogUtil.showEditCodeDialog(getLayoutInflater(), data, newData -> {
            otpListAdapter.replace(data, newData);
            saveOTPs();
            otpListAdapter.finishEditing();
        });
    }

    public void moveOTP() {
        if(!otpListAdapter.isEditing()) return;

        List<OTPListItem> items = otpListAdapter.getSelectedCodes();

        DialogUtil.showChooseGroupDialog(requireContext(), group -> {
            OTPDatabase.promptLoadDatabase(requireActivity(), () -> {
                try {
                    for(OTPListItem item : items) {
                        OTPData data = item.getOTPData();
                        OTPDatabase.getLoadedDatabase().addOTP(group, data);
                        otpListAdapter.remove(data);
                    }

                    OTPDatabase.saveDatabase(requireContext(), SettingsUtil.getCryptoParameters(requireContext()));
                    saveOTPs();
                    otpListAdapter.finishEditing();
                } catch (OTPDatabaseException | CryptoException e) {
                    DialogUtil.showErrorDialog(requireContext(), e.toString());
                }
            }, null);
            saveOTPs();
        }, null);
    }

    public void deleteOTP() {
        if(!otpListAdapter.isEditing()) return;

        List<OTPListItem> items = otpListAdapter.getSelectedCodes();

        DialogUtil.showYesNoCancel(requireContext(), R.string.otp_delete_title, R.string.otp_delete_message, () -> {
            for(OTPListItem item : items) {
                otpListAdapter.remove(item.getOTPData());
            }

            saveOTPs();
            finishEditing();
        }, null, null);
    }

    public boolean isEditing() {
        return otpListAdapter.isEditing();
    }

    public void finishEditing() {
        otpListAdapter.finishEditing();
    }

    public boolean hasSelectedMultipleItems() {
        return otpListAdapter.getSelectedCodes().size() > 1;
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
