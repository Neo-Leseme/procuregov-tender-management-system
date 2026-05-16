package com.controller;

import com.dao.EvalDAO;
import com.dao.EvalDAOImpl;
import com.dao.TenderDAO;
import com.dao.TenderDAOImpl;
import com.dao.UserDAO;
import com.dao.UserDAOImpl;
import com.model.Tender;
import com.model.User;
import com.util.ReferenceGenerator;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TenderServlet — handles all tender management operations for Procurement
 * Officers (Module 2).
 *
 * <p>
 * URL mappings (declared in {@code web.xml}):
 * <ul>
 * <li>{@code GET  /officer/dashboard} — officer dashboard</li>
 * <li>{@code GET  /officer/tenders} — tender list with filters</li>
 * <li>{@code GET  /officer/tender/create} — show create form</li>
 * <li>{@code POST /officer/tender/create} — process create form</li>
 * <li>{@code GET  /officer/tender/edit} — show edit form (Draft only)</li>
 * <li>{@code POST /officer/tender/edit} — process edit form</li>
 * <li>{@code GET  /officer/tender/detail} — tender detail view</li>
 * <li>{@code POST /officer/tender/status} — change tender status</li>
 * </ul>
 *
 * <p>
 * Key rules enforced:
 * <ul>
 * <li>Tender reference numbers are system-generated ({@code MPW-YYYY-NNNN}) —
 * the officer never types them.</li>
 * <li>Tenders can only be edited while in {@code DRAFT} status.</li>
 * <li>Status transitions are forward-only; {@code CLOSED} is set automatically
 * by the system, never manually.</li>
 * <li>File uploads (tender notice PDF) are handled via the Servlet {@link Part}
 * API, limited to 5 MB.</li>
 * <li>When moving to {@code UNDER_EVALUATION}, both Evaluation Committee
 * Members and Procurement Officers are appointed as evaluators so that all four
 * must score before auto-transition to {@code EVALUATED}.</li>
 * </ul>
 *
 * @author Neo Leseme
 * @version 1.0
 */
