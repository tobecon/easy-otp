package com.easyotp.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.nio.ByteBuffer;

public class TOTPGenerator {
    
    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final int[] DIGITS_POWER = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000};
    
    public static String generateTOTP(String secret, long timeStep, int digits) {
        try {
            byte[] key = decodeBase32(secret);
            byte[] data = ByteBuffer.allocate(8).putLong(timeStep).array();
            byte[] hash = hmacSha1(key, data);
            
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24) |
                        ((hash[offset + 1] & 0xFF) << 16) |
                        ((hash[offset + 2] & 0xFF) << 8) |
                        (hash[offset + 3] & 0xFF);
            
            int otp = binary % DIGITS_POWER[digits];
            return String.format("%0" + digits + "d", otp);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }
    
    public static String getCurrentTOTP(String secret, int digits) {
        long timeStep = System.currentTimeMillis() / 1000 / 30;
        return generateTOTP(secret, timeStep, digits);
    }
    
    private static byte[] decodeBase32(String encoded) {
        encoded = encoded.toUpperCase().replaceAll("\\s+", "");
        
        StringBuilder binary = new StringBuilder();
        for (char c : encoded.toCharArray()) {
            int val;
            if (c >= 'A' && c <= 'Z') {
                val = c - 'A';
            } else if (c >= '2' && c <= '7') {
                val = 26 + (c - '2');
            } else {
                continue;
            }
            for (int i = 4; i >= 0; i--) {
                binary.append((val >> i) & 1);
            }
        }
        
        byte[] result = new byte[binary.length() / 8];
        for (int i = 0; i < result.length; i++) {
            int byteVal = 0;
            for (int j = 0; j < 8; j++) {
                byteVal = (byteVal << 1) | (binary.charAt(i * 8 + j) - '0');
            }
            result[i] = (byte) byteVal;
        }
        
        return result;
    }
    
    private static byte[] hmacSha1(byte[] key, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(key, HMAC_ALGORITHM);
        mac.init(keySpec);
        return mac.doFinal(data);
    }
}
