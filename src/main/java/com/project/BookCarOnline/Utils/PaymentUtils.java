package com.project.BookCarOnline.Utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Payment Utility Class
 * Provides HMAC, SHA-256, URL encoding functions for VNPay and MoMo
 */
@Slf4j
public class PaymentUtils {

    /**
     * Generate HMAC SHA-512 signature for VNPay
     */
    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKeySpec);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(result);
        } catch (Exception e) {
            log.error("Error generating HMAC SHA512: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Generate HMAC SHA-256 signature for MoMo
     */
    public static String hmacSHA256(String key, String data) {
        try {
            Mac hmac256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac256.init(secretKeySpec);
            byte[] result = hmac256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(result);
        } catch (Exception e) {
            log.error("Error generating HMAC SHA256: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Generate SHA-256 hash
     */
    public static String sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating SHA-256: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Convert byte array to hex string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Build query string from map (sorted by key)
     */
    public static String buildQueryString(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue);
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }

    /**
     * Build URL with query string
     */
    public static String buildPaymentUrl(String baseUrl, Map<String, String> params) {
        try {
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);
            StringBuilder sb = new StringBuilder();
            sb.append(baseUrl);
            sb.append("?");
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    sb.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                    sb.append("=");
                    sb.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                }
                if (itr.hasNext()) {
                    sb.append("&");
                }
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            log.error("Error building payment URL: {}", e.getMessage());
            return baseUrl;
        }
    }

    /**
     * Generate random request ID
     */
    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Get current timestamp in milliseconds
     */
    public static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * Get current timestamp in VNPay format (yyyyMMddHHmmss)
     */
    public static String getVNPayTimestamp() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        return String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS", calendar);
    }

    /**
     * Get expire time for VNPay (15 minutes from now)
     */
    public static String getVNPayExpireTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        calendar.add(Calendar.MINUTE, 15);
        return String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS", calendar);
    }
}
