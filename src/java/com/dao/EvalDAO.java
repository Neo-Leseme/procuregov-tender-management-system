package com.dao;

import com.model.EvaluationScore;
import java.util.List;

/**
 * EvalDAO — data access interface for evaluation score operations.
 *
 * <p>All implementations must go through this interface.
 * No JDBC code is permitted in Servlets or JSPs.</p>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public interface EvalDAO {

    /**
     * Persists a new evaluation score row. Sets the generated {@code scoreId}.
     *
     * @param score the score bean to insert
     * @return the generated score ID, or -1 on failure
     */
    int insertScore(EvaluationScore score);

    /**
     * Returns all scores submitted for a specific bid.
     *
     * @param bidId the bid's ID
     * @return list of EvaluationScore records, never null
     */
    List<EvaluationScore> findByBidId(int bidId);

    /**
     * Returns all scores submitted by a specific evaluator for a specific bid.
     * Used to check if an evaluator has already scored this bid.
     *
     * @param bidId the bid's ID
     * @param evaluatorUserId the evaluator's user ID
     * @return the existing score, or {@code null} if not yet scored
     */
    EvaluationScore findByBidAndEvaluator(int bidId, int evaluatorUserId);

    /**
     * Checks whether a specific evaluator has already submitted scores for a
     * given bid.
     *
     * @param bidId the bid's ID
     * @param evaluatorUserId the evaluator's user ID
     * @return {@code true} if a score record already exists
     */
    boolean hasEvaluatorScoredBid(int bidId, int evaluatorUserId);

    /**
     * Returns all evaluator user IDs appointed to a tender. Used to check if
     * ALL evaluators have scored ALL bids.
     *
     * @param tenderId the tender's ID
     * @return list of user IDs for appointed evaluators
     */
    List<Integer> getEvaluatorIds(int tenderId);

    /**
     * Checks if all appointed evaluators have scored all bids for a tender.
     * Triggers the automatic EVALUATED status transition when true.
     *
     * @param tenderId the tender's ID
     * @param bidCount the total number of bids for this tender
     * @return {@code true} if every evaluator has scored every bid
     */
    boolean allEvaluatorsHaveScoredAllBids(int tenderId, int bidCount);

    /**
     * Returns all scores for all bids of a tender, enriched with evaluator
     * names. Used to build the ranked leaderboard.
     *
     * @param tenderId the tender's ID
     * @return flat list of all score records for the tender
     */
    List<EvaluationScore> findAllScoresForTender(int tenderId);

    /**
     * Inserts an evaluator appointment record. Called when an officer moves a
     * tender to UNDER_EVALUATION.
     *
     * @param tenderId the tender's ID
     * @param evaluatorUserId the user to appoint as evaluator
     */
    void appointEvaluator(int tenderId, int evaluatorUserId);
}