package com.controller;

import com.dao.BidDAO;
import com.dao.BidDAOImpl;
import com.dao.TenderDAO;
import com.dao.TenderDAOImpl;
import com.model.Bid;
import com.model.Tender;
import com.model.User;
import com.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FileDownloadServlet — securely serves uploaded PDF and DOCX files
 * without exposing server filesystem paths to the browser.
 *
 * <p>Handles two download types:
 * <ul>
 *   <li>{@code GET /files/download?type=notice&tenderId=X}
 *       — streams the tender notice PDF (all authenticated users)</li>
 *   <li>{@code GET /files/download?type=bid&bidId=X}
 *       — streams a bid support document (PDF or DOCX; restricted to
 *       the owning supplier, procurement officers, and evaluation
 *       committee members)</li>
 * </ul>
 *
 * <p>File paths are never stored in the database — only filenames.
 * The full path is reconstructed at runtime using the configurable
 * upload directory from {@code web.xml} or the default WAR uploads
 * folder. This prevents exposure of server filesystem structure if
 * the database is compromised.</p>
 *
 * <p>All downloads are streamed through this servlet after
 * authentication and authorisation checks. The original file extension
 * is preserved so that DOCX files open correctly in Microsoft Word or
 * compatible editors.</p>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public class FileDownloadServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FileDownloadServlet.class.getName());

    private TenderDAO tenderDAO;
    private BidDAO    bidDAO;

    /**
     * Initialises the servlet by creating DAO instances.
     *
     * @throws ServletException if initialisation fails
     */
    @Override
    public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
        bidDAO    = new BidDAOImpl();
    }

    /**
     * Routes GET requests to the appropriate download handler based on
     * the {@code type} query parameter.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!SessionUtil.requireLogin(req, resp)) return;

        String type = req.getParameter("type") != null ? req.getParameter("type") : "notice";

        switch (type) {
            case "bid":    serveBidDocument(req, resp);    break;
            case "notice":
            default:       serveTenderNotice(req, resp);   break;
        }
    }

    /* ── Tender notice download ─────────────────────────────── */

    /**
     * Streams the tender notice PDF to the authenticated client.
     * All authenticated users (including suppliers) may download
     * tender notice documents.
     *
     * <p>The database stores only the filename (e.g.
     * {@code notice_4_1777968189650.pdf}). The full server path
     * is reconstructed by combining the configurable upload
     * directory with the stored filename.</p>
     *
     * @param req  the HTTP request containing the {@code tenderId} parameter
     * @param resp the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void serveTenderNotice(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int tenderId = parseId(req.getParameter("tenderId"));
        if (tenderId <= 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing tender ID.");
            return;
        }

        Tender tender = tenderDAO.findById(tenderId);
        if (tender == null || tender.getNoticeFilePath() == null
                || tender.getNoticeFilePath().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                "No notice document has been uploaded for this tender.");
            return;
        }

        // Reconstruct full path from upload directory + stored filename
        String fullPath = getUploadDir() + File.separator + tender.getNoticeFilePath();
        String filename = tender.getReferenceNo().replace("/", "-") + "_Notice.pdf";
        streamFile(resp, fullPath, filename, true);
    }

    /* ── Bid document download ──────────────────────────────── */

    /**
     * Streams a bid support document (PDF or DOCX) to the client.
     * Access is restricted to:
     * <ul>
     *   <li>The supplier who owns the bid</li>
     *   <li>Procurement Officers</li>
     *   <li>Evaluation Committee Members</li>
     * </ul>
     * The original file extension is preserved so that DOCX files
     * are served with the correct MIME type and filename.
     *
     * <p>The database stores only the filename. The full server path
     * is reconstructed at runtime.</p>
     *
     * @param req  the HTTP request containing the {@code bidId} parameter
     * @param resp the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void serveBidDocument(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        User user  = SessionUtil.getLoggedInUser(req);
        int  bidId = parseId(req.getParameter("bidId"));

        if (bidId <= 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing bid ID.");
            return;
        }

        Bid bid = bidDAO.findById(bidId);
        if (bid == null || bid.getDocumentPath() == null || bid.getDocumentPath().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                "No document found for this bid.");
            return;
        }

        // Access control for bid documents:
        // - Suppliers can only download their own bid document
        // - Procurement Officers and Evaluation Committee can download any bid document
        if (user.getRole() == User.Role.SUPPLIER
                && bid.getSupplierUserId() != user.getUserId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                "You do not have permission to access this document.");
            return;
        }

        // Reconstruct full path from upload directory + stored filename
        String storedName = bid.getDocumentPath();
        String fullPath = getUploadDir() + File.separator + storedName;

        // Preserve the original file extension so DOCX and PDF files are served correctly
        String ext = ".pdf"; // default fallback
        String lowerPath = storedName.toLowerCase();
        if (lowerPath.endsWith(".docx")) {
            ext = ".docx";
        } else if (lowerPath.endsWith(".doc")) {
            ext = ".doc";
        } else if (lowerPath.endsWith(".pdf")) {
            ext = ".pdf";
        }

        String filename = "Bid_" + bidId + "_Document" + ext;
        streamFile(resp, fullPath, filename, false);
    }

    /* ── Core streaming method ──────────────────────────────── */

    /**
     * Streams a file from the server filesystem to the browser.
     * Detects the content type from the file extension and sets
     * appropriate response headers.
     *
     * @param resp     the HTTP response
     * @param filePath absolute path on the server filesystem
     * @param filename the filename shown in the browser download dialog
     * @param inline   {@code true} to display in browser,
     *                 {@code false} to force download
     * @throws IOException if an I/O error occurs
     */
    private void streamFile(HttpServletResponse resp,
                             String filePath,
                             String filename,
                             boolean inline) throws IOException {

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            LOGGER.warning("File not found on disk: " + filePath);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                "The requested document is not available on the server.");
            return;
        }

        // Detect content type from the actual file
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            // Fallback detection by extension
            String lower = filePath.toLowerCase();
            if      (lower.endsWith(".pdf"))  contentType = "application/pdf";
            else if (lower.endsWith(".docx")) contentType =
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            else if (lower.endsWith(".doc"))  contentType = "application/msword";
            else                              contentType = "application/octet-stream";
        }

        String disposition = inline ? "inline" : "attachment";

        resp.setContentType(contentType);
        resp.setContentLengthLong(file.length());
        resp.setHeader("Content-Disposition",
            disposition + "; filename=\"" + filename + "\"");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        try (InputStream  in  = new FileInputStream(file);
             OutputStream out = resp.getOutputStream()) {

            byte[] buffer = new byte[8192];
            int    read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
            LOGGER.info("File streamed: " + filePath + " (" + file.length() + " bytes)");

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Stream interrupted for file: " + filePath, e);
        }
    }

    /**
     * Returns the upload directory path, resolved from {@code web.xml}
     * context-param {@code uploadDir}. If no parameter is configured, the
     * JVM temporary directory is used so files remain outside the WAR.
     *
     * @return the absolute path to the upload directory
     */
    private String getUploadDir() {
        String dir = getServletContext().getInitParameter("uploadDir");
        if (dir == null || dir.trim().isEmpty()) {
            dir = System.getProperty("java.io.tmpdir")
                    + File.separator + "procuregov-uploads";
        }
        return dir;
    }

    /**
     * Parses a request parameter into an integer, returning -1 on failure.
     *
     * @param val the string value to parse
     * @return the parsed integer, or -1 if invalid
     */
    private int parseId(String val) {
        try { return Integer.parseInt(val != null ? val.trim() : ""); }
        catch (NumberFormatException e) { return -1; }
    }
}