package com.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * User — JavaBean representing a user account in the ProcureGov
 * tender management system.
 *
 * <p>Covers all three system roles:
 * {@link Role#SUPPLIER}, {@link Role#PROCUREMENT_OFFICER}, and
 * {@link Role#EVAL_COMMITTEE}. Passwords are stored as SHA-256
 * hex strings — never in plain text.</p>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * System roles — must match the database ENUM exactly.
     */
    public enum Role {
        /** Registered company or individual submitting bids. */
        SUPPLIER,
        /** Ministry official who manages the tender process. */
        PROCUREMENT_OFFICER,
        /** Ministry official appointed to score bids. */
        EVAL_COMMITTEE
    }

    private int userId;
    private String fullName;
    private String email;
    private String passwordHash;       // SHA-256 hex, never plain text
    private Role role;
    private boolean locked;
    private Timestamp createdAt;

    /**
     * Default no-argument constructor.
     */
    public User() {
    }

    /* ── Getters & Setters ─────────────────────────────── */

    /**
     * @return the user ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @param userId the user ID to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * @return the user's full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName the full name to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return the user's email address (used as login username)
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the SHA-256 password hash (never plain text)
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * @param passwordHash the password hash to set
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * @return the user's system role
     */
    public Role getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * @return {@code true} if the account is currently locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * @param locked {@code true} to lock the account, {@code false} to unlock
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * @return the account creation timestamp
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /* ── Convenience helpers ───────────────────────────── */

    /**
     * Returns {@code true} if this user is a Procurement Officer.
     *
     * @return {@code true} if role is PROCUREMENT_OFFICER
     */
    public boolean isOfficer() {
        return Role.PROCUREMENT_OFFICER.equals(role);
    }

    /**
     * Returns {@code true} if this user is an Evaluation Committee Member.
     *
     * @return {@code true} if role is EVAL_COMMITTEE
     */
    public boolean isEvaluator() {
        return Role.EVAL_COMMITTEE.equals(role);
    }

    /**
     * Returns {@code true} if this user is a Supplier.
     *
     * @return {@code true} if role is SUPPLIER
     */
    public boolean isSupplier() {
        return Role.SUPPLIER.equals(role);
    }

    /**
     * Returns a concise string representation for logging and debugging.
     *
     * @return a string containing the user ID, email, and role
     */
    @Override
    public String toString() {
        return "User{userId=" + userId + ", email='" + email + "', role=" + role + "}";
    }
}