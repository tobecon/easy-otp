package com.easyotp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.easyotp.model.OTPAccount;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class OTPDataManager {
    
    private static final String PREF_NAME = "otp_data";
    private static final String KEY_ACCOUNTS = "accounts";
    
    private static OTPDataManager instance;
    private SharedPreferences prefs;
    private Gson gson;
    
    private OTPDataManager(Context context) {
        gson = new Gson();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                prefs = EncryptedSharedPreferences.create(
                        context,
                        PREF_NAME,
                        masterKeyAlias,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } else {
                prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }
    
    public static synchronized OTPDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new OTPDataManager(context.getApplicationContext());
        }
        return instance;
    }
    
    public List<OTPAccount> getAccounts() {
        String json = prefs.getString(KEY_ACCOUNTS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<OTPAccount>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    public void saveAccounts(List<OTPAccount> accounts) {
        String json = gson.toJson(accounts);
        prefs.edit().putString(KEY_ACCOUNTS, json).apply();
    }
    
    public void addAccount(OTPAccount account) {
        List<OTPAccount> accounts = getAccounts();
        accounts.add(account);
        saveAccounts(accounts);
    }
    
    public void removeAccount(String accountId) {
        List<OTPAccount> accounts = getAccounts();
        accounts.removeIf(account -> account.getId().equals(accountId));
        saveAccounts(accounts);
    }
    
    public void updateAccount(OTPAccount account) {
        List<OTPAccount> accounts = getAccounts();
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getId().equals(account.getId())) {
                accounts.set(i, account);
                break;
            }
        }
        saveAccounts(accounts);
    }
    
    public boolean hasAccounts() {
        return !getAccounts().isEmpty();
    }
}
