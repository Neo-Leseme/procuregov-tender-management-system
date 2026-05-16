package com.controller;

import com.dao.*;
import com.model.*;
import com.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BidServlet — handles all supplier bid operations for Module 3 (Supplier Bid
 * Submission).
 *
 * <p>
 * URL mappings (declared in {@code web.xml}):
 * <ul>
 * <li>{@code GET  /supplier/dashboard} — supplier dashboard</li>
 * <li>{@code GET  /supplier/tenders} — browse open tenders</li>
 * <li>{@code GET  /supplier/tender/detail} — tender detail + submit button</li>
 * <li>{@code GET  /supplier/bid/submit} — show bid form</li>
 * <li>{@code POST /supplier/bid/submit} — process bid submission</li>
 * <li>{@code GET  /supplier/award-notice} — view award notice</li>
 * </ul>
 *
 * <p>
 * Key rules enforced:
 * <ul>
 * <li>Server-side closing date enforcement — JSP never determines whether a
 * tender is still open.</li>
 * <li>One bid per supplier per tender — duplicate submissions are detected and
 * blocked with a clear message.</li>
 * <li>File-size validation runs <em>before</em> the database insert to prevent
 * orphan bid records.</li>
 * <li>POST-Redirect-GET pattern used after successful submission.</li>
 * </ul>
 *
 * @author Neo Leseme
 * @version 1.0
 */
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024, // 1 MB — buffer before writing to disk
        maxFileSize = 15L * 1024 * 1024, // 15 MB per file (generous — validated in code)
        maxRequestSize = 16L * 1024 * 1024 // 16 MB total request
)
public class BidServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BidServlet.class.getName());
    private static final long MAX_FILE_BYTES = 10L * 1024 * 1024; // 10 MB business rule

    private static final String VIEW_DASHBOARD = "/WEB-INF/views/supplier/supplierDashboard.jsp";
    private static final String VIEW_BROWSE = "/WEB-INF/views/supplier/tenderBrowse.jsp";
    private static final String VIEW_DETAIL = "/WEB-INF/views/supplier/tenderDetail.jsp";
    private static final String VIEW_BID_FORM = "/WEB-INF/views/supplier/bidForm.jsp";
    private static final String VIEW_AWARD = "/WEB-INF/views/supplier/awardNotice.jsp";

    private TenderDAO tenderDAO;
    private BidDAO bidDAO;
    private AwardDAO awardDAO;
    private UserDAO userDAO;
    private String uploadDir;

    /**
     * Initialises the servlet by creating DAO instances and resolving the
     * upload directory from {@code web.xml} or the deployed WAR.
     *
     * @throws ServletException if initialisation fails
     */
    @Override
    public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
        bidDAO = new BidDAOImpl();
        awardDAO = new AwardDAOImpl();
        userDAO = new UserDAOImpl();
        // Resolve upload directory from web.xml. If the context-param is
        // missing, fall back to the JVM temporary directory, not the WAR.
        // This keeps uploaded files outside the deployed application package.
        uploadDir = getServletContext().getInitParameter("uploadDir");
        if (uploadDir == null || uploadDir.trim().isEmpty()) {
            uploadDir = System.getProperty("java.io.tmpdir")
                    + File.separator + "procuregov-uploads";
            LOGGER.warning("uploadDir context-param is missing. Using temporary external directory: " + uploadDir);
        }
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        LOGGER.info("BidServlet upload directory: " + uploadDir);
    }

    /* ══════════════════════════════════════════════════════════
       GET dispatcher
       ══════════════════════════════════════════════════════════ */
    /**
     * Handles GET requests for all supplier pages. Calls
     * {@link TenderDAO#closeExpiredTenders()} on every request and enforces the
     * {@code SUPPLIER} role.
     *
     * @param req the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        tenderDAO.closeExpiredTenders();
        if (!SessionUtil.requireRole(req, resp, User.Role.SUPPLIER)) {
            return;
        }

        String path = req.getServletPath();
        switch (path) {
            case "/supplier/dashboard":
                showDashboard(req, resp);
                break;
            case "/supplier/tenders":
                showBrowseTenders(req, resp);
                break;
            case "/supplier/tender/detail":
                showTenderDetail(req, resp);
                break;
            case "/supplier/bid/submit":
                showBidForm(req, resp);
                break;
            case "/supplier/award-notice":
                showAwardNotice(req, resp);
                break;
            default:
                resp.sendRedirect(req.getContextPath() + "/supplier/dashboard");
        }
    }

    /* ══════════════════════════════════════════════════════════
       POST dispatcher
       ══════════════════════════════════════════════════════════ */
    /**
     * Handles POST requests — currently only bid submission. Sets UTF-8
     * encoding and enforces the {@code SUPPLIER} role.
     *
     * @param req the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        tenderDAO.closeExpiredTenders();
        if (!SessionUtil.requireRole(req, resp, User.Role.SUPPLIER)) {
            return;
        }

        if ("/supplier/bid/submit".equals(req.getServletPath())) {
            processBidSubmission(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/supplier/dashboard");
        }
    }

    /* ══════════════════════════════════════════════════════════
       GET handlers
       ══════════════════════════════════════════════════════════ */
    /**
     * Displays the supplier dashboard showing currently open tenders and the
     * supplier's own submitted bids with their statuses.
     *
     * @param req the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    private void showDashboard(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = SessionUtil.getLoggedInUser(req);
        Supplier supp = userDAO.findSupplierByUserId(user.getUserId());

        List<Tender> openTenders = tenderDAO.findOpenTenders();
        List<Bid> myBids = (supp != null)
                ? bidDAO.findBySupplierId(supp.getSupplierId()) : java.util.Collections.emptyList();

        flashToRequest(req);
        req.setAttribute("openTenders", openTenders);
        req.setAttribute("myBids", myBids);
        req.setAttribute("supplier", supp);
        req.getRequestDispatcher(VIEW_DASHBOARD).forward(req, resp);
    }

    /**
     * Displays the browse-tenders page listing all open tenders with an
     * indicator of whether the current supplier has already submitted a bid on
     * each one.
     *
     * @param req the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    private void showBrowseTenders(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = SessionUtil.getLoggedInUser(req);
        Supplier supp = userDAO.findSupplierByUserId(user.getUserId());
        List<Tender> openTenders = tenderDAO.findOpenTenders();

        if (supp != null) {
            for (Tender t : openTenders) {
                boolean hasBid = bidDAO.hasSupplierBidOnTender(supp.getSupplierId(), t.getTenderId());
                req.setAttribute("bid_" + t.getTenderId(), hasBid);
            }
        }

        req.setAttribute("openTenders", openTenders);
        req.setAttribute("supplier", supp);
        req.getRequestDispatcher(VIEW_BROWSE).forward(req, resp);
    }

    /**
     * Displays the full detail view for a single tender, including whether the
     * supplier has already bid, whether the tender is still open (server-side
     * check), and the award notice if the tender has been awarded.
     *
     * @param req the HTTP request containing the tender {@code id}
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    private void showTenderDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int tenderId = parseId(req.getParameter("id"));
        Tender tender = tenderDAO.findById(tenderId);

        if (tender == null) {
            resp.sendRedirect(req.getContextPath() + "/supplier/tenders");
            return;
        }

        User user = SessionUtil.getLoggedInUser(req);
        Supplier supp = userDAO.findSupplierByUserId(user.getUserId());

        boolean hasBid = supp != null && bidDAO.hasSupplierBidOnTender(supp.getSupplierId(), tenderId);
        boolean isStillOpen = isOpenServerSide(tender);
        AwardNotice award = (tender.getStatus() == Tender.Status.AWARDED)
                ? awardDAO.findByTenderId(tenderId) : null;

        flashToRequest(req);
        req.setAttribute("tender", tender);
        req.setAttribute("hasBid", hasBid);
        req.setAttribute("isStillOpen", isStillOpen);
        req.setAttribute("award", award);
        req.setAttribute("supplier", supp);
        req.getRequestDispatcher(VIEW_DETAIL).forward(req, resp);
    }

    /**
     * Displays the bid submission form for a specific tender. Redirects with an
     * error if the tender is not open or the supplier has already submitted a
     * bid.
     *
     * @param req the HTTP request containing {@code tenderId}
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    private void showBidForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int tenderId = parseId(req.getParameter("tenderId"));
        Tender tender = tenderDAO.findById(tenderId);

        if (tender == null || !isOpenServerSide(tender)) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "This tender is not open for bidding.");
            resp.sendRedirect(req.getContextPath() + "/supplier/tenders");
            return;
        }

        User user = SessionUtil.getLoggedInUser(req);
        Supplier supp = userDAO.findSupplierByUserId(user.getUserId());

        if (supp != null && bidDAO.hasSupplierBidOnTender(supp.getSupplierId(), tenderId)) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "You have already submitted a bid for this tender.");
            resp.sendRedirect(req.getContextPath() + "/supplier/tender/detail?id=" + tenderId);
            return;
        }

        req.setAttribute("tender", tender);
        req.setAttribute("supplier", supp);
        req.getRequestDispatcher(VIEW_BID_FORM).forward(req, resp);
    }

    /**
     * Displays the award notice for a specific tender. Accessible only after
     * the tender has been awarded.
     *
     * @param req the HTTP request containing {@code tenderId}
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    private void showAwardNotice(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int tenderId = parseId(req.getParameter("tenderId"));
        req.setAttribute("award", awardDAO.findByTenderId(tenderId));
        req.setAttribute("tender", tenderDAO.findById(tenderId));
        req.getRequestDispatcher(VIEW_AWARD).forward(req, resp);
    }

    /* ══════════════════════════════════════════════════════════
       POST handler — bid submission
       ══════════════════════════════════════════════════════════ */
    /**
     * Processes the bid submission form.
     *
     * <p>
     * Validation order:
     * <ol>
     * <li>Tender exists and supplier profile is available.</li>
     * <li>Server-side closing date check via
     * {@link #isOpenServerSide(Tender)}.</li>
     * <li>One-bid-per-tender rule (database enforced + app check).</li>
     * <li>Field validation: positive bid amount, compliance statement ≤ 600
     * characters, positive delivery days.</li>
     * <li>File size ≤ 10 MB (checked before DB insert).</li>
     * <li>Persist bid, save required supporting document, POST-Redirect-GET.</li>
     * </ol>
     * All validation errors forward back to {@code bidForm.jsp} with the tender
     * object always set so the JSP never throws a
     * {@code NullPointerException}.</p>
     *
     * @param req the HTTP request containing bid form fields
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    private void processBidSubmission(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = SessionUtil.getLoggedInUser(req);
        Supplier supp = userDAO.findSupplierByUserId(user.getUserId());

        int tenderId = parseId(req.getParameter("tenderId"));
        Tender tender = tenderDAO.findById(tenderId);

        if (tender == null) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "Tender not found. Please try again.");
            resp.sendRedirect(req.getContextPath() + "/supplier/tenders");
            return;
        }

        if (supp == null) {
            LOGGER.severe("Supplier profile not found for userId=" + user.getUserId()
                    + ". Check that suppliers table has a row for this user.");
            req.setAttribute(SessionUtil.ATTR_ERROR,
                    "Your supplier profile could not be found. "
                    + "Please contact help.procuregov@gov.ls for assistance.");
            req.setAttribute("tender", tender);
            req.getRequestDispatcher(VIEW_BID_FORM).forward(req, resp);
            return;
        }

        // ── Server-side closing date enforcement ─────────────────
        // JSP must NEVER make this determination.
        // Closing datetime is stored in UTC; convert to local time for comparison.
        if (!isOpenServerSide(tender)) {
            req.setAttribute(SessionUtil.ATTR_ERROR,
                    "This tender is closed. Bid submission is no longer accepted.");
            req.setAttribute("tender", tender);
            req.getRequestDispatcher(VIEW_BID_FORM).forward(req, resp);
            return;
        }

        // ── One bid per tender enforcement ───────────────────────
        if (supp != null && bidDAO.hasSupplierBidOnTender(supp.getSupplierId(), tenderId)) {
            req.setAttribute(SessionUtil.ATTR_ERROR,
                    "You have already submitted a bid for this tender. Only one bid is allowed.");
            req.setAttribute("tender", tender);
            req.getRequestDispatcher(VIEW_BID_FORM).forward(req, resp);
            return;
        }

        // ── Collect and validate fields ───────────────────────────
        String amountStr = sanitise(req.getParameter("bidAmount"));
        String statement = sanitise(req.getParameter("complianceStatement"));
        String daysStr = sanitise(req.getParameter("deliveryDays"));

        if (amountStr.isEmpty() || statement.isEmpty() || daysStr.isEmpty()) {
            req.setAttribute(SessionUtil.ATTR_ERROR, "All fields are required.");
            req.setAttribute("tender", tender);
            req.getRequestDispatcher(VIEW_BID_FORM).forward(req, resp);
            return;
        }

        if (statement.length() > 600) {
            req.setAttribute(SessionUtil.ATTR_ERROR,
                    "Compliance statement must not exceed 600 characters. Yours is "
                    + statement.length() + ".");
            req.setAttribute("tender", tender);
            req.getRequestDispatcher(VIEW_BID_FORM).forward(req, resp);
            return;
        }

        BigDecimal bidAmount;
        int deliveryDays;

        try {
            bidAmount = new BigDecimal(amountStr);
            if (bidAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            req.setAttribute(SessionUtil.ATTR_ERROR, "Bid amount must be a positive number.");
            req.setAttribute("tender", tender);
            req.getRequestDispatcher(VIEW_BID_FORM).forward(req, resp);
            return;
        }

        try {
            deliveryDays = Integer.parseInt(daysStr);
            if (deliveryDays <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            req.setAttribute(SessionUtil.ATTR_ERROR, "Delivery days must be a positive whole number.");
            req.setAttribute("tender", tender);
            req.getRequestDispatcher(VIEW_BID_FORM).forward(req, resp);
            return;
        }

        // ── Validate required supporting document BEFORE inserting bid ─
        Part filePart;
        try {
            filePart = req.getPart("bidDocument");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not read bid supporting document", e);
            req.setAttribute(SessionUtil.ATTR_ERROR,
                    "Supporting document could not be read. Please upload a valid PDF or DOCX file.");
            req.setAttribute("tender", tender);
            req.getRequestDispatcher(VIEW_BID_FORM).forward(req, resp);
            return;
        }

        if (filePart == null || filePart.getSize() <= 0
                || filePart.getSubmittedFileName() == null
                || filePart.getSubmittedFileName().trim().isEmpty()) {
            req.setAttribute(SessionUtil.ATTR_ERROR,
                    "A supporting document is required. Upload one PDF or DOCX file before submitting.");
            req.setAttribute("tender", tender);
            req.getRequestDispatcher(VIEW_BID_FORM).forward(req, resp);
            return;
        }

        String submittedDocName = Paths.get(filePart.getSubmittedFileName())
                .getFileName().toString().toLowerCase();
        if (!(submittedDocName.endsWith(".pdf") || submittedDocName.endsWith(".docx"))) {
            req.setAttribute(SessionUtil.ATTR_ERROR,
                    "Supporting document must be PDF or DOCX only. DOC and other file types are rejected.");
            req.setAttribute("tender", tender);
            req.getRequestDispatcher(VIEW_BID_FORM).forward(req, resp);
            return;
        }

        if (filePart.getSize() > MAX_FILE_BYTES) {
            req.setAttribute(SessionUtil.ATTR_ERROR,
                    "Supporting document exceeds the 10 MB limit. "
                    + "Your file is "
                    + String.format("%.1f", filePart.getSize() / (1024.0 * 1024.0))
                    + " MB. Please compress it or use a smaller file.");
            req.setAttribute("tender", tender);
            req.getRequestDispatcher(VIEW_BID_FORM).forward(req, resp);
            return;
        }

        // ── Insert bid record ──────────────────────────────────────
        Bid bid = new Bid();
        bid.setTenderId(tenderId);
        bid.setSupplierId(supp.getSupplierId());
        bid.setBidAmount(bidAmount);
        bid.setComplianceStatement(statement);
        bid.setDeliveryDays(deliveryDays);
        bid.setStatus(Bid.Status.SUBMITTED);

        int bidId = bidDAO.insertBid(bid);
        if (bidId == -1) {
            req.setAttribute(SessionUtil.ATTR_ERROR,
                    "Failed to save your bid due to a system error. Please try again.");
            req.setAttribute("tender", tender);
            req.getRequestDispatcher(VIEW_BID_FORM).forward(req, resp);
            return;
        }

        // ── Save required supporting document ───────────────────────
        String savedPath = saveUploadedFile(filePart, bidId, "bid");
        if (savedPath != null) {
            bidDAO.updateDocumentPath(bidId, savedPath);
        } else {
            LOGGER.warning("Bid was saved but supporting document failed to save for bidId=" + bidId);
        }

        LOGGER.info("Bid submitted: bidId=" + bidId + " tenderId=" + tenderId
                + " supplierId=" + supp.getSupplierId());

        // ── POST-Redirect-GET ──────────────────────────────────────
        req.getSession().setAttribute(SessionUtil.ATTR_SUCCESS,
                "Your bid for " + tender.getReferenceNo() + " has been submitted successfully.");
        resp.sendRedirect(req.getContextPath() + "/supplier/dashboard");
    }

    /* ── File save helper ────────────────────────────────────── */
    /**
     * Saves an uploaded {@link Part} to the server filesystem and returns only
     * the filename (not the full server path).
     *
     * <p>
     * Storing only the filename in the database prevents exposure of the
     * server's filesystem structure. The full path is reconstructed at download
     * time by {@link FileDownloadServlet#getUploadDir}.</p>
     *
     * @param part the uploaded file part
     * @param id the entity ID (used in the filename)
     * @param prefix a filename prefix (e.g. {@code "bid"})
     * @return the saved filename (e.g. {@code bid_24_1777968189650.pdf}), or
     * {@code null} on failure
     */
    private String saveUploadedFile(Part part, int id, String prefix) {
        try {
            String original = Paths.get(part.getSubmittedFileName()).getFileName().toString();
            String ext = original.contains(".")
                    ? original.substring(original.lastIndexOf('.')).toLowerCase() : ".pdf";
            String fileName = prefix + "_" + id + "_" + System.currentTimeMillis() + ext;
            String fullPath = uploadDir + File.separator + fileName;
            try (InputStream in = part.getInputStream()) {
                Files.copy(in, Paths.get(fullPath), StandardCopyOption.REPLACE_EXISTING);
            }
            return fileName;  // Store only filename, not full server path
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "saveUploadedFile failed for id=" + id, e);
            return null;
        }
    }

    /* ── Utility helpers ─────────────────────────────────────── */
    /**
     * Determines whether a tender is still open for bidding from the
     * server-side perspective. The JSP must never make this determination.
     *
     * <p>
     * Converts the UTC timestamp stored in MySQL to the JVM's local timezone
     * before comparing against {@link LocalDateTime#now()}.</p>
     *
     * @param tender the tender to check
     * @return {@code true} if the tender is OPEN and the closing time has not
     * yet passed in local time
     */
    private boolean isOpenServerSide(Tender tender) {
        if (tender.getStatus() != Tender.Status.OPEN || tender.getClosingDatetime() == null) {
            return false;
        }
        // Convert UTC timestamp from DB to JVM's local timezone for comparison
        ZonedDateTime closingLocal = tender.getClosingDatetime().toInstant()
                .atZone(ZoneId.systemDefault());
        return closingLocal.toLocalDateTime().isAfter(LocalDateTime.now());
    }

    /**
     * Moves success/error flash messages from session scope to request scope so
     * JSPs can display them using {@code ${successMsg}} or {@code ${errorMsg}}
     * without checking {@code sessionScope}.
     *
     * @param req the HTTP request
     */
    private void flashToRequest(HttpServletRequest req) {
        String s = (String) req.getSession().getAttribute(SessionUtil.ATTR_SUCCESS);
        String e = (String) req.getSession().getAttribute(SessionUtil.ATTR_ERROR);
        if (s != null) {
            req.setAttribute(SessionUtil.ATTR_SUCCESS, s);
            req.getSession().removeAttribute(SessionUtil.ATTR_SUCCESS);
        }
        if (e != null) {
            req.setAttribute(SessionUtil.ATTR_ERROR, e);
            req.getSession().removeAttribute(SessionUtil.ATTR_ERROR);
        }
    }

    /**
     * Parses a request parameter into an integer, returning -1 on failure.
     *
     * @param val the string value to parse
     * @return the parsed integer, or -1 if invalid
     */
    private int parseId(String val) {
        try {
            return Integer.parseInt(val != null ? val.trim() : "");
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Trims and null-safe sanitise helper.
     *
     * @param val the raw string to sanitise
     * @return the trimmed string, or an empty string if {@code val} is null
     */
    private String sanitise(String val) {
        return (val == null) ? "" : val.trim();
    }
}
