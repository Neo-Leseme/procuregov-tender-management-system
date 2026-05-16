package com.dao;

import com.model.Supplier;
import com.model.User;

import java.sql.*;
import java.time.Year;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UserDAOImpl — JDBC implementation of {@link UserDAO}.
 *
 * <p>All database access for users and suppliers goes through this class.
 * SQLExceptions are caught, logged, and never exposed to the UI layer.</p>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public class UserDAOImpl implements UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAOImpl.class.getName());

    /* ── findByEmail ─────────────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public User findByEmail(String email) {
        final String sql = "SELECT user_id, full_name, email, password_hash, role, is_locked, created_at "
                + "FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "UserDAO.findByEmail failed for email=" + email, e);
        }
        return null;
    }

    /* ── findById ────────────────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public User findById(int userId) {
        final String sql = "SELECT user_id, full_name, email, password_hash, role, is_locked, created_at "
                + "FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "UserDAO.findById failed for userId=" + userId, e);
        }
        return null;
    }

    /* ── insertUser ──────────────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public int insertUser(User user) {
        final String sql = "INSERT INTO users (full_name, email, password_hash, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole().name());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int generatedId = keys.getInt(1);
                        user.setUserId(generatedId);
                        return generatedId;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "UserDAO.insertUser failed for email=" + user.getEmail(), e);
        }
        return -1;
    }

    /* ── insertSupplier ──────────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public int insertSupplier(Supplier supplier) {
        final String sql = "INSERT INTO suppliers "
                + "(user_id, company_name, registration_no, physical_address, contact_number) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, supplier.getUserId());
            ps.setString(2, supplier.getCompanyName());
            ps.setString(3, supplier.getRegistrationNo());
            ps.setString(4, supplier.getPhysicalAddress());
            ps.setString(5, supplier.getContactNumber());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        supplier.setSupplierId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "UserDAO.insertSupplier failed for userId=" + supplier.getUserId(), e);
        }
        return -1;
    }

    /* ── findSupplierByUserId ────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public Supplier findSupplierByUserId(int userId) {
        final String sql = "SELECT supplier_id, user_id, company_name, registration_no, "
                + "physical_address, contact_number FROM suppliers WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSupplier(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "UserDAO.findSupplierByUserId failed for userId=" + userId, e);
        }
        return null;
    }

    /* ── emailExists ─────────────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean emailExists(String email) {
        final String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "UserDAO.emailExists failed for email=" + email, e);
        }
        return false;
    }

    /* ── generateSupplierRegNo ───────────────────────────────────── */

    /**
     * {@inheritDoc}
     *
     * <p>Uses the current count of suppliers to form a sequential number.
     * Format: {@code SUP-YYYY00NN} e.g. {@code SUP-20260001}.</p>
     */
    @Override
    public String generateSupplierRegNo() {
        final String sql = "SELECT COUNT(*) FROM suppliers";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return String.format("SUP-%d%04d", Year.now().getValue(), count);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "UserDAO.generateSupplierRegNo failed", e);
        }
        return "SUP-" + Year.now().getValue() + "0001";
    }

    /* ── storeResetCode ─────────────────────────────────────────── */

    /**
     * {@inheritDoc}
     *
     * <p>Expires any previous unused codes for this user first, then
     * inserts the new code. Uses MySQL's
     * {@code DATE_ADD(NOW(), INTERVAL 15 MINUTE)} to calculate the
     * expiry directly on the server, eliminating any timezone mismatch
     * between the JVM and MySQL.</p>
     */
    @Override
    public void storeResetCode(int userId, String code, java.sql.Timestamp expiresAt) {
        // Expire any previous unused codes for this user
        final String expireOldSql
                = "UPDATE password_reset_codes SET used = 1 WHERE user_id = ? AND used = 0";
        // Use MySQL's DATE_ADD(NOW(), INTERVAL 15 MINUTE) instead of the Java-supplied
        // timestamp. This guarantees the expiry is relative to MySQL's clock,
        // eliminating any timezone mismatch between JVM and MySQL.
        final String insertSql
                = "INSERT INTO password_reset_codes (user_id, code, expires_at) "
                + "VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 15 MINUTE))";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psExpire = conn.prepareStatement(expireOldSql); PreparedStatement psInsert = conn.prepareStatement(insertSql)) {

                psExpire.setInt(1, userId);
                psExpire.executeUpdate();

                psInsert.setInt(1, userId);
                psInsert.setString(2, code);
                psInsert.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "UserDAO.storeResetCode failed for userId=" + userId, e);
        }
    }

    /* ── verifyResetCode ────────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean verifyResetCode(int userId, String code) {
        final String sql
                = "SELECT 1 FROM password_reset_codes "
                + "WHERE user_id = ? AND code = ? AND used = 0 "
                + "AND expires_at > NOW() LIMIT 1";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "UserDAO.verifyResetCode failed for userId=" + userId, e);
        }
        return false;
    }

    /* ── markResetCodeUsed ──────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public void markResetCodeUsed(int userId, String code) {
        final String sql
                = "UPDATE password_reset_codes SET used = 1 "
                + "WHERE user_id = ? AND code = ? AND used = 0";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, code);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "UserDAO.markResetCodeUsed failed for userId=" + userId, e);
        }
    }

    /* ── updatePassword ─────────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePassword(int userId, String newPasswordHash) {
        final String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "UserDAO.updatePassword failed for userId=" + userId, e);
        }
    }

    /* ── setLocked ───────────────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLocked(int userId, boolean locked) {
        final String sql = "UPDATE users SET is_locked = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, locked);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "UserDAO.setLocked failed for userId=" + userId, e);
        }
    }

    /* ── getLoginAttemptCount ────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLoginAttemptCount(int userId) {
        final String sql = "SELECT attempt_count FROM login_attempts WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("attempt_count");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "UserDAO.getLoginAttemptCount failed for userId=" + userId, e);
        }
        return 0;
    }

    /* ── incrementLoginAttempts ──────────────────────────────────── */

    /**
     * {@inheritDoc}
     *
     * <p>Uses {@code INSERT ... ON DUPLICATE KEY UPDATE} for an atomic
     * upsert operation — creates the record if it does not exist, or
     * increments the counter if it does.</p>
     */
    @Override
    public void incrementLoginAttempts(int userId) {
        final String sql = "INSERT INTO login_attempts (user_id, attempt_count) VALUES (?, 1) "
                + "ON DUPLICATE KEY UPDATE attempt_count = attempt_count + 1, "
                + "last_attempt = CURRENT_TIMESTAMP";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "UserDAO.incrementLoginAttempts failed for userId=" + userId, e);
        }
    }

    /* ── resetLoginAttempts ──────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetLoginAttempts(int userId) {
        final String sql = "UPDATE login_attempts SET attempt_count = 0 WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "UserDAO.resetLoginAttempts failed for userId=" + userId, e);
        }
    }

    /* ── findAllByRole ──────────────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public java.util.List<User> findAllByRole(User.Role role) {
        final String sql
                = "SELECT user_id, full_name, email, password_hash, role, is_locked, created_at "
                + "FROM users WHERE role = ? ORDER BY full_name";

        java.util.List<User> users = new java.util.ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "UserDAO.findAllByRole failed for role=" + role, e);
        }
        return users;
    }

    /* ── Private mappers ─────────────────────────────────────────── */

    /**
     * Maps a {@link ResultSet} row to a {@link User} bean.
     *
     * @param rs the result set positioned at the current row
     * @return a fully populated User bean
     * @throws SQLException if a database error occurs
     */
    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(User.Role.valueOf(rs.getString("role")));
        u.setLocked(rs.getBoolean("is_locked"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }

    /**
     * Maps a {@link ResultSet} row to a {@link Supplier} bean.
     *
     * @param rs the result set positioned at the current row
     * @return a fully populated Supplier bean
     * @throws SQLException if a database error occurs
     */
    private Supplier mapSupplier(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setSupplierId(rs.getInt("supplier_id"));
        s.setUserId(rs.getInt("user_id"));
        s.setCompanyName(rs.getString("company_name"));
        s.setRegistrationNo(rs.getString("registration_no"));
        s.setPhysicalAddress(rs.getString("physical_address"));
        s.setContactNumber(rs.getString("contact_number"));
        return s;
    }
}