@MultipartConfig(
        fileSizeThreshold = 1048576,
        maxFileSize = 5242880,
        maxRequestSize = 6291456
)
public class TenderServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TenderServlet.class.getName());
    private static final long MAX_FILE_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final DateTimeFormatter DT_FMT
            = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private static final String VIEW_DASHBOARD = "/WEB-INF/views/officer/officerDashboard.jsp";
    private static final String VIEW_LIST = "/WEB-INF/views/officer/tenderList.jsp";
    private static final String VIEW_FORM = "/WEB-INF/views/officer/tenderForm.jsp";
    private static final String VIEW_DETAIL = "/WEB-INF/views/officer/tenderDetail.jsp";

    private TenderDAO tenderDAO;
    private EvalDAO evalDAO;
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
        evalDAO = new EvalDAOImpl();
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
            LOGGER.info("Created upload directory: " + uploadDir);
        }
        LOGGER.info("TenderServlet upload directory: " + uploadDir);
    }

    /* ══════════════════════════════════════════════════════════
       GET dispatcher
       ══════════════════════════════════════════════════════════ */
    /**
     * Handles GET requests for all officer pages. Calls
     * {@link TenderDAO#closeExpiredTenders()} on every request and enforces the
     * {@code PROCUREMENT_OFFICER} role.
     *
     * @param req the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Auto-close expired tenders on every officer request
        tenderDAO.closeExpiredTenders();

        // All tender management requires PROCUREMENT_OFFICER role
        if (!SessionUtil.requireRole(req, resp, User.Role.PROCUREMENT_OFFICER)) {
            return;
        }

        String path = req.getServletPath();

        switch (path) {
            case "/officer/dashboard":
                showDashboard(req, resp);
                break;
            case "/officer/tenders":
                showTenderList(req, resp);
                break;
            case "/officer/tender/create":
                showCreateForm(req, resp);
                break;
            case "/officer/tender/edit":
                showEditForm(req, resp);
                break;
            case "/officer/tender/detail":
                showTenderDetail(req, resp);
                break;
            default:
                resp.sendRedirect(req.getContextPath() + "/officer/dashboard");
        }
    }

    /* ══════════════════════════════════════════════════════════
       POST dispatcher
       ══════════════════════════════════════════════════════════ */
    /**
     * Handles POST requests for tender create, edit, and status change. Sets
     * UTF-8 encoding and enforces the {@code PROCUREMENT_OFFICER} role.
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

        if (!SessionUtil.requireRole(req, resp, User.Role.PROCUREMENT_OFFICER)) {
            return;
        }

        String path = req.getServletPath();

        switch (path) {
            case "/officer/tender/create":
                processCreate(req, resp);
                break;
            case "/officer/tender/edit":
                processEdit(req, resp);
                break;
            case "/officer/tender/status":
                processStatusChange(req, resp);
                break;
            default:
                resp.sendRedirect(req.getContextPath() + "/officer/dashboard");
        }
    }

    /* ══════════════════════════════════════════════════════════
       GET handlers
       ══════════════════════════════════════════════════════════ */
    /**
     * Displays the officer dashboard with summary counts (Draft, Open, Under
     * Review, Awarded) and a table of all tenders.
     *
     * @param req the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    private void showDashboard(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<Tender> allTenders = tenderDAO.findAll();

        // Summary counts for the dashboard cards
        long draftCount = allTenders.stream().filter(t -> t.getStatus() == Tender.Status.DRAFT).count();
        long openCount = allTenders.stream().filter(t -> t.getStatus() == Tender.Status.OPEN).count();
        long closedCount = allTenders.stream().filter(t -> t.getStatus() == Tender.Status.CLOSED
                || t.getStatus() == Tender.Status.UNDER_EVALUATION).count();
        long awardedCount = allTenders.stream().filter(t -> t.getStatus() == Tender.Status.AWARDED).count();

        req.setAttribute("allTenders", allTenders);
        req.setAttribute("draftCount", draftCount);
        req.setAttribute("openCount", openCount);
        req.setAttribute("closedCount", closedCount);
        req.setAttribute("awardedCount", awardedCount);
        req.setAttribute("categories", Tender.Category.values());

        req.getRequestDispatcher(VIEW_DASHBOARD).forward(req, resp);
    }

    /**
     * Displays the filterable tender list, supporting filtering by status,
     * category, or both. If an invalid filter value is supplied the full list
     * is returned.
     *
     * @param req the HTTP request containing optional {@code status} and
     * {@code category} parameters
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    private void showTenderList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String statusParam = req.getParameter("status");
        String categoryParam = req.getParameter("category");

        List<Tender> tenders;

        try {
            if (statusParam != null && !statusParam.isEmpty()
                    && categoryParam != null && !categoryParam.isEmpty()) {
                tenders = tenderDAO.findByStatusAndCategory(
                        Tender.Status.valueOf(statusParam),
                        Tender.Category.valueOf(categoryParam));
            } else if (statusParam != null && !statusParam.isEmpty()) {
                tenders = tenderDAO.findByStatus(Tender.Status.valueOf(statusParam));
            } else if (categoryParam != null && !categoryParam.isEmpty()) {
                tenders = tenderDAO.findByCategory(Tender.Category.valueOf(categoryParam));
            } else {
                tenders = tenderDAO.findAll();
            }
        } catch (IllegalArgumentException e) {
            tenders = tenderDAO.findAll();
        }

        req.setAttribute("tenders", tenders);
        req.setAttribute("statuses", Tender.Status.values());
        req.setAttribute("categories", Tender.Category.values());
        req.setAttribute("selectedStatus", statusParam);
        req.setAttribute("selectedCat", categoryParam);

        req.getRequestDispatcher(VIEW_LIST).forward(req, resp);
    }

    /**
     * Displays the blank create-tender form with a pre-generated reference
     * number from {@link ReferenceGenerator}.
     *
     * @param req the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    private void showCreateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setAttribute("referenceNo", ReferenceGenerator.nextTenderReference());
        req.setAttribute("categories", Tender.Category.values());
        req.setAttribute("mode", "create");
        req.getRequestDispatcher(VIEW_FORM).forward(req, resp);
    }

    /**
     * Displays the edit form pre-filled with existing tender data. Only tenders
     * in {@code DRAFT} status can be edited.
     *
     * @param req the HTTP request containing the tender {@code id}
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int tenderId = parseId(req.getParameter("id"));
        if (tenderId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/officer/tenders");
            return;
        }

        Tender tender = tenderDAO.findById(tenderId);
        if (tender == null || !tender.isEditable()) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "Tender cannot be edited — it is no longer in Draft status.");
            resp.sendRedirect(req.getContextPath() + "/officer/tenders");
            return;
        }

        req.setAttribute("tender", tender);
        req.setAttribute("categories", Tender.Category.values());
        req.setAttribute("mode", "edit");
        req.getRequestDispatcher(VIEW_FORM).forward(req, resp);
    }

    /**
     * Displays the full detail view for a single tender, including its
     * lifecycle status and available actions.
     *
     * @param req the HTTP request containing the tender {@code id}
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    private void showTenderDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int tenderId = parseId(req.getParameter("id"));
        if (tenderId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/officer/tenders");
            return;
        }

        Tender tender = tenderDAO.findById(tenderId);
        if (tender == null) {
            resp.sendRedirect(req.getContextPath() + "/officer/tenders");
            return;
        }

        req.setAttribute("tender", tender);
        req.setAttribute("statuses", Tender.Status.values());
        req.getRequestDispatcher(VIEW_DETAIL).forward(req, resp);
    }

    /* ══════════════════════════════════════════════════════════
       POST handlers
       ══════════════════════════════════════════════════════════ */
    /**
     * Processes the create-tender form submission.
     *
     * <p>
     * Validation order:
     * <ol>
     * <li>All fields required.</li>
     * <li>Category must be a valid enum value.</li>
     * <li>Estimated value must be a positive number.</li>
     * <li>Closing date/time must be in the future.</li>
     * <li>Generate reference number via
     * {@link ReferenceGenerator#nextTenderReference()}.</li>
     * <li>Validate required tender notice PDF (max 5 MB) before inserting.</li>
     * <li>Redirect to detail page (POST-Redirect-GET).</li>
     * </ol>
     * File upload uses the Servlet {@link Part} API — no third-party
     * libraries.</p>
     *
     * @param req the HTTP request containing tender form fields and required
     * {@code noticeFile} part
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    private void processCreate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User officer = SessionUtil.getLoggedInUser(req);

        // Collect fields
        String title = sanitise(req.getParameter("title"));
        String categoryStr = sanitise(req.getParameter("category"));
        String description = sanitise(req.getParameter("description"));
        String valueStr = sanitise(req.getParameter("estimatedValue"));
        String closingStr = sanitise(req.getParameter("closingDatetime"));

        // Validate
        if (title.isEmpty() || categoryStr.isEmpty() || description.isEmpty()
                || valueStr.isEmpty() || closingStr.isEmpty()) {
            req.setAttribute(SessionUtil.ATTR_ERROR, "All fields are required.");
            req.setAttribute("categories", Tender.Category.values());
            req.setAttribute("mode", "create");
            req.getRequestDispatcher(VIEW_FORM).forward(req, resp);
            return;
        }

        Tender.Category category;
        BigDecimal estimatedValue;
        Timestamp closingDatetime;

        try {
            category = Tender.Category.valueOf(categoryStr);
        } catch (IllegalArgumentException e) {
            setFormError(req, "Invalid category selected.", "create");
            req.getRequestDispatcher(VIEW_FORM).forward(req, resp);
            return;
        }

        try {
            estimatedValue = new BigDecimal(valueStr);
            if (estimatedValue.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            setFormError(req, "Estimated value must be a positive number.", "create");
            req.getRequestDispatcher(VIEW_FORM).forward(req, resp);
            return;
        }

        try {
            LocalDateTime ldt = LocalDateTime.parse(closingStr, DT_FMT);
            if (ldt.isBefore(LocalDateTime.now())) {
                setFormError(req, "Closing date/time must be in the future.", "create");
                req.getRequestDispatcher(VIEW_FORM).forward(req, resp);
                return;
            }
            closingDatetime = Timestamp.valueOf(ldt);
        } catch (DateTimeParseException e) {
            setFormError(req, "Invalid date/time format.", "create");
            req.getRequestDispatcher(VIEW_FORM).forward(req, resp);
            return;
        }

        // Validate required tender notice PDF BEFORE inserting the tender.
        Part noticePart;
        try {
            noticePart = req.getPart("noticeFile");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not read tender notice upload", e);
            setFormError(req, "Tender notice PDF could not be read. Please upload a valid PDF file.", "create");
            req.getRequestDispatcher(VIEW_FORM).forward(req, resp);
            return;
        }

        if (noticePart == null || noticePart.getSize() <= 0
                || noticePart.getSubmittedFileName() == null
                || noticePart.getSubmittedFileName().trim().isEmpty()) {
            setFormError(req, "Tender notice PDF is required for every new tender.", "create");
            req.getRequestDispatcher(VIEW_FORM).forward(req, resp);
            return;
        }

        String noticeName = Paths.get(noticePart.getSubmittedFileName())
                .getFileName().toString().toLowerCase();
        if (!noticeName.endsWith(".pdf")) {
            setFormError(req, "Tender notice document must be a PDF file. Other file types are not accepted.", "create");
            req.getRequestDispatcher(VIEW_FORM).forward(req, resp);
            return;
        }

        if (noticePart.getSize() > MAX_FILE_BYTES) {
            setFormError(req, "Notice file exceeds the 5 MB limit.", "create");
            req.getRequestDispatcher(VIEW_FORM).forward(req, resp);
            return;
        }

        // Build bean
        Tender tender = new Tender();
        tender.setReferenceNo(ReferenceGenerator.nextTenderReference());
        tender.setTitle(title);
        tender.setCategory(category);
        tender.setDescription(description);
        tender.setEstimatedValue(estimatedValue);
        tender.setClosingDatetime(closingDatetime);
        tender.setCreatedBy(officer.getUserId());
        tender.setStatus(Tender.Status.DRAFT);

        // Insert first to get tenderId
        int tenderId = tenderDAO.insertTender(tender);
        if (tenderId == -1) {
            setFormError(req, "Failed to save tender. Please try again.", "create");
            req.getRequestDispatcher(VIEW_FORM).forward(req, resp);
            return;
        }

        // Save required PDF upload. Only the generated filename is stored in the database.
        String savedPath = saveUploadedFile(noticePart, tenderId, "notice");
        if (savedPath != null) {
            tenderDAO.updateNoticeFilePath(tenderId, savedPath);
        } else {
            LOGGER.warning("Tender was saved but notice PDF failed to save for tenderId=" + tenderId);
        }

        LOGGER.info("Tender created: " + tender.getReferenceNo() + " by userId=" + officer.getUserId());

        // POST-Redirect-GET
        req.getSession().setAttribute(SessionUtil.ATTR_SUCCESS,
                "Tender " + tender.getReferenceNo() + " created successfully.");
        resp.sendRedirect(req.getContextPath() + "/officer/tender/detail?id=" + tenderId);
    }

    /**
     * Processes the edit-tender form submission. Only tenders in {@code DRAFT}
     * status can be edited. Supports optional replacement of the notice PDF.
     * Non-PDF files are rejected.
     *
     * @param req the HTTP request containing updated tender fields and optional
     * {@code noticeFile} part
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    private void processEdit(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int tenderId = parseId(req.getParameter("tenderId"));
        Tender existing = tenderDAO.findById(tenderId);

        if (existing == null || !existing.isEditable()) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "This tender can no longer be edited.");
            resp.sendRedirect(req.getContextPath() + "/officer/tenders");
            return;
        }

        String title = sanitise(req.getParameter("title"));
        String categoryStr = sanitise(req.getParameter("category"));
        String description = sanitise(req.getParameter("description"));
        String valueStr = sanitise(req.getParameter("estimatedValue"));
        String closingStr = sanitise(req.getParameter("closingDatetime"));

        if (title.isEmpty() || categoryStr.isEmpty() || description.isEmpty()
                || valueStr.isEmpty() || closingStr.isEmpty()) {
            req.setAttribute(SessionUtil.ATTR_ERROR, "All fields are required.");
            req.setAttribute("tender", existing);
            req.setAttribute("categories", Tender.Category.values());
            req.setAttribute("mode", "edit");
            req.getRequestDispatcher(VIEW_FORM).forward(req, resp);
            return;
        }

        try {
            existing.setTitle(title);
            existing.setCategory(Tender.Category.valueOf(categoryStr));
            existing.setDescription(description);
            existing.setEstimatedValue(new BigDecimal(valueStr));
            LocalDateTime ldt = LocalDateTime.parse(closingStr, DT_FMT);
            existing.setClosingDatetime(Timestamp.valueOf(ldt));
        } catch (Exception e) {
            req.setAttribute(SessionUtil.ATTR_ERROR, "Invalid input. Please check all fields.");
            req.setAttribute("tender", existing);
            req.setAttribute("categories", Tender.Category.values());
            req.setAttribute("mode", "edit");
            req.getRequestDispatcher(VIEW_FORM).forward(req, resp);
            return;
        }

        // Handle optional file, PDF files only
        try {
            Part filePart = req.getPart("noticeFile");
            if (filePart != null && filePart.getSize() > 0) {
                // Validate file type — tender notices must be PDF
                String submittedName = Paths.get(filePart.getSubmittedFileName())
                        .getFileName().toString().toLowerCase();
                if (!submittedName.endsWith(".pdf")) {
                    req.setAttribute(SessionUtil.ATTR_ERROR,
                            "Tender notice document must be a PDF file. "
                            + "Other file types are not accepted.");
                    req.setAttribute("tender", existing);
                    req.setAttribute("categories", Tender.Category.values());
                    req.setAttribute("mode", "edit");
                    req.getRequestDispatcher(VIEW_FORM).forward(req, resp);
                    return;
                }
                if (filePart.getSize() > MAX_FILE_BYTES) {
                    req.setAttribute(SessionUtil.ATTR_ERROR, "Notice file exceeds 5MB limit.");
                    req.setAttribute("tender", existing);
                    req.setAttribute("categories", Tender.Category.values());
                    req.setAttribute("mode", "edit");
                    req.getRequestDispatcher(VIEW_FORM).forward(req, resp);
                    return;
                }
                String savedPath = saveUploadedFile(filePart, tenderId, "notice");
                if (savedPath != null) {
                    tenderDAO.updateNoticeFilePath(tenderId, savedPath);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "File upload failed on edit for tenderId=" + tenderId, e);
        }

        tenderDAO.updateTender(existing);

        req.getSession().setAttribute(SessionUtil.ATTR_SUCCESS, "Tender updated successfully.");
        resp.sendRedirect(req.getContextPath() + "/officer/tender/detail?id=" + tenderId);
    }

    /**
     * Processes a manual status transition request.
     *
     * <p>
     * Enforces valid forward-only transitions:
     * <ul>
     * <li>{@code DRAFT → OPEN}</li>
     * <li>{@code CLOSED → UNDER_EVALUATION}</li>
     * <li>{@code EVALUATED → AWARDED}</li>
     * </ul> {@code OPEN → CLOSED} and {@code UNDER_EVALUATION → EVALUATED} are
     * system-only transitions and cannot be triggered manually.
     *
     * <p>
     * When a tender is published (DRAFT → OPEN), all registered suppliers are
     * notified via email on a background thread.</p>
     *
     * <p>
     * When moving to {@code UNDER_EVALUATION}, all Evaluation Committee Members
     * and Procurement Officers are appointed as evaluators and notified via
     * email on a background thread. This ensures
     * {@link EvalDAO#allEvaluatorsHaveScoredAllBids} waits for all four
     * evaluators before auto-transitioning.</p>
     *
     * @param req the HTTP request containing {@code tenderId} and
     * {@code targetStatus}
     * @param resp the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void processStatusChange(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int tenderId = parseId(req.getParameter("tenderId"));
        String targetStr = sanitise(req.getParameter("targetStatus"));

        Tender tender = tenderDAO.findById(tenderId);
        if (tender == null) {
            resp.sendRedirect(req.getContextPath() + "/officer/tenders");
            return;
        }

        Tender.Status current = tender.getStatus();
        Tender.Status target;

        try {
            target = Tender.Status.valueOf(targetStr);
        } catch (IllegalArgumentException e) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR, "Invalid status.");
            resp.sendRedirect(req.getContextPath() + "/officer/tender/detail?id=" + tenderId);
            return;
        }

        // Enforce valid manual transitions — CLOSED is system-only
        boolean valid = false;
        switch (current) {
            case DRAFT:
                valid = target == Tender.Status.OPEN;
                break;
            case OPEN:
                valid = false; // only auto-close allowed
                break;
            case CLOSED:
                valid = target == Tender.Status.UNDER_EVALUATION;
                break;
            case UNDER_EVALUATION:
                valid = false; // auto-transitions to EVALUATED
                break;
            case EVALUATED:
                valid = target == Tender.Status.AWARDED;
                break;
            default:
                valid = false;
        }

        if (!valid) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "Invalid transition from " + current + " to " + target + ".");
            resp.sendRedirect(req.getContextPath() + "/officer/tender/detail?id=" + tenderId);
            return;
        }

        tenderDAO.updateStatus(tenderId, target);
        LOGGER.info("Tender " + tender.getReferenceNo() + " status: " + current + " → " + target);

        // ── Email: Notify all suppliers when a tender is published ─────
        if (target == Tender.Status.OPEN) {
            final String tenderTitle = tender.getTitle();
            final String tenderRef = tender.getReferenceNo();
            final String categoryLabel = tender.getCategory().getLabel();
            final String closingDate = tender.getClosingDatetime().toString();
            final String serverBase = req.getScheme() + "://" + req.getServerName()
                    + ":" + req.getServerPort();
            final String contextPath = req.getContextPath();

            new Thread(() -> {
                try {
                    com.dao.UserDAO bgUserDAO = new com.dao.UserDAOImpl();
                    java.util.List<com.model.User> suppliers
                            = bgUserDAO.findAllByRole(com.model.User.Role.SUPPLIER);
                    java.util.List<String> emails = new java.util.ArrayList<>();
                    java.util.List<String> names = new java.util.ArrayList<>();
                    for (com.model.User s : suppliers) {
                        emails.add(s.getEmail());
                        names.add(s.getFullName());
                    }
                    com.util.EmailService.sendNewTenderNotification(
                            tenderTitle, tenderRef, categoryLabel, closingDate,
                            emails, names, contextPath, serverBase);
                    LOGGER.info("New tender notifications sent for " + tenderRef);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING,
                            "Failed to send new tender notifications for " + tenderRef, e);
                }
            }, "email-new-tender-" + tenderId).start();
        }

        // ── Email: Notify evaluators when evaluation starts ───────────
        if (target == Tender.Status.UNDER_EVALUATION) {
            final String tenderTitle2 = tender.getTitle();
            final String tenderRef2 = tender.getReferenceNo();
            final String serverBase2 = req.getScheme() + "://" + req.getServerName()
                    + ":" + req.getServerPort();
            final String contextPath2 = req.getContextPath();

            // Appoint ALL evaluators: both EVAL_COMMITTEE members AND
            // PROCUREMENT_OFFICER officers. The spec (Module 4) requires
            // Procurement Officers to also participate in scoring bids.
            // 1. Appoint Evaluation Committee Members
            java.util.List<com.model.User> committeeMembers
                    = userDAO.findAllByRole(User.Role.EVAL_COMMITTEE);
            for (com.model.User evaluator : committeeMembers) {
                evalDAO.appointEvaluator(tenderId, evaluator.getUserId());
                LOGGER.info("Appointed committee evaluator: userId="
                        + evaluator.getUserId()
                        + " (" + evaluator.getFullName()
                        + ") for tenderId=" + tenderId);
            }

            // 2. Appoint Procurement Officers
            java.util.List<com.model.User> officers
                    = userDAO.findAllByRole(User.Role.PROCUREMENT_OFFICER);
            for (com.model.User officer : officers) {
                evalDAO.appointEvaluator(tenderId, officer.getUserId());
                LOGGER.info("Appointed officer evaluator: userId="
                        + officer.getUserId()
                        + " (" + officer.getFullName()
                        + ") for tenderId=" + tenderId);
            }

            int totalAppointed = committeeMembers.size() + officers.size();
            LOGGER.info("Total evaluators appointed: " + totalAppointed
                    + " for tenderId=" + tenderId
                    + " (" + committeeMembers.size() + " committee + "
                    + officers.size() + " officers)");

            // Send evaluation start notification emails on a background thread
            new Thread(() -> {
                try {
                    com.dao.UserDAO bgUserDAO = new com.dao.UserDAOImpl();
                    java.util.List<com.model.User> evalCommittee
                            = bgUserDAO.findAllByRole(com.model.User.Role.EVAL_COMMITTEE);
                    java.util.List<com.model.User> evalOfficers
                            = bgUserDAO.findAllByRole(com.model.User.Role.PROCUREMENT_OFFICER);
                    java.util.List<String> emails = new java.util.ArrayList<>();
                    java.util.List<String> names = new java.util.ArrayList<>();
                    for (com.model.User u : evalCommittee) {
                        emails.add(u.getEmail());
                        names.add(u.getFullName());
                    }
                    for (com.model.User u : evalOfficers) {
                        emails.add(u.getEmail());
                        names.add(u.getFullName());
                    }
                    com.util.EmailService.sendEvaluationStartNotification(
                            tenderTitle2, tenderRef2, emails, names,
                            contextPath2, serverBase2);
                    LOGGER.info("Evaluation start notifications sent for " + tenderRef2);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING,
                            "Failed to send evaluation notifications for " + tenderRef2, e);
                }
            }, "email-eval-start-" + tenderId).start();
        }

        req.getSession().setAttribute(SessionUtil.ATTR_SUCCESS,
                "Tender status updated to " + target.name().replace("_", " ") + ".");
        resp.sendRedirect(req.getContextPath() + "/officer/tender/detail?id=" + tenderId);
    }

    /* ── File upload helper ──────────────────────────────────── */
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
     * @param tenderId the tender ID (used in the filename)
     * @param prefix a filename prefix (e.g. {@code "notice"})
     * @return the saved filename (e.g. {@code notice_4_1777968189650.pdf}), or
     * {@code null} on failure
     */
    private String saveUploadedFile(Part part, int tenderId, String prefix) {
        try {
            String originalName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
            String ext = originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf('.')).toLowerCase()
                    : ".pdf";
            String fileName = prefix + "_" + tenderId + "_" + System.currentTimeMillis() + ext;
            String fullPath = uploadDir + File.separator + fileName;

            try (InputStream in = part.getInputStream()) {
                Files.copy(in, Paths.get(fullPath), StandardCopyOption.REPLACE_EXISTING);
            }
            LOGGER.info("File saved: " + fullPath);
            return fileName;  // Store only filename, not full server path
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "saveUploadedFile failed", e);
            return null;
        }
    }

    /* ── Utility helpers ─────────────────────────────────────── */
    /**
     * Trims and null-safe sanitise helper.
     *
     * @param val the raw string to sanitise
     * @return the trimmed string, or an empty string if {@code val} is null
     */
    private String sanitise(String val) {
        return (val == null) ? "" : val.trim();
    }

    /**
     * Parses a request parameter into an integer, returning -1 on failure.
     *
     * @param val the string value to parse
     * @return the parsed integer, or -1 if invalid
     */
    private int parseId(String val) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Sets the error message and form mode so JSPs can display validation
     * errors without accessing the session.
     *
     * @param req the HTTP request
     * @param msg the error message to display
     * @param mode the form mode ({@code "create"} or {@code "edit"})
     */
    private void setFormError(HttpServletRequest req, String msg, String mode) {
        req.setAttribute(SessionUtil.ATTR_ERROR, msg);
        req.setAttribute("categories", Tender.Category.values());
        req.setAttribute("mode", mode);
    }
}
