package com.easyotp;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.easyotp.adapter.OTPAdapter;
import com.easyotp.model.OTPAccount;
import com.easyotp.util.HIDKeyboard;
import com.easyotp.util.OTPDataManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OTPAdapter.OnOTPClickListener {
    
    private RecyclerView recyclerView;
    private OTPAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fabAdd;
    private Handler handler;
    private Runnable refreshRunnable;
    
    private ActivityResultLauncher<String[]> permissionLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupRecyclerView();
        setupSwipeRefresh();
        setupFab();
        setupPermissionLauncher();
        requestPermissions();
        
        handler = new Handler(Looper.getMainLooper());
        startAutoRefresh();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        fabAdd = findViewById(R.id.fabAdd);
    }
    
    private void setupRecyclerView() {
        adapter = new OTPAdapter(OTPDataManager.getInstance(this).getAccounts(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            refreshOTPList();
            swipeRefresh.setRefreshing(false);
        });
    }
    
    private void setupFab() {
        fabAdd.setOnClickListener(v -> showAddAccountDialog());
    }
    
    private void setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                // Handle permission result
            }
        );
    }
    
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            };
            
            List<String> permissionsToRequest = new java.util.ArrayList<>();
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
            
            if (!permissionsToRequest.isEmpty()) {
                permissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
            }
        }
    }
    
    private void startAutoRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshOTPList();
                handler.postDelayed(this, 1000); // Refresh every second
            }
        };
        handler.post(refreshRunnable);
    }
    
    private void refreshOTPList() {
        adapter.updateAccounts(OTPDataManager.getInstance(this).getAccounts());
    }
    
    @Override
    public void onOTPClick(OTPAccount account, String code) {
        if (HIDKeyboard.isBluetoothKeyboardConnected()) {
            // Try to send via Bluetooth keyboard
            HIDKeyboard.sendStringViaInput(code);
            Toast.makeText(this, "Sent via Bluetooth Keyboard: " + code, Toast.LENGTH_SHORT).show();
        } else {
            // Fallback to clipboard
            copyToClipboard(code);
            Toast.makeText(this, "Copied to clipboard: " + code, Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onOTPLongClick(OTPAccount account) {
        showAccountOptionsDialog(account);
    }
    
    private void copyToClipboard(String code) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("OTP Code", code);
        clipboard.setPrimaryClip(clip);
    }
    
    private void showAddAccountDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_account, null);
        EditText etIssuer = dialogView.findViewById(R.id.etIssuer);
        EditText etAccount = dialogView.findViewById(R.id.etAccount);
        EditText etSecret = dialogView.findViewById(R.id.etSecret);
        
        new AlertDialog.Builder(this)
            .setTitle("Add New Account")
            .setView(dialogView)
            .setPositiveButton("Add", (dialog, which) -> {
                String issuer = etIssuer.getText().toString().trim();
                String account = etAccount.getText().toString().trim();
                String secret = etSecret.getText().toString().trim().toUpperCase();
                
                if (!issuer.isEmpty() && !account.isEmpty() && !secret.isEmpty()) {
                    OTPAccount newAccount = new OTPAccount(issuer, account, secret);
                    OTPDataManager.getInstance(this).addAccount(newAccount);
                    refreshOTPList();
                    Toast.makeText(this, "Account added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void showAccountOptionsDialog(OTPAccount account) {
        String[] options = {"Edit", "Delete"};
        
        new AlertDialog.Builder(this)
            .setTitle(account.getDisplayLabel())
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        showEditAccountDialog(account);
                        break;
                    case 1:
                        deleteAccount(account);
                        break;
                }
            })
            .show();
    }
    
    private void showEditAccountDialog(OTPAccount account) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_account, null);
        EditText etIssuer = dialogView.findViewById(R.id.etIssuer);
        EditText etAccount = dialogView.findViewById(R.id.etAccount);
        EditText etSecret = dialogView.findViewById(R.id.etSecret);
        
        etIssuer.setText(account.getIssuer());
        etAccount.setText(account.getAccount());
        etSecret.setText(account.getSecret());
        
        new AlertDialog.Builder(this)
            .setTitle("Edit Account")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                account.setIssuer(etIssuer.getText().toString().trim());
                account.setAccount(etAccount.getText().toString().trim());
                account.setSecret(etSecret.getText().toString().trim().toUpperCase());
                
                OTPDataManager.getInstance(this).updateAccount(account);
                refreshOTPList();
                Toast.makeText(this, "Account updated", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void deleteAccount(OTPAccount account) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete " + account.getDisplayLabel() + "?")
            .setPositiveButton("Delete", (dialog, which) -> {
                OTPDataManager.getInstance(this).removeAccount(account.getId());
                refreshOTPList();
                Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }
}
