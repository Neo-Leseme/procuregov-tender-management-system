package com.dao;

import com.model.AwardNotice;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AwardDAOImpl — JDBC implementation of {@link AwardDAO}.
 *
 * <p>All database access for award notices goes through this class.
 * SQLExceptions are caught, logged, and never exposed to the UI layer.</p>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public class AwardDAOImpl implements AwardDAO {

    private static final Logger LOGGER = Logger.getLogger(AwardDAOImpl.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public int insertAwardNotice(AwardNotice notice) {
        final String sql =
            "INSERT INTO award_notices "
          + "(tender_id, winning_bid_id, awarded_by, awarded_value, justification) "
          + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, notice.getTenderId());
            ps.setInt(2, notice.getWinningBidId());
            ps.setInt(3, notice.getAwardedBy());
            ps.setBigDecimal(4, notice.getAwardedValue());
            ps.setString(5, notice.getJustification());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        notice.setAwardId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                "AwardDAO.insertAwardNotice failed for tenderId=" + notice.getTenderId(), e);
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Joins tenders, bids, suppliers, and users to populate display
     * fields such as the winning supplier name, officer name, and tender
     * reference number.</p>
     */
    @Override
    public AwardNotice findByTenderId(int tenderId) {
        final String sql =
            "SELECT an.*, "
          + "t.title AS tender_title, t.reference_no AS tender_reference_no, "
          + "s.company_name AS winning_supplier_name, "
          + "u.full_name AS officer_name "
          + "FROM award_notices an "
          + "JOIN tenders t  ON an.tender_id      = t.tender_id "
          + "JOIN bids b     ON an.winning_bid_id = b.bid_id "
          + "JOIN suppliers s ON b.supplier_id    = s.supplier_id "
          + "JOIN users u    ON an.awarded_by     = u.user_id "
          + "WHERE an.tender_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tenderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AwardNotice an = new AwardNotice();
                    an.setAwardId(rs.getInt("award_id"));
                    an.setTenderId(rs.getInt("tender_id"));
                    an.setWinningBidId(rs.getInt("winning_bid_id"));
                    an.setAwardedBy(rs.getInt("awarded_by"));
                    an.setAwardedValue(rs.getBigDecimal("awarded_value"));
                    an.setJustification(rs.getString("justification"));
                    an.setAwardDate(rs.getTimestamp("award_date"));
                    an.setTenderTitle(rs.getString("tender_title"));
                    an.setTenderReferenceNo(rs.getString("tender_reference_no"));
                    an.setWinningSupplierName(rs.getString("winning_supplier_name"));
                    an.setOfficerName(rs.getString("officer_name"));
                    return an;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                "AwardDAO.findByTenderId failed for tenderId=" + tenderId, e);
        }
        return null;
    }
}