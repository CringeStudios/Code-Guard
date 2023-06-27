package com.cringe_studios.cringe_authenticator.otplist;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cringe_studios.cringe_authenticator.OTPData;
import com.cringe_studios.cringe_authenticator.databinding.OtpCodeBinding;
import com.cringe_studios.cringe_authenticator_library.OTPType;

import java.util.ArrayList;
import java.util.List;

public class OTPListAdapter extends RecyclerView.Adapter<OTPListItem> {

    private LayoutInflater inflater;

    private List<OTPData> items;

    private Handler handler;

    public OTPListAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        this.items = new ArrayList<>();
        this.handler = new Handler(Looper.getMainLooper());
    }

    @NonNull
    @Override
    public OTPListItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        OtpCodeBinding binding = OtpCodeBinding.inflate(inflater, parent, false);
        return new OTPListItem(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OTPListItem holder, int position) {
        OTPData data = items.get(position);

        holder.setOTPData(data);
        holder.getBinding().label.setText(holder.getOTPData().getName());
        holder.getBinding().progress.setVisibility(holder.getOTPData().getType() == OTPType.TOTP ? View.VISIBLE : View.GONE);

        holder.getBinding().getRoot().setOnClickListener(view -> {
            if(data.getType() != OTPType.HOTP) return;

            // Click delay for HOTP
            view.setClickable(false);
            Toast.makeText(view.getContext(), "Generated new code", Toast.LENGTH_LONG).show();
            data.incrementCounter();
            holder.getBinding().otpCode.setText(data.getPin());
            handler.postDelayed(() -> view.setClickable(true), 5000);
        });
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
        if(index == -1) return;
        items.remove(data);
        notifyItemRemoved(index);
    }

}
