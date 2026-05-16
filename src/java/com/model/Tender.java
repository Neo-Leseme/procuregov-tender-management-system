package com.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Tender — JavaBean representing a government tender in ProcureGov.
 *
 * <p>Maps directly to the {@code tenders} table. Status transitions are
 * enforced at the Servlet layer — this bean is a pure data carrier.</p>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public class Tender implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Tender lifecycle statuses — must match the database ENUM exactly.
     */
    public enum Status {
        /** Tender created but not yet visible to suppliers. */
        DRAFT,
        /** Tender published and accepting bids. */
        OPEN,
        /** Bidding deadline has passed. */
        CLOSED,
        /** Evaluation committee is scoring bids. */
        UNDER_EVALUATION,
        /** All evaluators have submitted scores. */
        EVALUATED,
        /** Contract has been awarded to the winning supplier. */
        AWARDED
    }

    /**
     * Tender categories — must match the database ENUM exactly.
     */
    public enum Category {
        /** Building and structural works. */
        CONSTRUCTION,
        /** Road infrastructure projects. */
        ROADS,
        /** Electrical systems and installations. */
        ELECTRICAL,
        /** Water and plumbing systems. */
        PLUMBING,
        /** General procurement and services. */
        GENERAL_SERVICES;

        /**
         * Returns a human-readable display label for this category.
         *
         * @return the label string (e.g. "Construction")
         */
        public String getLabel() {
            switch (this) {
                case CONSTRUCTION:      return "Construction";
                case ROADS:             return "Roads";
                case ELECTRICAL:        return "Electrical";
                case PLUMBING:          return "Plumbing";
                case GENERAL_SERVICES:  return "General Services";
                default:                return name();
            }
        }
    }

    private int tenderId;
    private String referenceNo;          // auto-generated MPW-YYYY-NNNN
    private String title;
    private Category category;
    private String description;
    private BigDecimal estimatedValue;   // Maloti
    private Timestamp closingDatetime;
    private Status status;
    private String noticeFilePath;       // server filesystem path, not exposed to browser
    private int createdBy;               // FK to users (Procurement Officer)
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Populated via JOIN when needed — not stored in tenders table
    private String createdByName;        // officer's full name
    private int bidCount;                // number of bids submitted

    /**
     * Default no-argument constructor.
     */
    public Tender() {
    }

    /* ── Getters & Setters ─────────────────────────────────── */

    /**
     * @return the tender ID
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
     * @return the system-generated reference number (e.g. MPW-2026-0001)
     */
    public String getReferenceNo() {
        return referenceNo;
    }

    /**
     * @param referenceNo the reference number to set
     */
    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    /**
     * @return the tender title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the tender category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(Category category) {
        this.category = category;
    }

    /**
     * @return the tender description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the estimated contract value in Maloti
     */
    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    /**
     * @param estimatedValue the estimated value to set
     */
    public void setEstimatedValue(BigDecimal estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    /**
     * @return the bid submission deadline
     */
    public Timestamp getClosingDatetime() {
        return closingDatetime;
    }

    /**
     * @param closingDatetime the closing datetime to set
     */
    public void setClosingDatetime(Timestamp closingDatetime) {
        this.closingDatetime = closingDatetime;
    }

    /**
     * @return the current lifecycle status
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
     * @return the server filesystem path of the uploaded notice PDF
     */
    public String getNoticeFilePath() {
        return noticeFilePath;
    }

    /**
     * @param noticeFilePath the notice file path to set
     */
    public void setNoticeFilePath(String noticeFilePath) {
        this.noticeFilePath = noticeFilePath;
    }

    /**
     * @return the user ID of the officer who created this tender
     */
    public int getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the creator's user ID to set
     */
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the creation timestamp
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return the last-updated timestamp
     */
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @param updatedAt the updated timestamp to set
     */
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * @return the full name of the officer who created this tender
     *         (populated via JOIN, may be null)
     */
    public String getCreatedByName() {
        return createdByName;
    }

    /**
     * @param createdByName the creator's name to set
     */
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    /**
     * @return the number of bids submitted for this tender
     */
    public int getBidCount() {
        return bidCount;
    }

    /**
     * @param bidCount the bid count to set
     */
    public void setBidCount(int bidCount) {
        this.bidCount = bidCount;
    }

    /* ── Convenience helpers ────────────────────────────────── */

    /**
     * Returns {@code true} if this tender can still be edited.
     * Only tenders in {@code DRAFT} status are editable.
     *
     * @return {@code true} if status is DRAFT
     */
    public boolean isEditable() {
        return Status.DRAFT.equals(status);
    }

    /**
     * Returns {@code true} if suppliers can currently submit bids.
     *
     * @return {@code true} if status is OPEN
     */
    public boolean isOpen() {
        return Status.OPEN.equals(status);
    }

    /**
     * Returns {@code true} if the tender has been awarded.
     *
     * @return {@code true} if status is AWARDED
     */
    public boolean isAwarded() {
        return Status.AWARDED.equals(status);
    }

    /**
     * Returns a CSS badge class string for the current status.
     * Used in JSPs via EL: {@code ${tender.statusBadgeClass}}.
     *
     * @return the CSS class name (e.g. "badge-open")
     */
    public String getStatusBadgeClass() {
        if (status == null) {
            return "badge-secondary";
        }
        switch (status) {
            case DRAFT:             return "badge-draft";
            case OPEN:              return "badge-open";
            case CLOSED:            return "badge-closed";
            case UNDER_EVALUATION:  return "badge-eval";
            case EVALUATED:         return "badge-evaluated";
            case AWARDED:           return "badge-awarded";
            default:                return "badge-secondary";
        }
    }

    /**
     * Returns a concise string representation for logging and debugging.
     *
     * @return a string containing the tender ID, reference number, and status
     */
    @Override
    public String toString() {
        return "Tender{tenderId=" + tenderId
                + ", referenceNo='" + referenceNo + "'"
                + ", status=" + status + "}";
    }
}