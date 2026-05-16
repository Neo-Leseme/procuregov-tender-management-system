package com.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PasswordUtil — SHA-256 password hashing utility.
 *
 * <p>Passwords are NEVER stored in plain text. This utility produces a
 * 64-character lowercase hex string using the SHA-256 algorithm as
 * required by the assessment specification (Module 1).</p>
 *
 * <p>Usage:
 * <pre>
 *   String hash = PasswordUtil.hash("myPassword123!");
 *   boolean ok  = PasswordUtil.verify("myPassword123!", storedHash);
 * </pre>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public final class PasswordUtil {

    private static final Logger LOGGER = Logger.getLogger(PasswordUtil.class.getName());
    private static final String ALGORITHM = "SHA-256";

    /**
     * Private constructor — utility class, not instantiable.
     */
    private PasswordUtil() {}

    /**
     * Hashes a plain-text password using SHA-256.
     *
     * @param plainText the raw password entered by the user
     * @return a 64-character lowercase hex string, or {@code null} if hashing fails
     */
    public static String hash(String plainText) {
        if (plainText == null || plainText.isEmpty()) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed by the Java spec — this should never happen
            LOGGER.log(Level.SEVERE, "SHA-256 algorithm not available", e);
            return null;
        }
    }

    /**
     * Verifies a plain-text password against a stored SHA-256 hash.
     *
     * @param plainText  the raw password to check
     * @param storedHash the hash retrieved from the database
     * @return {@code true} if the hashes match
     */
    public static boolean verify(String plainText, String storedHash) {
        if (plainText == null || storedHash == null) return false;
        String computedHash = hash(plainText);
        return storedHash.equalsIgnoreCase(computedHash);
    }

    /**
     * Converts a byte array to a lowercase hexadecimal string.
     *
     * @param bytes the raw bytes from the MessageDigest
     * @return a lowercase hex string representation
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}