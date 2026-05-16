package com.controller;

import com.dao.UserDAO;
import com.dao.UserDAOImpl;
import com.model.Supplier;
import com.model.User;
import com.util.PasswordUtil;
import com.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * AuthServlet — handles all authentication and password-reset flows
 * for the ProcureGov tender management system.
 *
 * <p>URL mappings are declared exclusively in {@code web.xml}
 * (no {@code @WebServlet} annotation) as required by the assessment.
 * Mapped to: {@code /login}, {@code /logout}, {@code /register},
 * {@code /forgot-password}, {@code /verify-code},
 * {@code /reset-password}.
 *
 * <p>Security rules enforced:
 * <ul>
 *   <li>Passwords are compared using SHA-256 hashes — never plain text.</li>
 *   <li>After {@value #MAX_ATTEMPTS} consecutive failed logins the account
 *       is temporarily locked for 30 seconds.</li>
 *   <li>Already-logged-in users are redirected to their role dashboard.</li>
 *   <li>JSPs are never accessible directly — all views are forwarded from
 *       this servlet.</li>
 *   <li>Password reset uses a 6-digit code sent via email, valid for
 *       15 minutes.</li>
 *   <li>Supports a {@code redirect} parameter so that deep links from
 *       emails (e.g. award notices) can land on a protected page after
 *       successful login.</li>
 * </ul>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public class AuthServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AuthServlet.class.getName());
    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCKOUT_DURATION_MS = 30_000; // 30 seconds

    private static final String VIEW_LOGIN           = "/WEB-INF/views/auth/login.jsp";
    private static final String VIEW_REGISTER        = "/WEB-INF/views/auth/register.jsp";
    private static final String VIEW_FORGOT_PASSWORD = "/WEB-INF/views/auth/forgotPassword.jsp";
    private static final String VIEW_VERIFY_CODE     = "/WEB-INF/views/auth/verifyCode.jsp";
    private static final String VIEW_RESET_PASSWORD  = "/WEB-INF/views/auth/resetPassword.jsp";

    private UserDAO userDAO;

    /**
     * Initialises the servlet by creating the UserDAO instance
     * used by all authentication handlers.
     *
     * @throws ServletException if initialisation fails
     */
    @Override
    public void init() throws ServletException {
        userDAO = new UserDAOImpl();
    }

    /* ══════════════════════════════════════════════════════════════
       GET — show forms
       ══════════════════════════════════════════════════════════════ */

    /**
     * Handles GET requests for all authentication pages.
     *
     * <p>Routing:
     * <ul>
     *   <li>{@code /login}            — login form</li>
     *   <li>{@code /register}         — supplier registration form</li>
     *   <li>{@code /forgot-password}  — forgot-password form</li>
     *   <li>{@code /verify-code}      — verification-code entry</li>
     *   <li>{@code /reset-password}   — new-password form</li>
     *   <li>{@code /logout}           — destroys the session</li>
     * </ul>
     * <p>Already-authenticated users are redirected to their dashboard
     * unless they are accessing logout or password-reset pages.</p>
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();

        // If already logged in, redirect to dashboard
        // (except for logout and password reset pages which are accessible to anyone)
        if (!"/logout".equals(path)
                && !"/forgot-password".equals(path)
                && !"/verify-code".equals(path)
                && !"/reset-password".equals(path)
                && SessionUtil.isLoggedIn(req)) {
            SessionUtil.redirectToDashboard(SessionUtil.getLoggedInUser(req), req, resp);
            return;
        }

        switch (path) {
            case "/logout":
                handleLogout(req, resp);
                break;

            case "/register":
                req.getRequestDispatcher(VIEW_REGISTER).forward(req, resp);
                break;

            case "/forgot-password":
                // Flash messages from POST redirect
                flashSessionToRequest(req);
                req.getRequestDispatcher(VIEW_FORGOT_PASSWORD).forward(req, resp);
                break;

            case "/verify-code":
                showVerifyCodePage(req, resp);
                break;

            case "/reset-password":
                showResetPasswordPage(req, resp);
                break;

            case "/login":
            default:
                // Clear any stale error messages from previous role-guard redirects
                // so they don't linger on the login page after the user navigates away
                req.getSession().removeAttribute(SessionUtil.ATTR_ERROR);
                req.getSession().removeAttribute(SessionUtil.ATTR_SUCCESS);
                // Pass current attempt count so the JSP can show a warning
                req.setAttribute("loginAttempts", SessionUtil.getSessionAttempts(req));
                req.getRequestDispatcher(VIEW_LOGIN).forward(req, resp);
                break;
        }
    }

    /* ══════════════════════════════════════════════════════════════
       POST — process forms
       ══════════════════════════════════════════════════════════════ */

    /**
     * Handles POST requests for all authentication forms.
     *
     * <p>Routing:
     * <ul>
     *   <li>{@code /login}            — process login</li>
     *   <li>{@code /register}         — process registration</li>
     *   <li>{@code /forgot-password}  — send reset code</li>
     *   <li>{@code /verify-code}      — validate reset code</li>
     *   <li>{@code /reset-password}   — update password</li>
     * </ul>
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
        String path = req.getServletPath();

        switch (path) {
            case "/login":
                handleLogin(req, resp);
                break;
            case "/register":
                handleRegister(req, resp);
                break;
            case "/forgot-password":
                handleForgotPassword(req, resp);
                break;
            case "/verify-code":
                handleVerifyCode(req, resp);
                break;
            case "/reset-password":
                handleResetPassword(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    /* ══════════════════════════════════════════════════════════════
       Private handlers — Authentication
       ══════════════════════════════════════════════════════════════ */

    /**
     * Processes the login form submission.
     *
     * <p>Flow:
     * <ol>
     *   <li>Check if account is locked (30-second timer) — reject if so.</li>
     *   <li>Look up user by email.</li>
     *   <li>Verify SHA-256 password hash via {@link PasswordUtil#verify}.</li>
     *   <li>On success: reset attempts, create session, redirect to
     *       dashboard (or to a redirect target if one was supplied,
     *       e.g. from an award notification email).</li>
     *   <li>On failure: increment attempts; lock for 30 seconds after
     *       {@value #MAX_ATTEMPTS} consecutive failures.</li>
     * </ol>
     * </p>
     *
     * @param req  the HTTP request containing {@code email},
     *             {@code password}, and optional {@code redirect}
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    private void handleLogin(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email    = sanitise(req.getParameter("email"));
        String password = req.getParameter("password");
        String redirect = sanitise(req.getParameter("redirect"));

        // ── 30-second timer lockout check ───────────────────────
        int sessionAttempts = SessionUtil.getSessionAttempts(req);
        if (sessionAttempts >= MAX_ATTEMPTS) {
            Long lockedAt = (Long) req.getSession().getAttribute("lockoutTime");
            if (lockedAt != null) {
                long elapsed = System.currentTimeMillis() - lockedAt;
                if (elapsed < LOCKOUT_DURATION_MS) {
                    long remaining = (LOCKOUT_DURATION_MS - elapsed) / 1000 + 1;
                    req.setAttribute(SessionUtil.ATTR_ERROR,
                        "Account temporarily locked. Please wait " + remaining
                        + " seconds before trying again.");
                    req.setAttribute("loginAttempts", sessionAttempts);
                    req.getRequestDispatcher(VIEW_LOGIN).forward(req, resp);
                    return;
                } else {
                    // 30 seconds have passed — auto-reset the lockout
                    req.getSession().setAttribute(SessionUtil.SESSION_ATTEMPTS, 0);
                    req.getSession().removeAttribute("lockoutTime");
                    sessionAttempts = 0;
                }
            }
        }

        // ── Basic validation ─────────────────────────────────────
        if (email.isEmpty() || password == null || password.isEmpty()) {
            req.setAttribute(SessionUtil.ATTR_ERROR, "Email and password are required.");
            req.setAttribute("loginAttempts", sessionAttempts);
            req.getRequestDispatcher(VIEW_LOGIN).forward(req, resp);
            return;
        }

        // ── Look up user ─────────────────────────────────────────
        User user = userDAO.findByEmail(email);

        if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
            // Wrong credentials
            int newCount = SessionUtil.incrementSessionAttempts(req);
            // Only update the database login_attempts table if the user
            // actually exists. If the email doesn't match any account,
            // passing userId=0 would violate the foreign key constraint.
            if (user != null) {
                userDAO.incrementLoginAttempts(user.getUserId());
            }

            String msg;
            if (newCount >= MAX_ATTEMPTS) {
                // Lock for 30 seconds — record the lockout time
                req.getSession().setAttribute("lockoutTime", System.currentTimeMillis());
                msg = "Account temporarily locked for 30 seconds. Please wait.";
            } else {
                msg = "Invalid email or password. Attempt " + newCount + " of " + MAX_ATTEMPTS + ".";
            }

            req.setAttribute(SessionUtil.ATTR_ERROR, msg);
            req.setAttribute("loginAttempts", newCount);
            LOGGER.warning("Failed login attempt for email=" + email + " (attempt " + newCount + ")");
            req.getRequestDispatcher(VIEW_LOGIN).forward(req, resp);
            return;
        }

        // ── Check if account is DB-locked ────────────────────────
        if (user.isLocked()) {
            req.setAttribute(SessionUtil.ATTR_ERROR,
                    "This account has been locked. Please contact help.procuregov@gov.ls.");
            req.setAttribute("loginAttempts", sessionAttempts);
            req.getRequestDispatcher(VIEW_LOGIN).forward(req, resp);
            return;
        }

        // ── Success ──────────────────────────────────────────────
        userDAO.resetLoginAttempts(user.getUserId());
        // Clear any lockout state from the session
        req.getSession().removeAttribute("lockoutTime");
        SessionUtil.login(req, user);
        LOGGER.info("User logged in: " + user.getEmail() + " role=" + user.getRole());

        // If a redirect parameter was supplied (e.g. from an award notification
        // email), send the user there after login. Otherwise go to dashboard.
        if (!redirect.isEmpty()) {
            LOGGER.info("Redirecting after login to: " + redirect);
            resp.sendRedirect(req.getContextPath() + "/" + redirect);
        } else {
            SessionUtil.redirectToDashboard(user, req, resp);
        }
    }

    /**
     * Processes the supplier registration form.
     *
     * <p>Flow:
     * <ol>
     *   <li>Validate all fields server-side.</li>
     *   <li>Check email uniqueness.</li>
     *   <li>Hash password with SHA-256 via {@link PasswordUtil#hash}.</li>
     *   <li>Insert user row, then supplier row.</li>
     *   <li>Redirect to login with a success message (POST-Redirect-GET).</li>
     * </ol>
     * </p>
     *
     * @param req  the HTTP request containing registration fields
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    private void handleRegister(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // ── Collect fields ────────────────────────────────────────
        String companyName      = sanitise(req.getParameter("companyName"));
        String physicalAddress  = sanitise(req.getParameter("physicalAddress"));
        String contactNumber    = sanitise(req.getParameter("contactNumber"));
        String email            = sanitise(req.getParameter("email"));
        String password         = req.getParameter("password");
        String confirmPassword  = req.getParameter("confirmPassword");

        // ── Server-side validation ────────────────────────────────
        if (companyName.isEmpty() || physicalAddress.isEmpty()
                || contactNumber.isEmpty() || email.isEmpty()
                || password == null || password.isEmpty()
                || confirmPassword == null || confirmPassword.isEmpty()) {
            setError(req, "All fields are mandatory. Please complete the form.");
            req.getRequestDispatcher(VIEW_REGISTER).forward(req, resp);
            return;
        }

        if (!password.equals(confirmPassword)) {
            setError(req, "Passwords do not match. Please re-enter.");
            req.getRequestDispatcher(VIEW_REGISTER).forward(req, resp);
            return;
        }

        if (password.length() < 8) {
            setError(req, "Password must be at least 8 characters long.");
            req.getRequestDispatcher(VIEW_REGISTER).forward(req, resp);
            return;
        }

        // ── Uniqueness check ──────────────────────────────────────
        if (userDAO.emailExists(email)) {
            setError(req, "An account with that email address already exists. Please sign in.");
            req.getRequestDispatcher(VIEW_REGISTER).forward(req, resp);
            return;
        }

        // ── Build and persist User ────────────────────────────────
        User newUser = new User();
        newUser.setFullName(companyName);
        newUser.setEmail(email);
        newUser.setPasswordHash(PasswordUtil.hash(password));
        newUser.setRole(User.Role.SUPPLIER);

        int userId = userDAO.insertUser(newUser);
        if (userId == -1) {
            LOGGER.severe("Failed to insert user during registration for email=" + email);
            setError(req, "Registration failed due to a system error. Please try again later.");
            req.getRequestDispatcher(VIEW_REGISTER).forward(req, resp);
            return;
        }

        // ── Build and persist Supplier ────────────────────────────
        Supplier supplier = new Supplier();
        supplier.setUserId(userId);
        supplier.setCompanyName(companyName);
        supplier.setRegistrationNo(userDAO.generateSupplierRegNo());
        supplier.setPhysicalAddress(physicalAddress);
        supplier.setContactNumber(contactNumber);

        int supplierId = userDAO.insertSupplier(supplier);
        if (supplierId == -1) {
            LOGGER.severe("Failed to insert supplier row for userId=" + userId);
            setError(req, "Registration failed during supplier setup. Please contact support.");
            req.getRequestDispatcher(VIEW_REGISTER).forward(req, resp);
            return;
        }

        LOGGER.info("New supplier registered: " + email + " supplierId=" + supplierId);

        // ── POST-Redirect-GET: redirect to login with success ─────
        req.getSession(true).setAttribute(SessionUtil.ATTR_SUCCESS,
                "Registration successful! Welcome to ProcureGov. Please sign in.");
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    /**
     * Invalidates the current HTTP session and redirects to the login page.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleLogout(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        SessionUtil.logout(req);
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    /* ══════════════════════════════════════════════════════════════
       Private handlers — Password Reset
       ══════════════════════════════════════════════════════════════ */

    /**
     * Processes the forgot-password form — generates a random 6-digit
     * reset code and emails it to the user on a background thread.
     *
     * <p>Always returns a generic success message regardless of whether
     * the email address exists in the database. This prevents attackers
     * from enumerating registered accounts.</p>
     *
     * @param req  the HTTP request containing {@code email}
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    private void handleForgotPassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = sanitise(req.getParameter("email"));

        if (email.isEmpty()) {
            req.setAttribute(SessionUtil.ATTR_ERROR,
                    "Please enter your registered email address.");
            req.getRequestDispatcher(VIEW_FORGOT_PASSWORD).forward(req, resp);
            return;
        }

        User user = userDAO.findByEmail(email);

        if (user != null) {
            // Generate a random 6-digit code
            String code = String.format("%06d", (int) (Math.random() * 1_000_000));

            // Expires in 15 minutes — calculated in UTC to match MySQL server timezone.
            java.sql.Timestamp expiresAt = java.sql.Timestamp.from(
                    ZonedDateTime.now(ZoneOffset.UTC)
                            .plusMinutes(15)
                            .toInstant()
            );

            userDAO.storeResetCode(user.getUserId(), code, expiresAt);

            // Build server URL for email
            String serverBase = req.getScheme() + "://" + req.getServerName()
                    + ":" + req.getServerPort();
            String contextPath = req.getContextPath();

            // Fire-and-forget on a daemon thread — don't block the user
            final String finalEmail = email;
            final String finalName  = user.getFullName();
            final String finalCode  = code;
            final String finalBase  = serverBase;
            final String finalCtx   = contextPath;

            new Thread(() -> {
                com.util.EmailService.sendPasswordResetCode(
                        finalEmail, finalName, finalCode, finalCtx, finalBase);
            }, "pwd-reset-email").start();

            LOGGER.info("Password reset code generated for userId=" + user.getUserId());
        }

        // Always show the same message — prevents email enumeration
        req.getSession().setAttribute(SessionUtil.ATTR_SUCCESS,
                "If that email address is registered, a verification code has been sent. "
                + "Please check your inbox and spam folder.");
        resp.sendRedirect(req.getContextPath() + "/forgot-password");
    }

    /**
     * Displays the verification-code entry page.
     *
     * @param req  the HTTP request containing {@code email}
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    private void showVerifyCodePage(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = sanitise(req.getParameter("email"));

        if (email.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/forgot-password");
            return;
        }

        flashSessionToRequest(req);
        req.setAttribute("resetEmail", email);
        req.getRequestDispatcher(VIEW_VERIFY_CODE).forward(req, resp);
    }

    /**
     * Validates the 6-digit reset code submitted by the user.
     *
     * @param req  the HTTP request containing {@code email} and {@code code}
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    private void handleVerifyCode(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = sanitise(req.getParameter("email"));
        String code  = sanitise(req.getParameter("code"));

        if (email.isEmpty() || code.isEmpty()) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "Please enter the verification code sent to your email.");
            resp.sendRedirect(req.getContextPath() + "/verify-code?email=" + email);
            return;
        }

        if (code.length() != 6 || !code.matches("\\d{6}")) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "Please enter a valid 6-digit code.");
            resp.sendRedirect(req.getContextPath() + "/verify-code?email=" + email);
            return;
        }

        User user = userDAO.findByEmail(email);

        if (user == null) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "Invalid or expired code. Please request a new one.");
            resp.sendRedirect(req.getContextPath() + "/forgot-password");
            return;
        }

        if (!userDAO.verifyResetCode(user.getUserId(), code)) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "Invalid or expired code. Please request a new one.");
            resp.sendRedirect(req.getContextPath() + "/verify-code?email=" + email);
            return;
        }

        // Code is valid — forward to reset password page
        req.setAttribute("resetEmail", email);
        req.setAttribute("resetCode", code);
        req.getRequestDispatcher(VIEW_RESET_PASSWORD).forward(req, resp);
    }

    /**
     * Displays the new-password form.
     *
     * @param req  the HTTP request containing {@code email} and {@code code}
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    private void showResetPasswordPage(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = sanitise(req.getParameter("email"));
        String code  = sanitise(req.getParameter("code"));

        if (email.isEmpty() || code.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/forgot-password");
            return;
        }

        flashSessionToRequest(req);
        req.setAttribute("resetEmail", email);
        req.setAttribute("resetCode", code);
        req.getRequestDispatcher(VIEW_RESET_PASSWORD).forward(req, resp);
    }

    /**
     * Processes the reset-password form.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    private void handleResetPassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email           = sanitise(req.getParameter("email"));
        String code            = sanitise(req.getParameter("code"));
        String password        = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        if (email.isEmpty() || code.isEmpty()
                || password == null || password.isEmpty()
                || confirmPassword == null || confirmPassword.isEmpty()) {
            req.setAttribute(SessionUtil.ATTR_ERROR, "All fields are required.");
            req.setAttribute("resetEmail", email);
            req.setAttribute("resetCode", code);
            req.getRequestDispatcher(VIEW_RESET_PASSWORD).forward(req, resp);
            return;
        }

        if (!password.equals(confirmPassword)) {
            req.setAttribute(SessionUtil.ATTR_ERROR,
                    "Passwords do not match. Please re-enter.");
            req.setAttribute("resetEmail", email);
            req.setAttribute("resetCode", code);
            req.getRequestDispatcher(VIEW_RESET_PASSWORD).forward(req, resp);
            return;
        }

        if (password.length() < 8) {
            req.setAttribute(SessionUtil.ATTR_ERROR,
                    "Password must be at least 8 characters long.");
            req.setAttribute("resetEmail", email);
            req.setAttribute("resetCode", code);
            req.getRequestDispatcher(VIEW_RESET_PASSWORD).forward(req, resp);
            return;
        }

        User user = userDAO.findByEmail(email);

        if (user == null || !userDAO.verifyResetCode(user.getUserId(), code)) {
            req.getSession().setAttribute(SessionUtil.ATTR_ERROR,
                    "Invalid or expired reset session. Please start again.");
            resp.sendRedirect(req.getContextPath() + "/forgot-password");
            return;
        }

        String newHash = PasswordUtil.hash(password);
        userDAO.updatePassword(user.getUserId(), newHash);
        userDAO.markResetCodeUsed(user.getUserId(), code);

        LOGGER.info("Password reset successful for userId=" + user.getUserId());

        req.getSession().setAttribute(SessionUtil.ATTR_SUCCESS,
                "Password reset successful! Please sign in with your new password.");
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    /* ── Utility helpers ─────────────────────────────────────────── */

    /**
     * Trims and null-safe sanitise helper.
     *
     * @param value the raw string to sanitise
     * @return the trimmed string, or an empty string if {@code value} is null
     */
    private String sanitise(String value) {
        return (value == null) ? "" : value.trim();
    }

    /**
     * Sets the {@code errorMsg} request attribute.
     *
     * @param req     the HTTP request
     * @param message the error message to display
     */
    private void setError(HttpServletRequest req, String message) {
        req.setAttribute(SessionUtil.ATTR_ERROR, message);
    }

    /**
     * Moves success/error flash messages from session scope to request
     * scope so JSPs can display them without checking {@code sessionScope}.
     *
     * @param req the HTTP request
     */
    private void flashSessionToRequest(HttpServletRequest req) {
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
    }
}