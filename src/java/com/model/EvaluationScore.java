package com.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * EvaluationScore — JavaBean representing one evaluator's scores for
 * one bid.
 *
 * <p>Maps directly to the {@code evaluation_scores} table. One row per
 * evaluator per bid, enforced by the unique constraint
 * {@code uq_score_per_evaluator (bid_id, evaluator_user_id)}.</p>
 *
 * <p>Scoring model (Module 4):
 * <ul>
 *   <li><strong>Price Score</strong> = (lowestBid / thisBid) × 100
 *       — auto-calculated, weight 40%</li>
 *   <li><strong>Technical Score</strong> = evaluator enters 0–100,
 *       weight 35%</li>
 *   <li><strong>Timeline Score</strong> = (shortestDays / thisDays) × 100
 *       — auto-calculated, weight 25%</li>
 *   <li><strong>Weighted Total</strong> = (price × 0.40)
 *       + (technical × 0.35) + (timeline × 0.25)</li>
 * </ul>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public class EvaluationScore implements Serializable {

    private static final long serialVersionUID = 1L;

    private int scoreId;
    private int bidId;
    private int evaluatorUserId;
    private BigDecimal technicalScore;    // entered by evaluator, 0–100
    private BigDecimal priceScore;        // computed by EvaluationService
    private BigDecimal timelineScore;     // computed by EvaluationService
    private BigDecimal weightedTotal;     // computed: (price×0.40)+(tech×0.35)+(timeline×0.25)
    private Timestamp scoredAt;

    // Populated via JOIN when needed
    private String evaluatorName;         // full name of the evaluator

    /**
     * Default no-argument constructor.
     */
    public EvaluationScore() {
    }

    /* ── Getters & Setters ─────────────────────────────────── */

    /**
     * @return the score ID
     */
    public int getScoreId() {
        return scoreId;
    }

    /**
     * @param scoreId the score ID to set
     */
    public void setScoreId(int scoreId) {
        this.scoreId = scoreId;
    }

    /**
     * @return the bid ID being scored
     */
    public int getBidId() {
        return bidId;
    }

    /**
     * @param bidId the bid ID to set
     */
    public void setBidId(int bidId) {
        this.bidId = bidId;
    }

    /**
     * @return the user ID of the evaluator who submitted this score
     */
    public int getEvaluatorUserId() {
        return evaluatorUserId;
    }

    /**
     * @param evaluatorUserId the evaluator user ID to set
     */
    public void setEvaluatorUserId(int evaluatorUserId) {
        this.evaluatorUserId = evaluatorUserId;
    }

    /**
     * @return the manually entered technical compliance score (0–100)
     */
    public BigDecimal getTechnicalScore() {
        return technicalScore;
    }

    /**
     * @param technicalScore the technical score to set
     */
    public void setTechnicalScore(BigDecimal technicalScore) {
        this.technicalScore = technicalScore;
    }

    /**
     * @return the auto-calculated price score
     */
    public BigDecimal getPriceScore() {
        return priceScore;
    }

    /**
     * @param priceScore the price score to set
     */
    public void setPriceScore(BigDecimal priceScore) {
        this.priceScore = priceScore;
    }

    /**
     * @return the auto-calculated timeline score
     */
    public BigDecimal getTimelineScore() {
        return timelineScore;
    }

    /**
     * @param timelineScore the timeline score to set
     */
    public void setTimelineScore(BigDecimal timelineScore) {
        this.timelineScore = timelineScore;
    }

    /**
     * @return the computed weighted total (price×0.40 + technical×0.35 + timeline×0.25)
     */
    public BigDecimal getWeightedTotal() {
        return weightedTotal;
    }

    /**
     * @param weightedTotal the weighted total to set
     */
    public void setWeightedTotal(BigDecimal weightedTotal) {
        this.weightedTotal = weightedTotal;
    }

    /**
     * @return the timestamp when the score was submitted
     */
    public Timestamp getScoredAt() {
        return scoredAt;
    }

    /**
     * @param scoredAt the scored-at timestamp to set
     */
    public void setScoredAt(Timestamp scoredAt) {
        this.scoredAt = scoredAt;
    }

    /**
     * @return the evaluator's full name (populated via JOIN, may be null)
     */
    public String getEvaluatorName() {
        return evaluatorName;
    }

    /**
     * @param evaluatorName the evaluator name to set
     */
    public void setEvaluatorName(String evaluatorName) {
        this.evaluatorName = evaluatorName;
    }

    /**
     * Returns a concise string representation for logging and debugging.
     *
     * @return a string containing the score, bid, evaluator IDs and weighted total
     */
    @Override
    public String toString() {
        return "EvaluationScore{scoreId=" + scoreId
                + ", bidId=" + bidId
                + ", evaluatorUserId=" + evaluatorUserId
                + ", weightedTotal=" + weightedTotal + "}";
    }
}