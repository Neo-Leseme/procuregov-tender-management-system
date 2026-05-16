package com.util;

import com.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * SessionUtil — reusable session management and role guard utility.
 *
 * <p>All protected Servlets must call one of the {@code requireRole} or
 * {@code getLoggedInUser} methods. This ensures role and session checks
 * are enforced consistently across the application — never in JSPs.</p>
 *
 * <p>Usage in a Servlet:
 * <pre>
 *   User user = SessionUtil.getLoggedInUser(request);
 *   if (user == null) {
 *       SessionUtil.redirectToLogin(request, response, "Please sign in.");
 *       return;
 *   }
 * </pre>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public final class SessionUtil {

    /** Session attribute key for the logged-in User bean. */
    public static final String SESSION_USER     = "loggedInUser";

    /** Session attribute key for the login attempt count (client-side guard). */
    public static final String SESSION_ATTEMPTS = "loginAttempts";

    /** Session attribute key for access-denied or flash error messages. */
    public static final String ATTR_ERROR       = "errorMsg";

    /** Session attribute key for success flash messages. */
    public static final String ATTR_SUCCESS     = "successMsg";

    /**
     * Private constructor — utility class, not instantiable.
     */
    private SessionUtil() {}

    /* ── Session read helpers ────────────────────────────────────── */

    /**
     * Returns the currently logged-in {@link User}, or {@code null} if no
     * valid session exists.
     *
     * @param request the current HTTP request
     * @return the User stored in session, or {@code null}
     */
    public static User getLoggedInUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object obj = session.getAttribute(SESSION_USER);
        return (obj instanceof User) ? (User) obj : null;
    }

    /**
     * Returns {@code true} if the current session contains a logged-in user.
     *
     * @param request the current HTTP request
     * @return {@code true} if a user is currently logged in
     */
    public static boolean isLoggedIn(HttpServletRequest request) {
        return getLoggedInUser(request) != null;
    }

    /* ── Session write helpers ───────────────────────────────────── */

    /**
     * Stores the authenticated user in the session and resets the attempt
     * counter. Sets the session timeout to 30 minutes.
     *
     * @param request the current HTTP request
     * @param user    the successfully authenticated user
     */
    public static void login(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER, user);
        session.setAttribute(SESSION_ATTEMPTS, 0);
        session.setMaxInactiveInterval(30 * 60); // 30-minute timeout
    }

    /**
     * Invalidates the current session, effectively logging the user out.
     *
     * @param request the current HTTP request
     */
    public static void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
    }

    /**
     * Increments the in-session login attempt counter by 1.
     *
     * @param request the current HTTP request
     * @return the new attempt count after incrementing
     */
    public static int incrementSessionAttempts(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Integer count = (Integer) session.getAttribute(SESSION_ATTEMPTS);
        int newCount = (count == null ? 0 : count) + 1;
        session.setAttribute(SESSION_ATTEMPTS, newCount);
        return newCount;
    }

    /**
     * Returns the current in-session login attempt count.
     *
     * @param request the current HTTP request
     * @return attempt count, or 0 if not set
     */
    public static int getSessionAttempts(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return 0;
        Integer count = (Integer) session.getAttribute(SESSION_ATTEMPTS);
        return count == null ? 0 : count;
    }

    /* ── Role guard helpers ──────────────────────────────────────── */

    /**
     * Checks that the current user has the required role.
     * If not logged in or lacking the role, redirects to the login page
     * with an appropriate error message and returns {@code false}.
     *
     * <p>Callers should always {@code return} immediately after calling
     * this if it returns {@code false}.</p>
     *
     * @param request  the current HTTP request
     * @param response the current HTTP response
     * @param role     the role required to access the resource
     * @return {@code true} if the user is logged in and has the correct role
     * @throws IOException if the redirect fails
     */
    public static boolean requireRole(
            HttpServletRequest request,
            HttpServletResponse response,
            User.Role role) throws IOException {

        User user = getLoggedInUser(request);
        if (user == null) {
            redirectToLogin(request, response, "Please sign in to continue.");
            return false;
        }
        if (!role.equals(user.getRole())) {
            redirectToLogin(request, response,
                "Access Denied: you do not have permission to view that page.");
            return false;
        }
        return true;
    }

    /**
     * Checks that the current user is logged in (any role).
     * Redirects to the login page if not.
     *
     * @param request  the current HTTP request
     * @param response the current HTTP response
     * @return {@code true} if a user is logged in
     * @throws IOException if the redirect fails
     */
    public static boolean requireLogin(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        if (!isLoggedIn(request)) {
            redirectToLogin(request, response, "Please sign in to continue.");
            return false;
        }
        return true;
    }

    /* ── Redirect helpers ────────────────────────────────────────── */

    /**
     * Redirects to the login page with an error message stored in the
     * session so it survives the redirect.
     *
     * @param request  the current HTTP request
     * @param response the current HTTP response
     * @param message  the message to display on the login page
     * @throws IOException if the redirect fails
     */
    public static void redirectToLogin(
            HttpServletRequest request,
            HttpServletResponse response,
            String message) throws IOException {

        HttpSession session = request.getSession(true);
        session.setAttribute(ATTR_ERROR, message);
        response.sendRedirect(request.getContextPath() + "/login");
    }

    /**
     * Redirects a logged-in user to their role-appropriate dashboard.
     *
     * @param user     the authenticated user
     * @param request  the current HTTP request
     * @param response the current HTTP response
     * @throws IOException if the redirect fails
     */
    public static void redirectToDashboard(
            User user,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        String base = request.getContextPath();
        switch (user.getRole()) {
            case PROCUREMENT_OFFICER:
                response.sendRedirect(base + "/officer/dashboard");
                break;
            case EVAL_COMMITTEE:
                response.sendRedirect(base + "/eval/dashboard");
                break;
            case SUPPLIER:
            default:
                response.sendRedirect(base + "/supplier/dashboard");
                break;
        }
    }
}