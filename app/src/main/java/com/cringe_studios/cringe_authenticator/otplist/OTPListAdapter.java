package com.cringe_studios.cringe_authenticator.otplist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.cringe_studios.cringe_authenticator.BaseActivity;
import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.databinding.OtpCodeBinding;
import com.cringe_studios.cringe_authenticator.icon.Icon;
import com.cringe_studios.cringe_authenticator.icon.IconPack;
import com.cringe_studios.cringe_authenticator.icon.IconUtil;
import com.cringe_studios.cringe_authenticator.model.OTPData;
import com.cringe_studios.cringe_authenticator.util.DialogUtil;
import com.cringe_studios.cringe_authenticator_library.OTPException;
import com.cringe_studios.cringe_authenticator_library.OTPType;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OTPListAdapter extends RecyclerView.Adapter<OTPListItem> {

    private Context context;

    private RecyclerView recyclerView;

    private LayoutInflater inflater;

    private List<OTPData> items;

    private Handler handler;

    private Runnable saveOTPs;

    private boolean editing;

    public OTPListAdapter(Context context, RecyclerView recyclerView, Runnable saveOTPs) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.inflater = LayoutInflater.from(context);
        this.items = new ArrayList<>();
        this.handler = new Handler(Looper.getMainLooper());
        this.saveOTPs = saveOTPs;

        attachTouchHelper(recyclerView);
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
        holder.setSelected(false);

        try {
            holder.refresh();
        } catch (OTPException e) {
            DialogUtil.showErrorDialog(context, context.getString(R.string.otp_add_error, e.getMessage() != null ? e.getMessage() : e.toString()));
        }

        holder.getBinding().label.setText(String.format("%s%s", data.getIssuer() == null || data.getIssuer().isEmpty() ? "" : data.getIssuer() + ": ", data.getName()));
        holder.getBinding().progress.setVisibility(data.getType() == OTPType.TOTP ? View.VISIBLE : View.GONE);
        holder.getBinding().refresh.setVisibility(data.getType() == OTPType.HOTP ? View.VISIBLE : View.GONE);

        List<IconPack> packs = IconUtil.loadAllIconPacks(context);

        byte[] imageBytes = null;
        String imageData = holder.getOTPData().getImageData();
        if(!OTPData.IMAGE_DATA_NONE.equals(imageData)) {
            if (imageData == null) {
                for (IconPack pack : packs) {
                    Icon ic = pack.findIconForIssuer(holder.getOTPData().getIssuer());
                    if (ic != null) {
                        imageBytes = ic.getBytes();
                        // TODO: save new icon
                        break;
                    }
                }
            } else {
                try {
                    imageBytes = Base64.decode(imageData, Base64.DEFAULT);
                }catch(IllegalArgumentException ignored) {
                    // Just use default icon
                }
            }
        }

        if(imageBytes == null) {
            String issuer = data.getIssuer() != null ? data.getIssuer() : data.getName();
            holder.getBinding().otpCodeIcon.setImageBitmap(IconUtil.generateCodeImage(issuer));
        }else {
            Bitmap bm = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if(bm != null) {
                holder.getBinding().otpCodeIcon.setImageBitmap(bm);
            }else {
                try {
                    SVG svg = SVG.getFromInputStream(new ByteArrayInputStream(imageBytes));
                    holder.getBinding().otpCodeIcon.setSVG(svg);
                }catch(SVGParseException e) {
                    holder.getBinding().otpCodeIcon.setImageBitmap(IconUtil.generateCodeImage(data.getIssuer()));
                }
            }
        }

        holder.getBinding().refresh.setOnClickListener(view -> {
            if (data.getType() != OTPType.HOTP) return;

            if(!holder.isCodeShown()) {
                showCode(holder);
            }

            // Click delay for HOTP
            view.setEnabled(false);
            data.incrementCounter();
            saveOTPs.run();

            Toast.makeText(view.getContext(), R.string.hotp_generated_new_code, Toast.LENGTH_SHORT).show();

            handler.postDelayed(() -> view.setEnabled(true), 5000);
        });

        holder.getBinding().getRoot().setOnClickListener(view -> {
            if(!editing) {
                if(!holder.isCodeShown()) {
                    showCode(holder);
                }else {
                    holder.setCodeShown(false);
                }

                try {
                    holder.refresh();
                } catch (OTPException e) {
                    DialogUtil.showErrorDialog(context, context.getString(R.string.otp_add_error, e.getMessage() != null ? e.getMessage() : e.toString()));
                }
            }else {
                holder.setSelected(!holder.isSelected());
                if(getSelectedCodes().isEmpty()) editing = false;
                ((BaseActivity) context).invalidateMenu();
            }
        });

        holder.getBinding().getRoot().setOnLongClickListener(view -> {
            if(editing) return true;

            holder.setSelected(true);
            editing = true;
            ((BaseActivity) context).invalidateMenu();
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

    public boolean isEditing() {
        return editing;
    }

    public void finishEditing() {
        if(!editing) return;

        editing = false;
        for(OTPListItem item : getSelectedCodes()) {
            item.setSelected(false);
        }

        ((BaseActivity) context).invalidateMenu();
    }

    public List<OTPListItem> getSelectedCodes() {
        if(!editing) return Collections.emptyList();

        List<OTPListItem> selected = new ArrayList<>();
        for(int i = 0; i < items.size(); i++) {
            OTPListItem vh = (OTPListItem) recyclerView.findViewHolderForAdapterPosition(i);
            if(vh == null) continue;
            if(vh.isSelected()) selected.add(vh);
        }
        return selected;
    }

    private void showCode(OTPListItem item) {
        for(OTPListItem h : getCodes()) {
            if(h.isCodeShown()) {
                h.setCodeShown(false);
                try {
                    h.refresh();
                } catch (OTPException e) {
                    DialogUtil.showErrorDialog(context, e.getMessage() == null ? "An error occurred while refreshing the code" : e.getMessage());
                }
            }
        }

        item.setCodeShown(true);
    }

    private List<OTPListItem> getCodes() {
        List<OTPListItem> is = new ArrayList<>();
        for(int i = 0; i < items.size(); i++) {
            OTPListItem vh = (OTPListItem) recyclerView.findViewHolderForAdapterPosition(i);
            if(vh == null) continue;
            is.add(vh);
        }
        return is;
    }

    private void attachTouchHelper(RecyclerView view) {
        new ItemTouchHelper(new OTPListAdapter.TouchHelperCallback()).attachToRecyclerView(view);
    }

    private class TouchHelperCallback extends ItemTouchHelper.Callback {

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            Collections.swap(items, viewHolder.getAdapterPosition(), target.getAdapterPosition());
            notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            saveOTPs.run();
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

        @Override
        public boolean isLongPressDragEnabled() {
            return editing;
        }
    }

}
