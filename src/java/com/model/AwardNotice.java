package com.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * AwardNotice — JavaBean representing a formal contract award.
 *
 * <p>Maps directly to the {@code award_notices} table. One row per
 * tender — enforced by the unique constraint on {@code tender_id}.</p>
 *
 * <p>Visible to all suppliers who bid on that tender after the award
 * is published.</p>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public class AwardNotice implements Serializable {

    private static final long serialVersionUID = 1L;

    private int awardId;
    private int tenderId;
    private int winningBidId;
    private int awardedBy;              // FK to users (Procurement Officer)
    private BigDecimal awardedValue;    // Maloti
    private String justification;
    private Timestamp awardDate;

    // Populated via JOIN when needed
    private String tenderTitle;
    private String tenderReferenceNo;
    private String winningSupplierName;
    private String officerName;

    /**
     * Default no-argument constructor.
     */
    public AwardNotice() {
    }

    /* ── Getters & Setters ─────────────────────────────────── */

    /**
     * @return the award ID
     */
    public int getAwardId() {
        return awardId;
    }

    /**
     * @param awardId the award ID to set
     */
    public void setAwardId(int awardId) {
        this.awardId = awardId;
    }

    /**
     * @return the tender ID this award belongs to
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
     * @return the winning bid ID
     */
    public int getWinningBidId() {
        return winningBidId;
    }

    /**
     * @param winningBidId the winning bid ID to set
     */
    public void setWinningBidId(int winningBidId) {
        this.winningBidId = winningBidId;
    }

    /**
     * @return the user ID of the officer who made the award
     */
    public int getAwardedBy() {
        return awardedBy;
    }

    /**
     * @param awardedBy the awarding officer's user ID
     */
    public void setAwardedBy(int awardedBy) {
        this.awardedBy = awardedBy;
    }

    /**
     * @return the awarded contract value in Maloti
     */
    public BigDecimal getAwardedValue() {
        return awardedValue;
    }

    /**
     * @param awardedValue the contract value to set
     */
    public void setAwardedValue(BigDecimal awardedValue) {
        this.awardedValue = awardedValue;
    }

    /**
     * @return the award justification text
     */
    public String getJustification() {
        return justification;
    }

    /**
     * @param justification the justification to set
     */
    public void setJustification(String justification) {
        this.justification = justification;
    }

    /**
     * @return the date and time the award was made
     */
    public Timestamp getAwardDate() {
        return awardDate;
    }

    /**
     * @param awardDate the award date to set
     */
    public void setAwardDate(Timestamp awardDate) {
        this.awardDate = awardDate;
    }

    /**
     * @return the tender title (populated via JOIN, may be null)
     */
    public String getTenderTitle() {
        return tenderTitle;
    }

    /**
     * @param tenderTitle the tender title to set
     */
    public void setTenderTitle(String tenderTitle) {
        this.tenderTitle = tenderTitle;
    }

    /**
     * @return the tender reference number (populated via JOIN)
     */
    public String getTenderReferenceNo() {
        return tenderReferenceNo;
    }

    /**
     * @param ref the reference number to set
     */
    public void setTenderReferenceNo(String ref) {
        this.tenderReferenceNo = ref;
    }

    /**
     * @return the winning supplier's company name (populated via JOIN)
     */
    public String getWinningSupplierName() {
        return winningSupplierName;
    }

    /**
     * @param name the supplier name to set
     */
    public void setWinningSupplierName(String name) {
        this.winningSupplierName = name;
    }

    /**
     * @return the awarding officer's full name (populated via JOIN)
     */
    public String getOfficerName() {
        return officerName;
    }

    /**
     * @param officerName the officer name to set
     */
    public void setOfficerName(String officerName) {
        this.officerName = officerName;
    }

    /**
     * Returns a concise string representation for logging and debugging.
     *
     * @return a string containing the award, tender, and winning bid IDs
     */
    @Override
    public String toString() {
        return "AwardNotice{awardId=" + awardId
                + ", tenderId=" + tenderId
                + ", winningBidId=" + winningBidId + "}";
    }
}