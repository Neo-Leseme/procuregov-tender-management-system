package com.dao;

import com.model.EvaluationScore;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EvalDAOImpl — JDBC implementation of {@link EvalDAO}.
 *
 * <p>All database access for evaluation scores goes through this class.
 * SQLExceptions are caught, logged, and never exposed to the UI layer.</p>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public class EvalDAOImpl implements EvalDAO {

    private static final Logger LOGGER = Logger.getLogger(EvalDAOImpl.class.getName());

    /* ── insertScore ─────────────────────────────────────────── */
    /**
     * {@inheritDoc}
     */
    @Override
    public int insertScore(EvaluationScore score) {
        final String sql
                = "INSERT INTO evaluation_scores "
                + "(bid_id, evaluator_user_id, technical_score, price_score, timeline_score, weighted_total) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, score.getBidId());
            ps.setInt(2, score.getEvaluatorUserId());
            ps.setBigDecimal(3, score.getTechnicalScore());
            ps.setBigDecimal(4, score.getPriceScore());
            ps.setBigDecimal(5, score.getTimelineScore());
            ps.setBigDecimal(6, score.getWeightedTotal());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        score.setScoreId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "EvalDAO.insertScore failed for bidId=" + score.getBidId(), e);
        }
        return -1;
    }

    /* ── findByBidId ─────────────────────────────────────────── */
    /**
     * {@inheritDoc}
     */
    @Override
    public List<EvaluationScore> findByBidId(int bidId) {
        final String sql
                = "SELECT es.*, u.full_name AS evaluator_name "
                + "FROM evaluation_scores es "
                + "JOIN users u ON es.evaluator_user_id = u.user_id "
                + "WHERE es.bid_id = ? "
                + "ORDER BY es.scored_at ASC";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bidId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapList(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "EvalDAO.findByBidId failed for bidId=" + bidId, e);
        }
        return new ArrayList<>();
    }

    /* ── findByBidAndEvaluator ───────────────────────────────── */
    /**
     * {@inheritDoc}
     */
    @Override
    public EvaluationScore findByBidAndEvaluator(int bidId, int evaluatorUserId) {
        final String sql
                = "SELECT es.*, u.full_name AS evaluator_name "
                + "FROM evaluation_scores es "
                + "JOIN users u ON es.evaluator_user_id = u.user_id "
                + "WHERE es.bid_id = ? AND es.evaluator_user_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bidId);
            ps.setInt(2, evaluatorUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapScore(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "EvalDAO.findByBidAndEvaluator failed", e);
        }
        return null;
    }

    /* ── hasEvaluatorScoredBid ───────────────────────────────── */
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasEvaluatorScoredBid(int bidId, int evaluatorUserId) {
        final String sql
                = "SELECT 1 FROM evaluation_scores "
                + "WHERE bid_id = ? AND evaluator_user_id = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bidId);
            ps.setInt(2, evaluatorUserId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "EvalDAO.hasEvaluatorScoredBid failed", e);
        }
        return false;
    }

    /* ── getEvaluatorIds ─────────────────────────────────────── */
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getEvaluatorIds(int tenderId) {
        final String sql
                = "SELECT user_id FROM evaluators WHERE tender_id = ?";

        List<Integer> ids = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tenderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("user_id"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "EvalDAO.getEvaluatorIds failed for tenderId=" + tenderId, e);
        }
        return ids;
    }

    /* ── allEvaluatorsHaveScoredAllBids ──────────────────────── */
    /**
     * {@inheritDoc}
     *
     * <p>Logic: total scores submitted = evaluator count × bid count.
     * If they match, everyone has scored everything.</p>
     */
    @Override
    public boolean allEvaluatorsHaveScoredAllBids(int tenderId, int bidCount) {
        if (bidCount == 0) {
            return false;
        }

        final String sql
                = "SELECT COUNT(DISTINCT e.user_id) AS eval_count, "
                + "       COUNT(es.score_id)        AS score_count "
                + "FROM evaluators e "
                + "LEFT JOIN bids b ON b.tender_id = e.tender_id "
                + "LEFT JOIN evaluation_scores es "
                + "       ON es.bid_id = b.bid_id AND es.evaluator_user_id = e.user_id "
                + "WHERE e.tender_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tenderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int evalCount = rs.getInt("eval_count");
                    int scoreCount = rs.getInt("score_count");
                    if (evalCount == 0) {
                        return false;
                    }
                    return scoreCount == (evalCount * bidCount);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "EvalDAO.allEvaluatorsHaveScoredAllBids failed for tenderId=" + tenderId, e);
        }
        return false;
    }

    /* ── findAllScoresForTender ──────────────────────────────── */
    /**
     * {@inheritDoc}
     */
    @Override
    public List<EvaluationScore> findAllScoresForTender(int tenderId) {
        final String sql
                = "SELECT es.*, u.full_name AS evaluator_name "
                + "FROM evaluation_scores es "
                + "JOIN bids b ON es.bid_id = b.bid_id "
                + "JOIN users u ON es.evaluator_user_id = u.user_id "
                + "WHERE b.tender_id = ? "
                + "ORDER BY es.bid_id, es.scored_at";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tenderId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapList(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "EvalDAO.findAllScoresForTender failed for tenderId=" + tenderId, e);
        }
        return new ArrayList<>();
    }

    /* ── appointEvaluator ────────────────────────────────────── */
    /**
     * {@inheritDoc}
     */
    @Override
    public void appointEvaluator(int tenderId, int evaluatorUserId) {
        final String sql
                = "INSERT IGNORE INTO evaluators (tender_id, user_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tenderId);
            ps.setInt(2, evaluatorUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "EvalDAO.appointEvaluator failed for tenderId=" + tenderId
                    + " userId=" + evaluatorUserId, e);
        }
    }

    /* ── Private mappers ──────────────────────────────────────── */

    /**
     * Maps a {@link ResultSet} to a list of {@link EvaluationScore} beans.
     *
     * @param rs the result set positioned before the first row
     * @return a list of evaluation scores, never null
     * @throws SQLException if a database error occurs
     */
    private List<EvaluationScore> mapList(ResultSet rs) throws SQLException {
        List<EvaluationScore> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapScore(rs));
        }
        return list;
    }

    /**
     * Maps a single {@link ResultSet} row to an {@link EvaluationScore}
     * bean, including the joined evaluator name.
     *
     * @param rs the result set positioned at the current row
     * @return a fully populated EvaluationScore bean
     * @throws SQLException if a database error occurs
     */
    private EvaluationScore mapScore(ResultSet rs) throws SQLException {
        EvaluationScore s = new EvaluationScore();
        s.setScoreId(rs.getInt("score_id"));
        s.setBidId(rs.getInt("bid_id"));
        s.setEvaluatorUserId(rs.getInt("evaluator_user_id"));
        s.setTechnicalScore(rs.getBigDecimal("technical_score"));
        s.setPriceScore(rs.getBigDecimal("price_score"));
        s.setTimelineScore(rs.getBigDecimal("timeline_score"));
        s.setWeightedTotal(rs.getBigDecimal("weighted_total"));
        s.setScoredAt(rs.getTimestamp("scored_at"));
        try {
            s.setEvaluatorName(rs.getString("evaluator_name"));
        } catch (SQLException ignored) {
        }
        return s;
    }
}