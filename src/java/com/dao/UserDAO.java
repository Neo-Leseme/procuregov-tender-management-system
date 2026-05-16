package com.dao;

import com.model.Supplier;
import com.model.User;

/**
 * UserDAO — data access interface for user and supplier operations.
 *
 * <p>All implementations must go through this interface.
 * No JDBC code is permitted in Servlets or JSPs.</p>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public interface UserDAO {

    /**
     * Finds a user by their email address.
     *
     * @param email the user's email (used as login username)
     * @return the matching {@link User}, or {@code null} if not found
     */
    User findByEmail(String email);

    /**
     * Finds a user by their primary key.
     *
     * @param userId the user's ID
     * @return the matching {@link User}, or {@code null} if not found
     */
    User findById(int userId);

    /**
     * Persists a new user row. Sets the generated {@code userId} on the bean.
     *
     * @param user the user to insert (passwordHash must already be set)
     * @return the generated user ID, or -1 on failure
     */
    int insertUser(User user);

    /**
     * Persists a new supplier row linked to an existing user.
     *
     * @param supplier the supplier details (userId must already be set)
     * @return the generated supplier ID, or -1 on failure
     */
    int insertSupplier(Supplier supplier);

    /**
     * Finds the Supplier record linked to a given user ID.
     *
     * @param userId the user's ID
     * @return the matching {@link Supplier}, or {@code null} if not found
     */
    Supplier findSupplierByUserId(int userId);

    /**
     * Checks whether an email address is already registered.
     *
     * @param email the email to check
     * @return {@code true} if the email already exists in the users table
     */
    boolean emailExists(String email);

    /**
     * Returns the next available supplier registration number in
     * the format {@code SUP-YYYYNNNN}.
     *
     * @return a unique registration number string
     */
    String generateSupplierRegNo();
    
    /**
     * Stores a password reset code for a user. Marks any previous unused
     * codes for this user as expired so only the newest code is valid.
     *
     * @param userId    the user requesting the reset
     * @param code      the 6-character reset code
     * @param expiresAt when the code stops being valid
     */
    void storeResetCode(int userId, String code, java.sql.Timestamp expiresAt);

    /**
     * Verifies a reset code is valid for the given user.
     * Checks that the code matches, has not been used, and has not expired.
     *
     * @param userId the user's ID
     * @param code   the 6-character code to verify
     * @return {@code true} if the code is valid
     */
    boolean verifyResetCode(int userId, String code);

    /**
     * Marks a reset code as used so it cannot be reused.
     *
     * @param userId the user's ID
     * @param code   the code to mark as used
     */
    void markResetCodeUsed(int userId, String code);

    /**
     * Updates a user's password hash.
     *
     * @param userId          the user's ID
     * @param newPasswordHash the new SHA-256 password hash
     */
    void updatePassword(int userId, String newPasswordHash);

    /**
     * Updates the is_locked flag on a user account.
     *
     * @param userId the user to update
     * @param locked {@code true} to lock, {@code false} to unlock
     */
    void setLocked(int userId, boolean locked);

    /**
     * Retrieves the current consecutive failed login count for a user.
     *
     * @param userId the user's ID
     * @return the attempt count, or 0 if no record exists
     */
    int getLoginAttemptCount(int userId);

    /**
     * Increments the failed login attempt counter for a user.
     * Creates the record if it does not yet exist.
     *
     * @param userId the user's ID
     */
    void incrementLoginAttempts(int userId);

    /**
     * Resets the failed login attempt counter for a user to zero.
     *
     * @param userId the user's ID
     */
    void resetLoginAttempts(int userId);

    /**
     * Returns all users with the specified role.
     * Used when appointing evaluators for a tender.
     *
     * @param role the role to filter by
     * @return list of users with that role, never null
     */
    java.util.List<com.model.User> findAllByRole(com.model.User.Role role);
}