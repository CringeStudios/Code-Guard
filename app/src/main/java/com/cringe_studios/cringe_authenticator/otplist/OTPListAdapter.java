package com.cringe_studios.cringe_authenticator.otplist;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.databinding.OtpCodeBinding;
import com.cringe_studios.cringe_authenticator.model.OTPData;
import com.cringe_studios.cringe_authenticator.util.DialogUtil;
import com.cringe_studios.cringe_authenticator_library.OTPException;
import com.cringe_studios.cringe_authenticator_library.OTPType;

import java.util.ArrayList;
import java.util.List;

public class OTPListAdapter extends RecyclerView.Adapter<OTPListItem> {

    private Context context;

    private LayoutInflater inflater;

    private List<OTPData> items;

    private Handler handler;

    private Consumer<OTPData> showMenuCallback;

    public OTPListAdapter(Context context, Consumer<OTPData> showMenuCallback) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.items = new ArrayList<>();
        this.handler = new Handler(Looper.getMainLooper());
        this.showMenuCallback = showMenuCallback;
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
        holder.getBinding().label.setText(String.format("%s%s", data.getIssuer() == null || data.getIssuer().isEmpty() ? "" : data.getIssuer() + ": ", data.getName()));
        holder.getBinding().progress.setVisibility(data.getType() == OTPType.TOTP ? View.VISIBLE : View.INVISIBLE);

        holder.getBinding().getRoot().setOnClickListener(view -> {
            Log.i("CLICKED", "CLICKED: " + view.isClickable());
            if(data.getType() != OTPType.HOTP) return;

            // Click delay for HOTP
            view.setClickable(false);
            data.incrementCounter();

            try {
                holder.getBinding().otpCode.setText(data.getPin());
            }catch(OTPException e) {
                DialogUtil.showErrorDialog(context, context.getString(R.string.otp_add_error, e.getMessage() != null ? e.getMessage() : e.toString()));
                return;
            }

            Toast.makeText(view.getContext(), R.string.hotp_generated_new_code, Toast.LENGTH_SHORT).show();

            handler.postDelayed(() -> view.setClickable(true), 5000);
        });

        holder.getBinding().getRoot().setOnLongClickListener(view -> {
            showMenuCallback.accept(holder.getOTPData());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<OTPData> getItems() {
        return items;
    }

    public void add(OTPData data) {
        items.add(data);
        notifyItemInserted(items.size() - 1);
    }

    public void replace(OTPData oldData, OTPData newData) {
        int index = items.indexOf(oldData);
        if(index == -1) return;
        items.set(index, newData);
        notifyItemChanged(index);
    }

    public void remove(OTPData data) {
        int index = items.indexOf(data);
        if(index == -1) return;
        items.remove(data);
        notifyItemRemoved(index);
    }

}
