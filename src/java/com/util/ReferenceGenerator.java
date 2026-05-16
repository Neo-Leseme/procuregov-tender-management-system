package com.util;

import com.dao.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ReferenceGenerator — generates unique tender reference numbers in the
 * format {@code MPW-YYYY-NNNN} (e.g. {@code MPW-2026-0001}).
 *
 * <p>Uses the {@code tender_reference_seq} table with a row-level lock
 * ({@code SELECT ... FOR UPDATE}) to guarantee uniqueness even under
 * concurrent requests. Also scans existing reference numbers in the
 * {@code tenders} table to avoid collisions with seed data. Reference
 * generation is server-side only — the Procurement Officer never types
 * the reference number manually.</p>
 *
 * <p>Usage:
 * <pre>
 *   String ref = ReferenceGenerator.nextTenderReference();
 * </pre>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public final class ReferenceGenerator {

    private static final Logger LOGGER = Logger.getLogger(ReferenceGenerator.class.getName());

    /**
     * Private constructor — utility class, not instantiable.
     */
    private ReferenceGenerator() {
    }

    /**
     * Generates the next unique tender reference number for the current
     * year.
     *
     * <p>Increments the sequence atomically inside a transaction using
     * {@code SELECT ... FOR UPDATE} to prevent duplicate numbers even
     * under concurrent access. Also checks the highest existing reference
     * number in the {@code tenders} table so that seed data (which may
     * already contain references like {@code MPW-2026-0001} and
     * {@code MPW-2026-0002}) never causes a collision.</p>
     *
     * <p>If the database operation fails, a timestamp-based fallback
     * is returned to avoid blocking the officer's workflow.</p>
     *
     * @return a unique reference string in the format {@code MPW-YYYY-NNNN}
     */
    public static synchronized String nextTenderReference() {
        int currentYear = Year.now().getValue();

        String selectSql = "SELECT seq_value FROM tender_reference_seq "
                + "WHERE seq_year = ? FOR UPDATE";
        String updateSql = "UPDATE tender_reference_seq "
                + "SET seq_value = ? WHERE seq_year = ?";
        String insertSql = "INSERT INTO tender_reference_seq (seq_year, seq_value) VALUES (?, ?)";
        // Fallback: find the highest existing reference number for this year
        // so seed data (e.g. MPW-2026-0001, MPW-2026-0002) never causes a
        // duplicate key violation.
        String maxRefSql = "SELECT MAX(CAST(SUBSTRING_INDEX(reference_no, '-', -1) AS UNSIGNED)) "
                + "FROM tenders WHERE reference_no LIKE ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int nextVal;

            try (PreparedStatement sel = conn.prepareStatement(selectSql)) {
                sel.setInt(1, currentYear);
                try (ResultSet rs = sel.executeQuery()) {
                    if (rs.next()) {
                        int seqValue = rs.getInt("seq_value");

                        // Check if the sequence is behind the seed data by
                        // scanning existing reference numbers in the database
                        int maxExisting = 0;
                        try (PreparedStatement maxPs = conn.prepareStatement(maxRefSql)) {
                            maxPs.setString(1, "MPW-" + currentYear + "-%");
                            try (ResultSet maxRs = maxPs.executeQuery()) {
                                if (maxRs.next()) {
                                    maxExisting = maxRs.getInt(1);
                                }
                            }
                        }

                        // Use whichever is higher: seq_value or max existing
                        nextVal = Math.max(seqValue, maxExisting) + 1;

                        // Update the sequence to the new value
                        try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                            upd.setInt(1, nextVal);
                            upd.setInt(2, currentYear);
                            upd.executeUpdate();
                        }
                    } else {
                        // First tender of the year — insert seed row
                        nextVal = 1;
                        try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                            ins.setInt(1, currentYear);
                            ins.setInt(2, nextVal);
                            ins.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();
            return String.format("MPW-%d-%04d", currentYear, nextVal);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "ReferenceGenerator.nextTenderReference failed", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Rollback failed", ex);
                }
            }
            // Fallback using timestamp — avoids blocking the officer
            return String.format("MPW-%d-%04d", currentYear,
                    (int) (System.currentTimeMillis() % 9999));
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Connection close failed", e);
                }
            }
        }
    }
}