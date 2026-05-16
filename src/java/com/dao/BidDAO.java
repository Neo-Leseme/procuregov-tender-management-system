package com.dao;

import com.model.Bid;
import java.util.List;

/**
 * BidDAO — full data access interface for bid operations.
 *
 * <p>All implementations must go through this interface.
 * No JDBC code is permitted in Servlets or JSPs.</p>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public interface BidDAO {

    /**
     * Persists a new bid record. Sets the generated {@code bidId} on the bean.
     *
     * @param bid the bid to insert
     * @return the generated bid ID, or -1 on failure
     */
    int insertBid(Bid bid);

    /**
     * Returns all bids submitted for a tender, enriched with supplier info.
     *
     * @param tenderId the tender's ID
     * @return list of bids ordered by submitted date descending
     */
    List<Bid> findByTenderId(int tenderId);

    /**
     * Finds a single bid by its primary key.
     *
     * @param bidId the bid's ID
     * @return the matching {@link Bid}, or {@code null} if not found
     */
    Bid findById(int bidId);

    /**
     * Returns all bids submitted by a specific supplier across all tenders.
     *
     * @param supplierId the supplier's ID
     * @return list of the supplier's bids, newest first
     */
    List<Bid> findBySupplierId(int supplierId);

    /**
     * Checks whether a supplier has already submitted a bid on a tender.
     * Enforces the one-bid-per-tender rule.
     *
     * @param supplierId the supplier's ID
     * @param tenderId   the tender's ID
     * @return {@code true} if a bid already exists
     */
    boolean hasSupplierBidOnTender(int supplierId, int tenderId);

    /**
     * Finds the bid submitted by a specific supplier on a specific tender.
     *
     * @param supplierId the supplier's ID
     * @param tenderId   the tender's ID
     * @return the matching {@link Bid}, or {@code null} if none
     */
    Bid findBySupplierAndTender(int supplierId, int tenderId);

    /**
     * Stores the server-side file path of the uploaded bid document.
     *
     * @param bidId the bid to update
     * @param path  the absolute path on the server filesystem
     * @return true if the update succeeded
     */
    boolean updateDocumentPath(int bidId, String path);

    /**
     * Updates all bid statuses when a contract is awarded.
     * Winning bid → AWARDED, all others → NOT_AWARDED.
     *
     * @param tenderId     the tender's ID
     * @param winningBidId the winning bid ID
     */
    void updateBidStatusesOnAward(int tenderId, int winningBidId);
}