package com.dao;

import com.model.Tender;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TenderDAOImpl — JDBC implementation of {@link TenderDAO}.
 *
 * <p>All database access for tenders goes through this class.
 * SQLExceptions are caught, logged, and never exposed to the UI layer.</p>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public class TenderDAOImpl implements TenderDAO {

    private static final Logger LOGGER = Logger.getLogger(TenderDAOImpl.class.getName());

    /* ── insertTender ────────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public int insertTender(Tender tender) {
        final String sql =
            "INSERT INTO tenders (reference_no, title, category, description, "
          + "estimated_value, closing_datetime, status, notice_file_path, created_by) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, tender.getReferenceNo());
            ps.setString(2, tender.getTitle());
            ps.setString(3, tender.getCategory().name());
            ps.setString(4, tender.getDescription());
            ps.setBigDecimal(5, tender.getEstimatedValue());
            ps.setTimestamp(6, tender.getClosingDatetime());
            ps.setString(7, Tender.Status.DRAFT.name());
            ps.setString(8, tender.getNoticeFilePath());
            ps.setInt(9, tender.getCreatedBy());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        tender.setTenderId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "TenderDAO.insertTender failed", e);
        }
        return -1;
    }

    /* ── updateTender ────────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateTender(Tender tender) {
        final String sql =
            "UPDATE tenders SET title=?, category=?, description=?, "
          + "estimated_value=?, closing_datetime=? "
          + "WHERE tender_id=? AND status='DRAFT'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tender.getTitle());
            ps.setString(2, tender.getCategory().name());
            ps.setString(3, tender.getDescription());
            ps.setBigDecimal(4, tender.getEstimatedValue());
            ps.setTimestamp(5, tender.getClosingDatetime());
            ps.setInt(6, tender.getTenderId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "TenderDAO.updateTender failed for tenderId=" + tender.getTenderId(), e);
        }
        return false;
    }

    /* ── findById ────────────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public Tender findById(int tenderId) {
        final String sql =
            "SELECT t.*, u.full_name AS created_by_name, "
          + "(SELECT COUNT(*) FROM bids b WHERE b.tender_id = t.tender_id) AS bid_count "
          + "FROM tenders t LEFT JOIN users u ON t.created_by = u.user_id "
          + "WHERE t.tender_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tenderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapTender(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "TenderDAO.findById failed for tenderId=" + tenderId, e);
        }
        return null;
    }

    /* ── findByReferenceNo ───────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public Tender findByReferenceNo(String referenceNo) {
        final String sql =
            "SELECT t.*, u.full_name AS created_by_name, "
          + "(SELECT COUNT(*) FROM bids b WHERE b.tender_id = t.tender_id) AS bid_count "
          + "FROM tenders t LEFT JOIN users u ON t.created_by = u.user_id "
          + "WHERE t.reference_no = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, referenceNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapTender(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "TenderDAO.findByReferenceNo failed", e);
        }
        return null;
    }

    /* ── findAll ─────────────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Tender> findAll() {
        final String sql =
            "SELECT t.*, u.full_name AS created_by_name, "
          + "(SELECT COUNT(*) FROM bids b WHERE b.tender_id = t.tender_id) AS bid_count "
          + "FROM tenders t LEFT JOIN users u ON t.created_by = u.user_id "
          + "ORDER BY t.created_at DESC";

        return executeList(sql);
    }

    /* ── findByStatus ────────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Tender> findByStatus(Tender.Status status) {
        final String sql =
            "SELECT t.*, u.full_name AS created_by_name, "
          + "(SELECT COUNT(*) FROM bids b WHERE b.tender_id = t.tender_id) AS bid_count "
          + "FROM tenders t LEFT JOIN users u ON t.created_by = u.user_id "
          + "WHERE t.status = ? ORDER BY t.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                return mapList(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "TenderDAO.findByStatus failed", e);
        }
        return new ArrayList<>();
    }

    /* ── findByCategory ──────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Tender> findByCategory(Tender.Category category) {
        final String sql =
            "SELECT t.*, u.full_name AS created_by_name, "
          + "(SELECT COUNT(*) FROM bids b WHERE b.tender_id = t.tender_id) AS bid_count "
          + "FROM tenders t LEFT JOIN users u ON t.created_by = u.user_id "
          + "WHERE t.category = ? ORDER BY t.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.name());
            try (ResultSet rs = ps.executeQuery()) {
                return mapList(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "TenderDAO.findByCategory failed", e);
        }
        return new ArrayList<>();
    }

    /* ── findByStatusAndCategory ─────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Tender> findByStatusAndCategory(Tender.Status status, Tender.Category category) {
        final String sql =
            "SELECT t.*, u.full_name AS created_by_name, "
          + "(SELECT COUNT(*) FROM bids b WHERE b.tender_id = t.tender_id) AS bid_count "
          + "FROM tenders t LEFT JOIN users u ON t.created_by = u.user_id "
          + "WHERE t.status = ? AND t.category = ? ORDER BY t.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setString(2, category.name());
            try (ResultSet rs = ps.executeQuery()) {
                return mapList(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "TenderDAO.findByStatusAndCategory failed", e);
        }
        return new ArrayList<>();
    }

    /* ── findOpenTenders ─────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Tender> findOpenTenders() {
        final String sql =
            "SELECT t.*, u.full_name AS created_by_name, "
          + "(SELECT COUNT(*) FROM bids b WHERE b.tender_id = t.tender_id) AS bid_count "
          + "FROM tenders t LEFT JOIN users u ON t.created_by = u.user_id "
          + "WHERE t.status = 'OPEN' ORDER BY t.closing_datetime ASC";

        return executeList(sql);
    }

    /* ── updateStatus ────────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateStatus(int tenderId, Tender.Status newStatus) {
        final String sql = "UPDATE tenders SET status = ? WHERE tender_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus.name());
            ps.setInt(2, tenderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                "TenderDAO.updateStatus failed for tenderId=" + tenderId + " status=" + newStatus, e);
        }
        return false;
    }

    /* ── updateNoticeFilePath ────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateNoticeFilePath(int tenderId, String filePath) {
        final String sql = "UPDATE tenders SET notice_file_path = ? WHERE tender_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, filePath);
            ps.setInt(2, tenderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "TenderDAO.updateNoticeFilePath failed for tenderId=" + tenderId, e);
        }
        return false;
    }

    /* ── closeExpiredTenders ─────────────────────────────────── */

    /**
     * {@inheritDoc}
     *
     * <p>Uses {@code NOW()} which runs in the timezone configured via
     * the JDBC {@code serverTimezone} parameter in {@code context.xml}.
     * A 1-minute grace period ({@code DATE_SUB(NOW(), INTERVAL 1 MINUTE)})
     * prevents a freshly published tender from being auto-closed in the
     * same request cycle.</p>
     */
    @Override
    public int closeExpiredTenders() {
        // Uses NOW() which runs in Africa/Maseru timezone via the JDBC serverTimezone
        // A 1-minute grace period prevents a tender published just-in-time from
        // being closed before any supplier can see it.
        final String sql =
            "UPDATE tenders SET status = 'CLOSED' "
          + "WHERE status = 'OPEN' AND closing_datetime <= DATE_SUB(NOW(), INTERVAL 1 MINUTE)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int count = ps.executeUpdate();
            if (count > 0) {
                LOGGER.info("Auto-closed " + count + " expired tender(s).");
            }
            return count;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "TenderDAO.closeExpiredTenders failed", e);
        }
        return 0;
    }

    /* ── findByOfficer ───────────────────────────────────────── */

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Tender> findByOfficer(int officerUserId) {
        final String sql =
            "SELECT t.*, u.full_name AS created_by_name, "
          + "(SELECT COUNT(*) FROM bids b WHERE b.tender_id = t.tender_id) AS bid_count "
          + "FROM tenders t LEFT JOIN users u ON t.created_by = u.user_id "
          + "WHERE t.created_by = ? ORDER BY t.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, officerUserId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapList(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "TenderDAO.findByOfficer failed for userId=" + officerUserId, e);
        }
        return new ArrayList<>();
    }

    /* ── Private helpers ─────────────────────────────────────── */

    /**
     * Executes a no-parameter SQL query and returns a list of
     * {@link Tender} beans.
     *
     * @param sql the SQL query to execute
     * @return list of tenders, never null
     */
    private List<Tender> executeList(String sql) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return mapList(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "TenderDAO list query failed: " + sql, e);
        }
        return new ArrayList<>();
    }

    /**
     * Maps a {@link ResultSet} to a list of {@link Tender} beans.
     *
     * @param rs the result set positioned before the first row
     * @return a list of tenders, never null
     * @throws SQLException if a database error occurs
     */
    private List<Tender> mapList(ResultSet rs) throws SQLException {
        List<Tender> list = new ArrayList<>();
        while (rs.next()) list.add(mapTender(rs));
        return list;
    }

    /**
     * Maps a single {@link ResultSet} row to a {@link Tender} bean,
     * including joined fields such as the creator's name and bid count.
     *
     * @param rs the result set positioned at the current row
     * @return a fully populated Tender bean
     * @throws SQLException if a database error occurs
     */
    private Tender mapTender(ResultSet rs) throws SQLException {
        Tender t = new Tender();
        t.setTenderId(rs.getInt("tender_id"));
        t.setReferenceNo(rs.getString("reference_no"));
        t.setTitle(rs.getString("title"));
        t.setCategory(Tender.Category.valueOf(rs.getString("category")));
        t.setDescription(rs.getString("description"));
        t.setEstimatedValue(rs.getBigDecimal("estimated_value"));
        t.setClosingDatetime(rs.getTimestamp("closing_datetime"));
        t.setStatus(Tender.Status.valueOf(rs.getString("status")));
        t.setNoticeFilePath(rs.getString("notice_file_path"));
        t.setCreatedBy(rs.getInt("created_by"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        t.setUpdatedAt(rs.getTimestamp("updated_at"));

        // JOIN fields — may be null if query didn't include them
        try { t.setCreatedByName(rs.getString("created_by_name")); } catch (SQLException ignored) {}
        try { t.setBidCount(rs.getInt("bid_count")); } catch (SQLException ignored) {}

        return t;
    }
}