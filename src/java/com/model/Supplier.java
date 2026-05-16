package com.model;

import java.io.Serializable;

/**
 * Supplier — JavaBean representing the supplier-specific details
 * linked to a {@link User} account.
 *
 * <p>One-to-one relationship with {@code users} table where
 * {@code role = SUPPLIER}. Maps directly to the {@code suppliers}
 * table.</p>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public class Supplier implements Serializable {

    private static final long serialVersionUID = 1L;

    private int supplierId;
    private int userId;                 // FK to users table
    private String companyName;
    private String registrationNo;      // auto-generated e.g. SUP-20260001
    private String physicalAddress;
    private String contactNumber;

    // Optional: populated via JOIN when needed
    private User user;

    /**
     * Default no-argument constructor.
     */
    public Supplier() {
    }

    /* ── Getters & Setters ─────────────────────────────── */

    /**
     * @return the supplier ID
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
     * @return the user ID linked to this supplier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @param userId the user ID to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * @return the company or individual name
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * @param companyName the company name to set
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * @return the auto-generated registration number (e.g. SUP-20260001)
     */
    public String getRegistrationNo() {
        return registrationNo;
    }

    /**
     * @param rn the registration number to set
     */
    public void setRegistrationNo(String rn) {
        this.registrationNo = rn;
    }

    /**
     * @return the physical business address
     */
    public String getPhysicalAddress() {
        return physicalAddress;
    }

    /**
     * @param addr the physical address to set
     */
    public void setPhysicalAddress(String addr) {
        this.physicalAddress = addr;
    }

    /**
     * @return the contact telephone number
     */
    public String getContactNumber() {
        return contactNumber;
    }

    /**
     * @param contactNumber the contact number to set
     */
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    /**
     * @return the linked {@link User} object (populated via JOIN, may be null)
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user the User to link
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns a concise string representation for logging and debugging.
     *
     * @return a string containing the supplier ID, company name, and registration number
     */
    @Override
    public String toString() {
        return "Supplier{supplierId=" + supplierId
                + ", companyName='" + companyName + "'"
                + ", registrationNo='" + registrationNo + "'}";
    }
}