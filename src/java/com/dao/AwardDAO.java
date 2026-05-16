package com.dao;

import com.model.AwardNotice;

/**
 * AwardDAO — data access interface for award notice operations.
 *
 * <p>All implementations must go through this interface.
 * No JDBC code is permitted in Servlets or JSPs.</p>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public interface AwardDAO {

    /**
     * Persists a new award notice record.
     *
     * @param notice the award notice to insert
     * @return the generated award ID, or -1 on failure
     */
    int insertAwardNotice(AwardNotice notice);

    /**
     * Finds the award notice for a given tender.
     *
     * @param tenderId the tender's ID
     * @return the award notice, or {@code null} if not yet awarded
     */
    AwardNotice findByTenderId(int tenderId);
}