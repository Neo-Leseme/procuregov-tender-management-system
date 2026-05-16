package com.dao;

import com.model.Bid;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BidDAOImpl — full JDBC implementation of {@link BidDAO}.
 *
 * <p>All JDBC code for bids lives here — never in Servlets or JSPs.
 * SQLExceptions are caught, logged, and never exposed to the UI layer.</p>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public class BidDAOImpl implements BidDAO {

    private static final Logger LOGGER = Logger.getLogger(BidDAOImpl.class.getName());

    /* ── insertBid ───────────────────────────────────────────── */
    /**
     * {@inheritDoc}
     */
    @Override
    public int insertBid(Bid bid) {
        final String sql
                = "INSERT INTO bids (tender_id, supplier_id, bid_amount, "
                + "compliance_statement, delivery_days, document_path, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'SUBMITTED')";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, bid.getTenderId());
            ps.setInt(2, bid.getSupplierId());
            ps.setBigDecimal(3, bid.getBidAmount());
            ps.setString(4, bid.getComplianceStatement());
            ps.setInt(5, bid.getDeliveryDays());
            ps.setString(6, bid.getDocumentPath());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        bid.setBidId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "BidDAO.insertBid failed for tenderId="
                    + bid.getTenderId() + " supplierId=" + bid.getSupplierId(), e);
        }
        return -1;
    }

    /* ── findByTenderId ──────────────────────────────────────── */
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Bid> findByTenderId(int tenderId) {
        final String sql
                = "SELECT b.*, s.company_name AS supplier_name, "
                + "u.email AS supplier_email, u.user_id AS supplier_user_id "
                + "FROM bids b "
                + "JOIN suppliers s ON b.supplier_id = s.supplier_id "
                + "JOIN users u ON s.user_id = u.user_id "
                + "WHERE b.tender_id = ? ORDER BY b.submitted_at DESC";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenderId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapList(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "BidDAO.findByTenderId failed for tenderId=" + tenderId, e);
        }
        return new ArrayList<>();
    }

    /* ── findById ────────────────────────────────────────────── */
    /**
     * {@inheritDoc}
     */
    @Override
    public Bid findById(int bidId) {
        final String sql
                = "SELECT b.*, s.company_name AS supplier_name, "
                + "u.email AS supplier_email, u.user_id AS supplier_user_id "
                + "FROM bids b "
                + "JOIN suppliers s ON b.supplier_id = s.supplier_id "
                + "JOIN users u ON s.user_id = u.user_id "
                + "WHERE b.bid_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bidId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBid(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "BidDAO.findById failed for bidId=" + bidId, e);
        }
        return null;
    }

    /* ── findBySupplier ──────────────────────────────────────── */
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Bid> findBySupplierId(int supplierId) {
        final String sql
                = "SELECT b.*, s.company_name AS supplier_name, "
                + "u.email AS supplier_email, u.user_id AS supplier_user_id "
                + "FROM bids b "
                + "JOIN suppliers s ON b.supplier_id = s.supplier_id "
                + "JOIN users u ON s.user_id = u.user_id "
                + "WHERE b.supplier_id = ? ORDER BY b.submitted_at DESC";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapList(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "BidDAO.findBySupplierId failed for supplierId=" + supplierId, e);
        }
        return new ArrayList<>();
    }

    /* ── hasSupplierBidOnTender ──────────────────────────────── */
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSupplierBidOnTender(int supplierId, int tenderId) {
        final String sql
                = "SELECT 1 FROM bids WHERE supplier_id = ? AND tender_id = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            ps.setInt(2, tenderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "BidDAO.hasSupplierBidOnTender failed", e);
        }
        return false;
    }

    /* ── findBySupplierAndTender ─────────────────────────────── */
    /**
     * {@inheritDoc}
     */
    @Override
    public Bid findBySupplierAndTender(int supplierId, int tenderId) {
        final String sql
                = "SELECT b.*, s.company_name AS supplier_name, "
                + "u.email AS supplier_email, u.user_id AS supplier_user_id "
                + "FROM bids b "
                + "JOIN suppliers s ON b.supplier_id = s.supplier_id "
                + "JOIN users u ON s.user_id = u.user_id "
                + "WHERE b.supplier_id = ? AND b.tender_id = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            ps.setInt(2, tenderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBid(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "BidDAO.findBySupplierAndTender failed", e);
        }
        return null;
    }

    /* ── updateDocumentPath ──────────────────────────────────── */
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateDocumentPath(int bidId, String path) {
        final String sql = "UPDATE bids SET document_path = ? WHERE bid_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, path);
            ps.setInt(2, bidId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "BidDAO.updateDocumentPath failed for bidId=" + bidId, e);
        }
        return false;
    }

    /* ── updateBidStatusesOnAward ────────────────────────────── */
    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBidStatusesOnAward(int tenderId, int winningBidId) {
        final String sqlWin = "UPDATE bids SET status='AWARDED'     WHERE bid_id = ?";
        final String sqlLose = "UPDATE bids SET status='NOT_AWARDED' WHERE tender_id = ? AND bid_id != ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psWin = conn.prepareStatement(sqlWin); PreparedStatement psLose = conn.prepareStatement(sqlLose)) {

                psWin.setInt(1, winningBidId);
                psWin.executeUpdate();

                psLose.setInt(1, tenderId);
                psLose.setInt(2, winningBidId);
                psLose.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "BidDAO.updateBidStatusesOnAward failed for tenderId=" + tenderId, e);
        }
    }

    /* ── Private mappers ─────────────────────────────────────── */

    /**
     * Maps a {@link ResultSet} to a list of {@link Bid} beans.
     *
     * @param rs the result set positioned before the first row
     * @return a list of bids, never null
     * @throws SQLException if a database error occurs
     */
    private List<Bid> mapList(ResultSet rs) throws SQLException {
        List<Bid> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapBid(rs));
        }
        return list;
    }

    /**
     * Maps a single {@link ResultSet} row to a {@link Bid} bean,
     * including joined supplier and user fields.
     *
     * @param rs the result set positioned at the current row
     * @return a fully populated Bid bean
     * @throws SQLException if a database error occurs
     */
    private Bid mapBid(ResultSet rs) throws SQLException {
        Bid b = new Bid();
        b.setBidId(rs.getInt("bid_id"));
        b.setTenderId(rs.getInt("tender_id"));
        b.setSupplierId(rs.getInt("supplier_id"));
        b.setBidAmount(rs.getBigDecimal("bid_amount"));
        b.setComplianceStatement(rs.getString("compliance_statement"));
        b.setDeliveryDays(rs.getInt("delivery_days"));
        b.setDocumentPath(rs.getString("document_path"));
        b.setSubmittedAt(rs.getTimestamp("submitted_at"));
        b.setStatus(Bid.Status.valueOf(rs.getString("status")));
        try {
            b.setSupplierName(rs.getString("supplier_name"));
        } catch (SQLException ignored) {
        }
        try {
            b.setSupplierEmail(rs.getString("supplier_email"));
        } catch (SQLException ignored) {
        }
        try {
            b.setSupplierUserId(rs.getInt("supplier_user_id"));
        } catch (SQLException ignored) {
        }
        return b;
    }
}