package com.cringe_studios.cringe_authenticator.otplist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cringe_studios.cringe_authenticator.OTPData;
import com.cringe_studios.cringe_authenticator.databinding.OtpCodeBinding;

import java.util.ArrayList;
import java.util.List;

public class OTPListAdapter extends RecyclerView.Adapter<OTPListItem> {

    private LayoutInflater inflater;

    private List<OTPData> items;

    public OTPListAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        this.items = new ArrayList<>();
    }

    @NonNull
    @Override
    public OTPListItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        OtpCodeBinding binding = OtpCodeBinding.inflate(inflater, parent, false);
        return new OTPListItem(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OTPListItem holder, int position) {
        holder.setOTPData(items.get(position));
        holder.getBinding().label.setText(holder.getOTPData().getName());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void add(OTPData data) {
        items.add(data);
        notifyItemInserted(items.size() - 1);
    }

    public void remove(OTPData data) {
        int index = items.indexOf(data);
        items.remove(data);
        notifyItemRemoved(index);
    }

}
