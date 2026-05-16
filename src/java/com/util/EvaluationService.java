package com.util;

import com.model.Bid;
import com.model.EvaluationScore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.logging.Logger;

/**
 * EvaluationService — all bid scoring calculations live here.
 *
 * <p>This is a dedicated service class as required by Module 4. No score
 * calculations are performed in Servlets or JSPs.</p>
 *
 * <p>Scoring model:
 * <pre>
 *   Price Score    = (lowestBidAmount / thisBidAmount) × 100   [weight: 40%]
 *   Technical Score = evaluator-entered value 0–100             [weight: 35%]
 *   Timeline Score  = (shortestDays / thisBidDays) × 100        [weight: 25%]
 *
 *   Weighted Total  = (priceScore × 0.40)
 *                   + (technicalScore × 0.35)
 *                   + (timelineScore × 0.25)
 *
 *   Final Score     = average of all evaluators' Weighted Totals for this bid
 * </pre>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public final class EvaluationService {

    private static final Logger LOGGER = Logger.getLogger(EvaluationService.class.getName());

    private static final BigDecimal WEIGHT_PRICE      = new BigDecimal("0.40");
    private static final BigDecimal WEIGHT_TECHNICAL  = new BigDecimal("0.35");
    private static final BigDecimal WEIGHT_TIMELINE   = new BigDecimal("0.25");
    private static final int SCALE = 2;
    private static final RoundingMode ROUND = RoundingMode.HALF_UP;

    /**
     * Private constructor — utility class, not instantiable.
     */
    private EvaluationService() {
    }

    /**
     * Calculates the Price Score for a single bid.
     *
     * <p>Formula: {@code (lowestBidAmount / thisBidAmount) × 100}</p>
     *
     * <p>The lowest bidder always gets 100. Higher bids score proportionally
     * lower. The evaluator does NOT enter this — it is computed
     * automatically.</p>
     *
     * @param lowestBidAmount the lowest bid amount among all bids for this tender
     * @param thisBidAmount   the bid amount for this specific bid
     * @return price score rounded to 2 decimal places, or 0 if inputs are invalid
     */
    public static BigDecimal calculatePriceScore(BigDecimal lowestBidAmount,
            BigDecimal thisBidAmount) {
        if (lowestBidAmount == null || thisBidAmount == null
                || thisBidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            LOGGER.warning("calculatePriceScore: invalid input — returning 0");
            return BigDecimal.ZERO;
        }
        return lowestBidAmount
                .multiply(new BigDecimal("100"))
                .divide(thisBidAmount, SCALE, ROUND);
    }

    /**
     * Calculates the Timeline Score for a single bid.
     *
     * <p>Formula: {@code (shortestDeliveryDays / thisBidDays) × 100}</p>
     *
     * <p>The fastest bidder always gets 100. Slower bids score proportionally
     * lower. The evaluator does NOT enter this — it is computed
     * automatically.</p>
     *
     * @param shortestDays the shortest delivery timeline among all bids for this tender
     * @param thisBidDays  the delivery timeline for this specific bid
     * @return timeline score rounded to 2 decimal places, or 0 if inputs are invalid
     */
    public static BigDecimal calculateTimelineScore(int shortestDays, int thisBidDays) {
        if (shortestDays <= 0 || thisBidDays <= 0) {
            LOGGER.warning("calculateTimelineScore: invalid input — returning 0");
            return BigDecimal.ZERO;
        }
        return new BigDecimal(shortestDays)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(thisBidDays), SCALE, ROUND);
    }

    /**
     * Calculates the Weighted Total Score for a bid from one evaluator.
     *
     * <p>Formula:
     * {@code (priceScore × 0.40) + (technicalScore × 0.35) + (timelineScore × 0.25)}</p>
     *
     * @param priceScore     computed price score (0–100)
     * @param technicalScore evaluator-entered technical compliance score (0–100)
     * @param timelineScore  computed timeline score (0–100)
     * @return weighted total rounded to 2 decimal places
     */
    public static BigDecimal calculateWeightedTotal(BigDecimal priceScore,
            BigDecimal technicalScore,
            BigDecimal timelineScore) {
        if (priceScore == null)     priceScore     = BigDecimal.ZERO;
        if (technicalScore == null) technicalScore = BigDecimal.ZERO;
        if (timelineScore == null)  timelineScore  = BigDecimal.ZERO;

        BigDecimal weighted = priceScore.multiply(WEIGHT_PRICE)
                .add(technicalScore.multiply(WEIGHT_TECHNICAL))
                .add(timelineScore.multiply(WEIGHT_TIMELINE));

        return weighted.setScale(SCALE, ROUND);
    }

    /**
     * Calculates the Final Score for a bid by averaging all evaluators'
     * weighted totals.
     *
     * <p>If only one evaluator has scored, the final score equals that
     * evaluator's weighted total. Multiple evaluators' scores are averaged.</p>
     *
     * @param scores list of all {@link EvaluationScore} records for this bid
     * @return averaged final score rounded to 2 decimal places, or 0 if list is empty
     */
    public static BigDecimal calculateFinalScore(List<EvaluationScore> scores) {
        if (scores == null || scores.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (EvaluationScore s : scores) {
            BigDecimal wt = s.getWeightedTotal();
            if (wt != null) {
                sum = sum.add(wt);
            }
        }

        return sum.divide(new BigDecimal(scores.size()), SCALE, ROUND);
    }

    /**
     * Builds a fully computed {@link EvaluationScore} bean ready for
     * persistence.
     *
     * <p>Computes price score and timeline score automatically from the list
     * of all bids, then computes the weighted total. The evaluator only
     * provides the technical score.</p>
     *
     * @param targetBid      the bid being scored
     * @param allBids        all bids for this tender (needed to find min values)
     * @param technicalScore the evaluator's manually entered technical score (0–100)
     * @param evaluatorId    the evaluator's user ID
     * @return a fully populated EvaluationScore ready to be inserted
     */
    public static EvaluationScore buildScore(Bid targetBid,
            List<Bid> allBids,
            BigDecimal technicalScore,
            int evaluatorId) {
        BigDecimal lowestAmount = allBids.stream()
                .map(Bid::getBidAmount)
                .filter(a -> a != null)
                .min(BigDecimal::compareTo)
                .orElse(targetBid.getBidAmount());

        int shortestDays = allBids.stream()
                .mapToInt(Bid::getDeliveryDays)
                .filter(d -> d > 0)
                .min()
                .orElse(targetBid.getDeliveryDays());

        BigDecimal priceScore    = calculatePriceScore(lowestAmount, targetBid.getBidAmount());
        BigDecimal timelineScore = calculateTimelineScore(shortestDays, targetBid.getDeliveryDays());
        BigDecimal weightedTotal = calculateWeightedTotal(priceScore, technicalScore, timelineScore);

        EvaluationScore score = new EvaluationScore();
        score.setBidId(targetBid.getBidId());
        score.setEvaluatorUserId(evaluatorId);
        score.setTechnicalScore(technicalScore.setScale(SCALE, ROUND));
        score.setPriceScore(priceScore);
        score.setTimelineScore(timelineScore);
        score.setWeightedTotal(weightedTotal);

        return score;
    }
}