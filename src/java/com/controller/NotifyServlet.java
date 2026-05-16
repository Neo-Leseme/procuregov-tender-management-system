package com.controller;

import com.util.SessionUtil;
import com.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * NotifyServlet — reserved for future notification endpoints.
 *
 * <p>In the current architecture, award notification emails are sent
 * directly from {@link com.controller.EvalServlet#processAward} on a
 * background daemon thread. This servlet exists as a placeholder for
 * any future notification features that may require a dedicated
 * endpoint (e.g. manual resend of award notices, batch notifications,
 * or status updates).</p>
 *
 * <p>Currently mapped to {@code POST /notify/award} in {@code web.xml}
 * but simply redirects to the application root. The endpoint is
 * protected — only {@code PROCUREMENT_OFFICER} role may access it.</p>
 *
 * @author Neo Leseme
 * @version 1.0
 * @see com.controller.EvalServlet#processAward
 */
public class NotifyServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(NotifyServlet.class.getName());

    /**
     * Placeholder POST handler for the notification endpoint.
     * Currently redirects to the application root.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireRole(req, resp, User.Role.PROCUREMENT_OFFICER)) return;
        resp.sendRedirect(req.getContextPath() + "/");
    }
}