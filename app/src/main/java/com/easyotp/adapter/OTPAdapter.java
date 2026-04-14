package com.easyotp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.easyotp.R;
import com.easyotp.model.OTPAccount;
import com.easyotp.util.TOTPGenerator;

import java.util.List;

public class OTPAdapter extends RecyclerView.Adapter<OTPAdapter.OTPViewHolder> {
    
    private List<OTPAccount> accounts;
    private OnOTPClickListener listener;
    
    public interface OnOTPClickListener {
        void onOTPClick(OTPAccount account, String code);
        void onOTPActionClick(OTPAccount account, String code);
        void onOTPLongClick(OTPAccount account);
    }
    
    public OTPAdapter(List<OTPAccount> accounts, OnOTPClickListener listener) {
        this.accounts = accounts;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public OTPViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_otp, parent, false);
        return new OTPViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull OTPViewHolder holder, int position) {
        OTPAccount account = accounts.get(position);
        String code = TOTPGenerator.getCurrentTOTP(account.getSecret(), account.getDigits());
        
        holder.tvIssuer.setText(account.getIssuer());
        holder.tvAccount.setText(account.getAccount());
        holder.tvCode.setText(formatCode(code));
        
        holder.btnSendCode.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOTPActionClick(account, code);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOTPClick(account, code);
            }
        });
        
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onOTPLongClick(account);
            }
            return true;
        });
    }
    
    @Override
    public int getItemCount() {
        return accounts.size();
    }
    
    public void updateAccounts(List<OTPAccount> newAccounts) {
        this.accounts = newAccounts;
        notifyDataSetChanged();
    }
    
    private String formatCode(String code) {
        if (code.length() == 6) {
            return code.substring(0, 3) + " " + code.substring(3);
        }
        return code;
    }
    
    static class OTPViewHolder extends RecyclerView.ViewHolder {
        TextView tvIssuer;
        TextView tvAccount;
        TextView tvCode;
        Button btnSendCode;
        
        OTPViewHolder(View itemView) {
            super(itemView);
            tvIssuer = itemView.findViewById(R.id.tvIssuer);
            tvAccount = itemView.findViewById(R.id.tvAccount);
            tvCode = itemView.findViewById(R.id.tvCode);
            btnSendCode = itemView.findViewById(R.id.btnSendCode);
        }
    }
}
