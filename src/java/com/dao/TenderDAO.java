package com.dao;

import com.model.Tender;
import java.util.List;

/**
 * TenderDAO — data access interface for all tender operations.
 *
 * <p>All implementations must go through this interface.
 * No JDBC code is permitted in Servlets or JSPs.</p>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public interface TenderDAO {

    /**
     * Inserts a new tender record. Sets the generated {@code tenderId} on the bean.
     *
     * @param tender the tender to persist (referenceNo must already be set)
     * @return the generated tender ID, or -1 on failure
     */
    int insertTender(Tender tender);

    /**
     * Updates an existing tender. Only allowed when status = DRAFT.
     *
     * @param tender the tender with updated fields (tenderId must be set)
     * @return true if the update affected at least one row
     */
    boolean updateTender(Tender tender);

    /**
     * Finds a tender by its primary key.
     *
     * @param tenderId the tender's ID
     * @return the matching {@link Tender}, or {@code null} if not found
     */
    Tender findById(int tenderId);

    /**
     * Finds a tender by its unique reference number.
     *
     * @param referenceNo e.g. "MPW-2026-0001"
     * @return the matching {@link Tender}, or {@code null} if not found
     */
    Tender findByReferenceNo(String referenceNo);

    /**
     * Returns all tenders, ordered by creation date descending.
     *
     * @return list of all tenders (never null, may be empty)
     */
    List<Tender> findAll();

    /**
     * Returns all tenders filtered by status.
     *
     * @param status the status to filter by
     * @return filtered list of tenders
     */
    List<Tender> findByStatus(Tender.Status status);

    /**
     * Returns all tenders filtered by category.
     *
     * @param category the category to filter by
     * @return filtered list of tenders
     */
    List<Tender> findByCategory(Tender.Category category);

    /**
     * Returns all tenders filtered by both status and category.
     *
     * @param status   the status to filter by
     * @param category the category to filter by
     * @return filtered list of tenders
     */
    List<Tender> findByStatusAndCategory(Tender.Status status, Tender.Category category);

    /**
     * Returns all tenders visible to suppliers (status = OPEN or later).
     *
     * @return list of open tenders
     */
    List<Tender> findOpenTenders();

    /**
     * Updates only the status field of a tender.
     *
     * @param tenderId  the tender to update
     * @param newStatus the new status to apply
     * @return true if the update succeeded
     */
    boolean updateStatus(int tenderId, Tender.Status newStatus);

    /**
     * Stores the server-side file path of the uploaded tender notice PDF.
     *
     * @param tenderId the tender to update
     * @param filePath the absolute path on the server filesystem
     * @return true if the update succeeded
     */
    boolean updateNoticeFilePath(int tenderId, String filePath);

    /**
     * Closes all tenders whose closing datetime has passed and are still OPEN.
     * Called on each request to enforce automatic closure.
     *
     * @return the number of tenders automatically closed
     */
    int closeExpiredTenders();

    /**
     * Returns all tenders created by a specific officer.
     *
     * @param officerUserId the officer's user ID
     * @return list of tenders created by that officer
     */
    List<Tender> findByOfficer(int officerUserId);
}