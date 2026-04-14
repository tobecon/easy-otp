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

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

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
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<ScanOptions> barcodeLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupRecyclerView();
        setupSwipeRefresh();
        initScanLaunchers();
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
    
    private void initScanLaunchers() {
        barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result != null && result.getContents() != null) {
                    parseOtpUri(result.getContents());
                } else {
                    Toast.makeText(this, "No QR code detected", Toast.LENGTH_SHORT).show();
                }
            }
        );

        cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) {
                    launchQrScanner();
                } else {
                    Toast.makeText(this, "Camera permission is required for QR scanning", Toast.LENGTH_SHORT).show();
                }
            }
        );
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
            HIDKeyboard.sendStringViaInput(code);
            Toast.makeText(this, "Sent via Bluetooth Keyboard: " + code, Toast.LENGTH_SHORT).show();
        } else {
            copyToClipboard(code);
            Toast.makeText(this, "Copied to clipboard: " + code, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onOTPActionClick(OTPAccount account, String code) {
        copyToClipboard(code);
        if (HIDKeyboard.isBluetoothKeyboardConnected()) {
            HIDKeyboard.sendStringViaInput(code);
            Toast.makeText(this, "Copied and typed via Bluetooth keyboard", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Copied to clipboard; Bluetooth keyboard not connected", Toast.LENGTH_SHORT).show();
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
            .setNeutralButton("Scan QR", (dialog, which) -> {
                requestCameraPermissionAndScan();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void requestCameraPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            launchQrScanner();
        }
    }

    private void launchQrScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan OTP QR code");
        options.setBeepEnabled(false);
        options.setOrientationLocked(true);
        barcodeLauncher.launch(options);
    }

    private void parseOtpUri(String otpUri) {
        try {
            if (otpUri == null || !otpUri.startsWith("otpauth://")) {
                throw new IllegalArgumentException("Unsupported QR code format");
            }

            java.net.URI uri = new java.net.URI(otpUri);
            String type = uri.getHost();
            if (type == null || type.isEmpty()) {
                type = "totp";
            }

            String path = uri.getPath();
            String label = "";
            if (path != null && path.length() > 1) {
                label = path.substring(1);
            }

            String issuer = null;
            String account = null;
            if (label.contains(":")) {
                issuer = label.substring(0, label.indexOf(":"));
                account = label.substring(label.indexOf(":") + 1);
            } else {
                account = label;
            }

            String query = uri.getQuery();
            String secret = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2) {
                        String key = java.net.URLDecoder.decode(pair[0], "UTF-8");
                        String value = java.net.URLDecoder.decode(pair[1], "UTF-8");
                        if (key.equalsIgnoreCase("secret")) {
                            secret = value.replace(" ", "").toUpperCase();
                        } else if (key.equalsIgnoreCase("issuer") && (issuer == null || issuer.isEmpty())) {
                            issuer = value;
                        }
                    }
                }
            }

            if (issuer == null) {
                issuer = "";
            }
            if (account == null) {
                account = "";
            }
            if (secret == null || secret.isEmpty()) {
                throw new IllegalArgumentException("OTP secret key is missing");
            }

            OTPAccount newAccount = new OTPAccount(issuer, account, secret);
            OTPDataManager.getInstance(this).addAccount(newAccount);
            refreshOTPList();
            Toast.makeText(this, "OTP account added: " + newAccount.getDisplayLabel(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Unable to parse OTP QR code", Toast.LENGTH_LONG).show();
        }
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
