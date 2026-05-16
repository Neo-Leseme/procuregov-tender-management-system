package com.controller;

import com.dao.*;
import com.model.*;
import com.util.EmailService;
import com.util.EvaluationService;
import com.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EvalServlet — handles bid evaluation, scoring, and the contract award
 * flow for Module 4 (Bid Evaluation).
 *
 * <p>URL mappings (declared in {@code web.xml}):
 * <ul>
 *   <li>{@code GET  /eval/dashboard}       — evaluation committee dashboard</li>
 *   <li>{@code GET  /eval/panel}           — scoring panel (committee member)</li>
 *   <li>{@code POST /eval/score/submit}    — submit scores for one bid</li>
 *   <li>{@code GET  /officer/eval/panel}   — evaluation panel / ranked
 *       leaderboard (procurement officer view)</li>
 *   <li>{@code POST /officer/tender/award} — process contract award form</li>
 * </ul>
 *
 * <p>Key rules enforced:
 * <ul>
 *   <li>Evaluators cannot see other evaluators' scores until they have
 *       submitted their own for that bid.</li>
 *   <li>All score calculations are delegated to
 *       {@link EvaluationService} — never performed here.</li>
 *   <li>Auto-transition to {@code EVALUATED} is triggered server-side
 *       after each score submission when all evaluators have scored all
 *       bids.</li>
 *   <li>Procurement Officers participate as evaluators alongside
 *       Evaluation Committee Members.</li>
 *   <li>Before {@code EVALUATED}, officers see the scoring panel; after,
 *       they see the ranked leaderboard and award form.</li>
 *   <li>Award emails are sent on a daemon background thread so the
 *       officer's UI is not blocked.</li>
 * </ul>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public class EvalServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(EvalServlet.class.getName());

    private static final String VIEW_EVAL_DASHBOARD = "/WEB-INF/views/eval/evalDashboard.jsp";
    private static final String VIEW_SCORING_PANEL  = "/WEB-INF/views/eval/scoringPanel.jsp";
    private static final String VIEW_EVAL_PANEL     = "/WEB-INF/views/officer/evalPanel.jsp";
    private static final String VIEW_AWARD_FORM     = "/WEB-INF/views/officer/awardForm.jsp";

    private TenderDAO tenderDAO;
    private BidDAO    bidDAO;
    private EvalDAO   evalDAO;
    private AwardDAO  awardDAO;

    /**
     * Initialises the servlet by creating all required DAO instances.
     *
     * @throws ServletException if initialisation fails
     */
    @Override
    public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
        bidDAO    = new BidDAOImpl();
        evalDAO   = new EvalDAOImpl();
        awardDAO  = new AwardDAOImpl();
    }

    /* ══════════════════════════════════════════════════════════
       GET dispatcher
       ══════════════════════════════════════════════════════════ */

    /**
     * Handles GET requests for evaluation pages.
     * Requires the user to be logged in; specific role checks are
     * performed by each handler.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!SessionUtil.requireLogin(req, resp)) {
            return;
        }

        String path = req.getServletPath();

        switch (path) {
            case "/eval/dashboard":
                showEvalDashboard(req, resp);
                break;
            case "/eval/panel":
                showScoringPanel(req, resp);
                break;
            case "/officer/eval/panel":
                showOfficerEvalPanel(req, resp);
                break;
            default:
                resp.sendRedirect(req.getContextPath() + "/");
        }
    }

    /* ══════════════════════════════════════════════════════════
       POST dispatcher
       ══════════════════════════════════════════════════════════ */

    /**
     * Handles POST requests for score submission and contract award.
     * Requires the user to be logged in; sets UTF-8 encoding on the
     * request before processing.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        if (!SessionUtil.requireLogin(req, resp)) {
            return;
        }

        String path = req.getServletPath();

        switch (path) {
            case "/eval/score/submit":
                processScoreSubmission(req, resp);
                break;
            case "/officer/tender/award":
                processAward(req, resp);
                break;
            default:
                resp.sendRedirect(req.getContextPath() + "/");
        }
    }

    /* ══════════════════════════════════════════════════════════
       GET handlers
       ══════════════════════════════════════════════════════════ */

    /**
     * Displays the Evaluation Committee Member dashboard listing
     * tenders in {@code CLOSED}, {@code UNDER_EVALUATION}, and
     * {@code EVALUATED} statuses.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    private void showEvalDashboard(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(req, resp, User.Role.EVAL_COMMITTEE)) {
            return;
        }
        User evaluator = SessionUtil.getLoggedInUser(req);

        // Show tenders that are ready for evaluation
        List<Tender> closedTenders    = tenderDAO.findByStatus(Tender.Status.CLOSED);
        List<Tender> underEvalTenders = tenderDAO.findByStatus(Tender.Status.UNDER_EVALUATION);
        List<Tender> evaluatedTenders = tenderDAO.findByStatus(Tender.Status.EVALUATED);

        req.setAttribute("closedTenders",    closedTenders);
        req.setAttribute("underEvalTenders", underEvalTenders);
        req.setAttribute("evaluatedTenders", evaluatedTenders);
        req.getRequestDispatcher(VIEW_EVAL_DASHBOARD).forward(req, resp);
    }

    /**
     * Displays the scoring panel for an evaluator (committee member or
     * procurement officer). Lists all bids for the tender and shows
     * whether the current evaluator has already scored each one.
     *
     * <p>Other evaluators' individual scores are never exposed on this
     * page — only the officer's evaluation panel shows consolidated
     * results after all scores are in.</p>
     *
     * @param req  the HTTP request containing {@code tenderId}
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    private void showScoringPanel(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User evaluator = SessionUtil.getLoggedInUser(req);

        // Both officers and committee members can access scoring panel
        if (evaluator.getRole() != User.Role.EVAL_COMMITTEE
                && evaluator.getRole() != User.Role.PROCUREMENT_OFFICER) {
            SessionUtil.redirectToLogin(req, resp, "Access Denied.");
            return;
        }

        int tenderId = parseId(req.getParameter("tenderId"));
        Tender tender = tenderDAO.findById(tenderId);

        if (tender == null
                || (tender.getStatus() != Tender.Status.UNDER_EVALUATION
                && tender.getStatus() != Tender.Status.EVALUATED)) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "This tender is not currently under evaluation.");
            resp.sendRedirect(req.getContextPath() + "/eval/dashboard");
            return;
        }

        List<Bid> bids = bidDAO.findByTenderId(tenderId);

        // For each bid, mark whether this evaluator has scored it
        for (Bid bid : bids) {
            boolean scored = evalDAO.hasEvaluatorScoredBid(bid.getBidId(), evaluator.getUserId());
            bid.setHasBeenScored(scored);
        }

        // Flash messages from session
        String successMsg = (String) req.getSession().getAttribute(SessionUtil.ATTR_SUCCESS);
        String errorMsg   = (String) req.getSession().getAttribute(SessionUtil.ATTR_ERROR);
        if (successMsg != null) {
            req.setAttribute(SessionUtil.ATTR_SUCCESS, successMsg);
            req.getSession().removeAttribute(SessionUtil.ATTR_SUCCESS);
        }
        if (errorMsg != null) {
            req.setAttribute(SessionUtil.ATTR_ERROR, errorMsg);
            req.getSession().removeAttribute(SessionUtil.ATTR_ERROR);
        }

        req.setAttribute("tender",    tender);
        req.setAttribute("bids",      bids);
        req.setAttribute("evaluator", evaluator);
        req.getRequestDispatcher(VIEW_SCORING_PANEL).forward(req, resp);
    }

    /**
     * Displays the officer's evaluation panel.
     *
     * <p>Before the tender reaches {@code EVALUATED} status, procurement
     * officers see the scoring panel (same as committee members) because
     * they must submit their own scores. Once the tender is
     * {@code EVALUATED} or {@code AWARDED}, the ranked leaderboard and
     * award form are shown.</p>
     *
     * <p>An auto-transition check runs on every view — if all evaluators
     * have scored all bids but the status hasn't been updated yet, it is
     * corrected here.</p>
     *
     * @param req  the HTTP request containing {@code tenderId}
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    private void showOfficerEvalPanel(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(req, resp, User.Role.PROCUREMENT_OFFICER)) {
            return;
        }

        int tenderId = parseId(req.getParameter("tenderId"));
        Tender tender = tenderDAO.findById(tenderId);

        if (tender == null) {
            resp.sendRedirect(req.getContextPath() + "/officer/tenders");
            return;
        }

        // ── Auto-transition check on every panel view ───────────────────────
        // This handles tenders where all scores were submitted BEFORE the
        // auto-transition code was deployed, or where the score-submission
        // trigger missed the transition for any reason.
        // Safe to call repeatedly — updateStatus is idempotent for same status.
        if (tender.getStatus() == Tender.Status.UNDER_EVALUATION) {
            List<Bid> checkBids = bidDAO.findByTenderId(tenderId);
            if (!checkBids.isEmpty()
                    && evalDAO.allEvaluatorsHaveScoredAllBids(tenderId, checkBids.size())) {
                tenderDAO.updateStatus(tenderId, Tender.Status.EVALUATED);
                // Reload tender so this request sees the new status
                tender = tenderDAO.findById(tenderId);
                LOGGER.info("Tender " + tenderId
                        + " transitioned to EVALUATED on panel view — all scores complete.");
            }
        }

        // ── BRANCH: Not yet EVALUATED → show scoring panel ──────────────────
        // Officers must submit their own scores before seeing the leaderboard.
        // This enforces the spec rule: "An evaluator cannot see another
        // evaluator's individual scores until they have submitted their own."
        if (tender.getStatus() == Tender.Status.UNDER_EVALUATION) {
            // Forward to the same scoring panel the committee uses
            User officer = SessionUtil.getLoggedInUser(req);
            List<Bid> bids = bidDAO.findByTenderId(tenderId);

            for (Bid bid : bids) {
                boolean scored = evalDAO.hasEvaluatorScoredBid(bid.getBidId(), officer.getUserId());
                bid.setHasBeenScored(scored);
            }

            // Flash messages
            String successMsg = (String) req.getSession().getAttribute(SessionUtil.ATTR_SUCCESS);
            String errorMsg   = (String) req.getSession().getAttribute(SessionUtil.ATTR_ERROR);
            if (successMsg != null) {
                req.setAttribute(SessionUtil.ATTR_SUCCESS, successMsg);
                req.getSession().removeAttribute(SessionUtil.ATTR_SUCCESS);
            }
            if (errorMsg != null) {
                req.setAttribute(SessionUtil.ATTR_ERROR, errorMsg);
                req.getSession().removeAttribute(SessionUtil.ATTR_ERROR);
            }

            req.setAttribute("tender",    tender);
            req.setAttribute("bids",      bids);
            req.setAttribute("evaluator", officer);
            req.getRequestDispatcher(VIEW_SCORING_PANEL).forward(req, resp);
            return;
        }

        // ── BRANCH: EVALUATED or AWARDED → show ranked leaderboard ─────────
        List<Bid> bids = bidDAO.findByTenderId(tenderId);

        // Calculate final score for each bid (average of all evaluators)
        List<EvaluationScore> allScores = evalDAO.findAllScoresForTender(tenderId);

        // Group scores by bidId
        Map<Integer, List<EvaluationScore>> scoresByBid = new HashMap<>();
        for (EvaluationScore es : allScores) {
            scoresByBid.computeIfAbsent(es.getBidId(), k -> new ArrayList<>()).add(es);
        }

        // Attach final score to each bid
        for (Bid bid : bids) {
            List<EvaluationScore> bidScores = scoresByBid.getOrDefault(bid.getBidId(), new ArrayList<>());
            if (!bidScores.isEmpty()) {
                BigDecimal finalScore = EvaluationService.calculateFinalScore(bidScores);
                bid.setFinalScore(finalScore.doubleValue());
            }
        }

        // Sort by final score descending (ranked leaderboard)
        bids.sort((a, b) -> {
            Double fa = a.getFinalScore() != null ? a.getFinalScore() : 0.0;
            Double fb = b.getFinalScore() != null ? b.getFinalScore() : 0.0;
            return Double.compare(fb, fa); // descending
        });

        // Assign rank numbers
        for (int i = 0; i < bids.size(); i++) {
            bids.get(i).setRank(i + 1);
        }

        // Check if an award notice already exists
        AwardNotice existingAward = awardDAO.findByTenderId(tenderId);

        // Flash messages
        String successMsg = (String) req.getSession().getAttribute(SessionUtil.ATTR_SUCCESS);
        String errorMsg   = (String) req.getSession().getAttribute(SessionUtil.ATTR_ERROR);
        if (successMsg != null) {
            req.setAttribute(SessionUtil.ATTR_SUCCESS, successMsg);
            req.getSession().removeAttribute(SessionUtil.ATTR_SUCCESS);
        }
        if (errorMsg != null) {
            req.setAttribute(SessionUtil.ATTR_ERROR, errorMsg);
            req.getSession().removeAttribute(SessionUtil.ATTR_ERROR);
        }

        req.setAttribute("tender",        tender);
        req.setAttribute("rankedBids",    bids);
        req.setAttribute("allScores",     scoresByBid);
        req.setAttribute("existingAward", existingAward);
        req.getRequestDispatcher(VIEW_EVAL_PANEL).forward(req, resp);
    }

    /* ══════════════════════════════════════════════════════════
       POST handlers
       ══════════════════════════════════════════════════════════ */

    /**
     * Processes score submission from an evaluator (committee member or
     * procurement officer) for a single bid.
     *
     * <p>Flow:
     * <ol>
     *   <li>Validate evaluator has not already scored this bid.</li>
     *   <li>Validate technical compliance score is in range 0–100.</li>
     *   <li>Delegate all calculations (price score, timeline score,
     *       weighted total) to {@link EvaluationService#buildScore}.</li>
     *   <li>Persist the score record via {@link EvalDAO#insertScore}.</li>
     *   <li>Check if ALL appointed evaluators have scored ALL bids —
     *       if so, auto-transition the tender to {@code EVALUATED}.</li>
     * </ol>
     * </p>
     *
     * @param req  the HTTP request containing {@code bidId},
     *             {@code tenderId}, and {@code technicalScore}
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    private void processScoreSubmission(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User evaluator = SessionUtil.getLoggedInUser(req);
        if (evaluator.getRole() != User.Role.EVAL_COMMITTEE
                && evaluator.getRole() != User.Role.PROCUREMENT_OFFICER) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        int    bidId     = parseId(req.getParameter("bidId"));
        int    tenderId  = parseId(req.getParameter("tenderId"));
        String techStr   = sanitise(req.getParameter("technicalScore"));

        // Guard: already scored?
        if (evalDAO.hasEvaluatorScoredBid(bidId, evaluator.getUserId())) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "You have already submitted scores for this bid.");
            resp.sendRedirect(req.getContextPath() + "/eval/panel?tenderId=" + tenderId);
            return;
        }

        // Validate technical score
        BigDecimal technicalScore;
        try {
            technicalScore = new BigDecimal(techStr);
            if (technicalScore.compareTo(BigDecimal.ZERO) < 0
                    || technicalScore.compareTo(new BigDecimal("100")) > 0) {
                throw new NumberFormatException("Out of range");
            }
        } catch (NumberFormatException e) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "Technical score must be a number between 0 and 100.");
            resp.sendRedirect(req.getContextPath() + "/eval/panel?tenderId=" + tenderId);
            return;
        }

        // Load the target bid and all bids for this tender
        Bid targetBid = bidDAO.findById(bidId);
        if (targetBid == null) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR, "Bid not found.");
            resp.sendRedirect(req.getContextPath() + "/eval/panel?tenderId=" + tenderId);
            return;
        }

        List<Bid> allBids = bidDAO.findByTenderId(tenderId);

        // Delegate ALL calculations to EvaluationService
        EvaluationScore score = EvaluationService.buildScore(
                targetBid, allBids, technicalScore, evaluator.getUserId());

        int scoreId = evalDAO.insertScore(score);
        if (scoreId == -1) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "Failed to save score. Please try again.");
            resp.sendRedirect(req.getContextPath() + "/eval/panel?tenderId=" + tenderId);
            return;
        }

        LOGGER.info("Score saved: bidId=" + bidId + " evaluatorId=" + evaluator.getUserId()
                + " weightedTotal=" + score.getWeightedTotal());

        // Auto-transition to EVALUATED if all evaluators have scored all bids
        boolean allDone = evalDAO.allEvaluatorsHaveScoredAllBids(tenderId, allBids.size());
        if (allDone) {
            tenderDAO.updateStatus(tenderId, Tender.Status.EVALUATED);
            LOGGER.info("Tender " + tenderId + " auto-transitioned to EVALUATED.");
            req.getSession().setAttribute(SessionUtil.ATTR_SUCCESS,
                    "Score submitted. All evaluators are done — tender has moved to Evaluated.");
        } else {
            req.getSession().setAttribute(SessionUtil.ATTR_SUCCESS,
                    "Score submitted successfully.");
        }

        resp.sendRedirect(req.getContextPath() + "/eval/panel?tenderId=" + tenderId);
    }

    /**
     * Processes the contract award form submission.
     *
     * <p>Flow:
     * <ol>
     *   <li>Validate officer role and that the tender is in
     *       {@code EVALUATED} status.</li>
     *   <li>Validate that an award justification has been provided.</li>
     *   <li>System automatically selects the highest-scoring bid (the
     *       officer does not manually choose).</li>
     *   <li>Persist the award notice via {@link AwardDAO#insertAwardNotice}.</li>
     *   <li>Update all bid statuses: winning → {@code AWARDED},
     *       others → {@code NOT_AWARDED}.</li>
     *   <li>Transition tender to {@code AWARDED}.</li>
     *   <li>Fire-and-forget email notifications to all bidding suppliers
     *       on a daemon background thread so the officer's UI is not
     *       blocked.</li>
     *   <li>Redirect to the officer evaluation panel with a success
     *       message.</li>
     * </ol>
     * </p>
     *
     * @param req  the HTTP request containing {@code tenderId} and
     *             {@code justification}
     * @param resp the HTTP response
     * @throws IOException      if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    private void processAward(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        if (!SessionUtil.requireRole(req, resp, User.Role.PROCUREMENT_OFFICER)) {
            return;
        }

        User officer         = SessionUtil.getLoggedInUser(req);
        int  tenderId        = parseId(req.getParameter("tenderId"));
        String justification = sanitise(req.getParameter("justification"));

        Tender tender = tenderDAO.findById(tenderId);

        // Validate tender state
        if (tender == null || tender.getStatus() != Tender.Status.EVALUATED) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "Award can only be made for tenders in Evaluated status.");
            resp.sendRedirect(req.getContextPath() + "/officer/eval/panel?tenderId=" + tenderId);
            return;
        }

        // Validate justification
        if (justification.isEmpty()) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "Please provide an award justification before confirming.");
            resp.sendRedirect(req.getContextPath() + "/officer/eval/panel?tenderId=" + tenderId);
            return;
        }

        // ── System automatically selects the highest-scoring bid ──────────────
        // The officer never chooses — the system determines the winner from scores.
        List<Bid> bids = bidDAO.findByTenderId(tenderId);
        List<EvaluationScore> allScores = evalDAO.findAllScoresForTender(tenderId);

        // Group scores by bidId
        Map<Integer, List<EvaluationScore>> scoresByBid = new HashMap<>();
        for (EvaluationScore es : allScores) {
            scoresByBid.computeIfAbsent(es.getBidId(), k -> new ArrayList<>()).add(es);
        }

        // Calculate final score for each bid and find the highest
        Bid winningBid   = null;
        BigDecimal topScore = BigDecimal.ZERO;

        for (Bid bid : bids) {
            List<EvaluationScore> bidScores = scoresByBid.getOrDefault(bid.getBidId(), new ArrayList<>());
            if (!bidScores.isEmpty()) {
                BigDecimal score = EvaluationService.calculateFinalScore(bidScores);
                if (score.compareTo(topScore) > 0) {
                    topScore   = score;
                    winningBid = bid;
                }
            }
        }

        if (winningBid == null) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "Cannot award — no bids have evaluation scores. "
                    + "Ensure all evaluators have submitted scores.");
            resp.sendRedirect(req.getContextPath() + "/officer/eval/panel?tenderId=" + tenderId);
            return;
        }

        int        winBidId     = winningBid.getBidId();
        BigDecimal awardedValue = winningBid.getBidAmount();

        LOGGER.info("System selected winning bid: bidId=" + winBidId
                + " score=" + topScore + " for tenderId=" + tenderId);

        // Build and persist award notice
        AwardNotice notice = new AwardNotice();
        notice.setTenderId(tenderId);
        notice.setWinningBidId(winBidId);
        notice.setAwardedBy(officer.getUserId());
        notice.setAwardedValue(awardedValue);
        notice.setJustification(justification);

        int awardId = awardDAO.insertAwardNotice(notice);
        if (awardId == -1) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "Failed to save award notice. Please try again.");
            resp.sendRedirect(req.getContextPath() + "/officer/eval/panel?tenderId=" + tenderId);
            return;
        }

        // Update bid statuses
        bidDAO.updateBidStatusesOnAward(tenderId, winBidId);

        // Transition tender to AWARDED
        tenderDAO.updateStatus(tenderId, Tender.Status.AWARDED);

        LOGGER.info("Contract awarded: tenderId=" + tenderId
                + " winningBidId=" + winBidId + " by officerId=" + officer.getUserId());

        // ── Fire-and-forget email notifications on a background thread ─────
        // Module 6: Email notifications via JavaMail API.
        // Runs on a daemon thread so the officer is NOT blocked waiting for
        // emails to send. The browser redirects immediately.
        //
        // We capture all needed data into final variables so the background
        // thread has its own copy — the request/response objects are released
        // back to the container straight away.
        final int    finalTenderId   = tenderId;
        final int    finalWinBidId   = winBidId;
        final String finalServerBase = req.getScheme() + "://" + req.getServerName()
                + ":" + req.getServerPort();
        final String finalContextPath = req.getContextPath();

        Thread emailThread = new Thread(() -> {
            try {
                // Each thread gets its own DAO instances because DAOs obtain
                // connections from the shared pool — they are thread-safe
                // as long as each DAO call uses its own connection.
                BidDAO   bgBidDAO   = new BidDAOImpl();
                AwardDAO bgAwardDAO = new AwardDAOImpl();

                List<Bid> allBidsForEmail = bgBidDAO.findByTenderId(finalTenderId);
                AwardNotice fullNotice    = bgAwardDAO.findByTenderId(finalTenderId);

                if (fullNotice != null && allBidsForEmail != null) {
                    EmailService.sendAwardNotifications(fullNotice, allBidsForEmail,
                            finalContextPath, finalServerBase);
                    LOGGER.info("Award notification emails sent for tenderId="
                            + finalTenderId + " (background thread)");
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING,
                        "Background email notification failed for tenderId="
                        + finalTenderId, e);
            }
        }, "email-award-tender-" + finalTenderId);

        emailThread.setDaemon(true);  // won't prevent Tomcat shutdown
        emailThread.start();

        // ── Redirect immediately — does NOT wait for the thread ────────────
        req.getSession().setAttribute(SessionUtil.ATTR_SUCCESS,
                "Contract awarded successfully. Award notice is now visible to all bidding suppliers. "
                + "Email notifications are being sent in the background.");
        resp.sendRedirect(req.getContextPath() + "/officer/eval/panel?tenderId=" + tenderId);
    }

    /* ── Utility helpers ─────────────────────────────────────── */

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
     * Trims and null-safe sanitise helper.
     *
     * @param val the raw string to sanitise
     * @return the trimmed string, or an empty string if {@code val} is null
     */
    private String sanitise(String val) {
        return (val == null) ? "" : val.trim();
    }
}