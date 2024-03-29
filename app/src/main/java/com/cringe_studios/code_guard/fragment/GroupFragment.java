package com.cringe_studios.code_guard.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cringe_studios.code_guard.R;
import com.cringe_studios.code_guard.crypto.CryptoException;
import com.cringe_studios.code_guard.databinding.FragmentGroupBinding;
import com.cringe_studios.code_guard.model.OTPData;
import com.cringe_studios.code_guard.otplist.OTPListAdapter;
import com.cringe_studios.code_guard.otplist.OTPListItem;
import com.cringe_studios.code_guard.util.DialogUtil;
import com.cringe_studios.code_guard.util.NavigationUtil;
import com.cringe_studios.code_guard.util.OTPDatabase;
import com.cringe_studios.code_guard.util.OTPDatabaseException;
import com.cringe_studios.code_guard.util.SettingsUtil;

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

    public String getGroupID() {
        return groupID;
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
            OTPDatabase.getLoadedDatabase().promptAddOTPs(requireContext(), groupID, () -> {
                try {
                    OTPDatabase.saveDatabase(requireContext(), SettingsUtil.getCryptoParameters(requireContext()));
                    for(OTPData d : data) otpListAdapter.add(d);
                } catch (OTPDatabaseException | CryptoException e) {
                    DialogUtil.showErrorDialog(requireContext(), getString(R.string.error_database_save), e);
                }
            }, data);
        }, null);
    }

    public void refreshCodes() {
        for(int i = 0; i < binding.itemList.getChildCount(); i++) {
            OTPListItem vh = (OTPListItem) binding.itemList.findViewHolderForAdapterPosition(i);
            if(vh == null) continue;
            try {
                vh.refresh();
            } catch (Exception e) {
                DialogUtil.showErrorDialog(requireContext(), getString(R.string.error_otp_refresh), e);
            }
        }
    }

    public void viewOTP() {
        if(!otpListAdapter.isEditing()) return;

        List<OTPListItem> items = otpListAdapter.getSelectedCodes();
        if(items.size() != 1) return;

        OTPData data = items.get(0).getOTPData();
        //DialogUtil.showViewCodeDialog(getLayoutInflater(), data);
        NavigationUtil.openOverlay(this, new EditOTPFragment(data, true, null));
    }

    public void editOTP() {
        if(!otpListAdapter.isEditing()) return;

        List<OTPListItem> items = otpListAdapter.getSelectedCodes();
        if(items.size() != 1) return;

        OTPData data = items.get(0).getOTPData();
        NavigationUtil.openOverlay(this, new EditOTPFragment(data, false, newData -> {
            otpListAdapter.replace(data, newData);
            saveOTPs();
            otpListAdapter.finishEditing();
        }));
    }

    public void moveOTP() {
        if(!otpListAdapter.isEditing()) return;

        List<OTPListItem> items = otpListAdapter.getSelectedCodes();

        DialogUtil.showChooseGroupDialog(requireContext(), group -> {
            OTPDatabase.promptLoadDatabase(requireActivity(), () -> {
                OTPData[] otps = new OTPData[items.size()];
                for(int i = 0; i < otps.length; i++) {
                    OTPData data = items.get(i).getOTPData();
                    otps[i] = data;
                }

                OTPDatabase.getLoadedDatabase().promptAddOTPs(requireContext(), group, () -> {
                    try {
                        OTPDatabase.saveDatabase(requireContext(), SettingsUtil.getCryptoParameters(requireContext()));
                        for(OTPData data : otps) otpListAdapter.remove(data);
                        saveOTPs();
                        otpListAdapter.finishEditing();
                    } catch (OTPDatabaseException | CryptoException e) {
                        DialogUtil.showErrorDialog(requireContext(), e.toString());
                    }
                }, otps);
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

    public void filter(String newText) {
        otpListAdapter.filter(newText);
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
