package com.easyotp.model;

import java.io.Serializable;

public class OTPAccount implements Serializable {
    private String id;
    private String issuer;
    private String account;
    private String secret;
    private String type; // TOTP or HOTP
    private int digits;
    private long counter;
    
    public OTPAccount() {
        this.id = java.util.UUID.randomUUID().toString();
        this.digits = 6;
        this.type = "TOTP";
        this.counter = 0;
    }
    
    public OTPAccount(String issuer, String account, String secret) {
        this();
        this.issuer = issuer;
        this.account = account;
        this.secret = secret;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public int getDigits() { return digits; }
    public void setDigits(int digits) { this.digits = digits; }
    
    public long getCounter() { return counter; }
    public void setCounter(long counter) { this.counter = counter; }
    
    public String getDisplayLabel() {
        if (issuer != null && !issuer.isEmpty()) {
            return issuer + " (" + account + ")";
        }
        return account;
    }
}
