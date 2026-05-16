package com.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Bid — JavaBean representing a supplier's bid on a tender.
 *
 * <p>Maps directly to the {@code bids} table. One bid per supplier per
 * tender is enforced at the Servlet and database layers (unique constraint
 * on {@code (tender_id, supplier_id)}).</p>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public class Bid implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Bid statuses — must match the database ENUM exactly.
     */
    public enum Status {
        /** Bid has been submitted and is awaiting evaluation. */
        SUBMITTED,
        /** Bid is currently being evaluated by the committee. */
        UNDER_EVALUATION,
        /** This bid was selected as the winner. */
        AWARDED,
        /** This bid was not selected. */
        NOT_AWARDED
    }

    private int bidId;
    private int tenderId;
    private int supplierId;
    private BigDecimal bidAmount;             // Maloti
    private String complianceStatement;       // max 600 chars
    private int deliveryDays;
    private String documentPath;              // server filesystem path
    private Timestamp submittedAt;
    private Status status;

    // Populated via JOIN when needed — not stored in bids table
    private String supplierName;              // company name from suppliers
    private String supplierEmail;             // email from users
    private int supplierUserId;

    // Populated by EvaluationService — not stored in bids table
    private Double finalScore;                // average of all evaluators' weighted totals
    private int rank;                         // rank in the leaderboard (1 = highest)
    private boolean hasBeenScored;            // has current evaluator already scored this bid?

    /**
     * Default no-argument constructor.
     */
    public Bid() {
    }

    /* ── Getters & Setters ─────────────────────────────────── */

    /**
     * @return the bid ID
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
     * @return the tender ID this bid belongs to
     */
    public int getTenderId() {
        return tenderId;
    }

    /**
     * @param tenderId the tender ID to set
     */
    public void setTenderId(int tenderId) {
        this.tenderId = tenderId;
    }

    /**
     * @return the supplier ID who submitted this bid
     */
    public int getSupplierId() {
        return supplierId;
    }

    /**
     * @param supplierId the supplier ID to set
     */
    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    /**
     * @return the bid amount in Maloti
     */
    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    /**
     * @param bidAmount the bid amount to set
     */
    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }

    /**
     * @return the technical compliance statement (max 600 characters)
     */
    public String getComplianceStatement() {
        return complianceStatement;
    }

    /**
     * @param s the compliance statement to set
     */
    public void setComplianceStatement(String s) {
        this.complianceStatement = s;
    }

    /**
     * @return the proposed delivery timeline in days
     */
    public int getDeliveryDays() {
        return deliveryDays;
    }

    /**
     * @param deliveryDays the delivery days to set
     */
    public void setDeliveryDays(int deliveryDays) {
        this.deliveryDays = deliveryDays;
    }

    /**
     * @return the server filesystem path of the uploaded document
     */
    public String getDocumentPath() {
        return documentPath;
    }

    /**
     * @param documentPath the document path to set
     */
    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    /**
     * @return the timestamp when the bid was submitted
     */
    public Timestamp getSubmittedAt() {
        return submittedAt;
    }

    /**
     * @param submittedAt the submission timestamp to set
     */
    public void setSubmittedAt(Timestamp submittedAt) {
        this.submittedAt = submittedAt;
    }

    /**
     * @return the current status of this bid
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return the supplier's company name (populated via JOIN, may be null)
     */
    public String getSupplierName() {
        return supplierName;
    }

    /**
     * @param supplierName the supplier name to set
     */
    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    /**
     * @return the supplier's email address (populated via JOIN, may be null)
     */
    public String getSupplierEmail() {
        return supplierEmail;
    }

    /**
     * @param supplierEmail the supplier email to set
     */
    public void setSupplierEmail(String supplierEmail) {
        this.supplierEmail = supplierEmail;
    }

    /**
     * @return the supplier's user ID (populated via JOIN)
     */
    public int getSupplierUserId() {
        return supplierUserId;
    }

    /**
     * @param supplierUserId the supplier user ID to set
     */
    public void setSupplierUserId(int supplierUserId) {
        this.supplierUserId = supplierUserId;
    }

    /**
     * @return the final averaged score across all evaluators, or null
     *         if not yet calculated
     */
    public Double getFinalScore() {
        return finalScore;
    }

    /**
     * @param finalScore the final score to set
     */
    public void setFinalScore(Double finalScore) {
        this.finalScore = finalScore;
    }

    /**
     * @return the rank position in the leaderboard (1 = highest)
     */
    public int getRank() {
        return rank;
    }

    /**
     * @param rank the rank to set
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * @return {@code true} if the current evaluator has already scored this bid
     */
    public boolean isHasBeenScored() {
        return hasBeenScored;
    }

    /**
     * @param hasBeenScored whether this bid has been scored by the current evaluator
     */
    public void setHasBeenScored(boolean hasBeenScored) {
        this.hasBeenScored = hasBeenScored;
    }

    /**
     * Returns a concise string representation for logging and debugging.
     *
     * @return a string containing the bid, tender, supplier IDs and amount
     */
    @Override
    public String toString() {
        return "Bid{bidId=" + bidId + ", tenderId=" + tenderId
                + ", supplierId=" + supplierId + ", amount=" + bidAmount + "}";
    }
}