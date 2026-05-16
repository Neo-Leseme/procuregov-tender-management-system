package com.util;

import com.model.AwardNotice;
import com.model.Bid;

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EmailService — sends award notification and password reset emails via the
 * JavaMail API (Module 6).
 *
 * <p>
 * Uses fully-qualified {@code javax.mail} class names (no wildcard imports) to
 * avoid compile errors caused by sources JARs in {@code WEB-INF/lib}.</p>
 *
 * <p>
 * <strong>SMTP setup:</strong> configure mail using environment variables or
 * JVM system properties. Do not hard-code real passwords in source control.
 * Required keys: {@code PROCUREGOV_SMTP_USER} and
 * {@code PROCUREGOV_SMTP_PASSWORD}. Optional keys:
 * {@code PROCUREGOV_SMTP_HOST}, {@code PROCUREGOV_SMTP_PORT}, and
 * {@code PROCUREGOV_FROM_NAME}.
 *
 * @author Neo Leseme
 * @version 1.0
 */
public final class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    /* ── SMTP configuration. Values are read from environment variables
       first, then JVM system properties. No real credentials are committed. */
    private static final String SMTP_HOST = config("PROCUREGOV_SMTP_HOST", "smtp.gmail.com");
    private static final String SMTP_PORT = config("PROCUREGOV_SMTP_PORT", "587");
    private static final String SMTP_USER = config("PROCUREGOV_SMTP_USER", "");
    private static final String SMTP_PASSWORD = config("PROCUREGOV_SMTP_PASSWORD", "");
    private static final String FROM_NAME = config("PROCUREGOV_FROM_NAME",
            "ProcureGov — Ministry of Public Works");

    /**
     * Private constructor — utility class, not instantiable.
     */
    private EmailService() {
    }

    /**
     * Sends a notification to all registered suppliers when a new tender is
     * published. Runs on a background daemon thread from the calling servlet so
     * the officer's UI is never blocked.
     *
     * @param tenderTitle the title of the newly published tender
     * @param tenderRef the reference number (e.g. MPW-2026-0003)
     * @param category the tender category label
     * @param closingDate formatted closing date string
     * @param supplierEmails list of supplier email addresses
     * @param supplierNames parallel list of supplier company names
     * @param contextPath application context path
     * @param serverBase server base URL
     */
    public static void sendNewTenderNotification(
            String tenderTitle, String tenderRef, String category,
            String closingDate,
            java.util.List<String> supplierEmails,
            java.util.List<String> supplierNames,
            String contextPath, String serverBase) {

        if (supplierEmails == null || supplierEmails.isEmpty()) {
            return;
        }

        String loginUrl = serverBase + contextPath + "/login";
        String subject = "ProcureGov — New Tender Published: " + tenderRef;

        for (int i = 0; i < supplierEmails.size(); i++) {
            String email = supplierEmails.get(i);
            String name = supplierNames.get(i);
            if (email == null || email.trim().isEmpty()) {
                continue;
            }

            String body = "<!DOCTYPE html><html><head><meta charset='UTF-8'/></head>"
                    + "<body style='margin:0;padding:0;background:#0d1117;font-family:Arial,sans-serif;'>"
                    + "<div style='max-width:560px;margin:32px auto;background:#131a24;"
                    + "border-radius:12px;overflow:hidden;border:1px solid rgba(201,168,76,0.2);'>"
                    + "<div style='background:#1a2333;padding:24px 32px;border-bottom:2px solid #c9a84c;'>"
                    + "<h2 style='margin:0;color:#c9a84c;font-size:18px;'>ProcureGov</h2>"
                    + "<p style='margin:4px 0 0;color:#a09880;font-size:12px;'>"
                    + "Ministry of Public Works — Kingdom of Lesotho</p></div>"
                    + "<div style='padding:32px;'>"
                    + "<h3 style='color:#f0ebe0;margin:0 0 6px;font-size:16px;'>New Tender Published</h3>"
                    + "<p style='color:#a09880;font-size:13px;margin:0 0 20px;'>"
                    + "A new government tender has been published for bidding.</p>"
                    + "<p style='color:#c2bdb0;font-size:14px;'>Dear "
                    + "<strong style='color:#f0ebe0;'>" + name + "</strong>,</p>"
                    + "<div style='background:#1a2333;border-radius:8px;padding:18px;margin:18px 0;"
                    + "border-left:4px solid #c9a84c;'>"
                    + "<p style='margin:0 0 6px;font-weight:bold;color:#f0ebe0;font-size:15px;'>"
                    + tenderTitle + "</p>"
                    + "<p style='margin:0;color:#a09880;font-size:13px;line-height:1.6;'>"
                    + "<strong>Reference:</strong> " + tenderRef + "<br/>"
                    + "<strong>Category:</strong> " + category + "<br/>"
                    + "<strong>Closing Date:</strong> " + closingDate + "</p></div>"
                    + "<div style='text-align:center;margin:24px 0;'>"
                    + "<a href='" + loginUrl + "' style='display:inline-block;background:#c9a84c;"
                    + "color:#0d1117;padding:12px 28px;border-radius:6px;text-decoration:none;"
                    + "font-weight:bold;font-size:14px;'>View Tender Details</a></div>"
                    + "<hr style='border:none;border-top:1px solid rgba(255,255,255,0.07);margin:20px 0;'/>"
                    + "<p style='color:#5a5448;font-size:11px;line-height:1.6;'>"
                    + "Automated notification from ProcureGov. "
                    + "For queries: <a href='mailto:help.procuregov@gov.ls' style='color:#c9a84c;'>"
                    + "help.procuregov@gov.ls</a></p>"
                    + "</div></div></body></html>";

            try {
                sendEmail(email, subject, body);
                LOGGER.info("New tender notification sent → " + email);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to send new tender email to " + email, e);
            }
        }
    }

    /**
     * Sends a notification to all Evaluation Committee members when a tender
     * moves to UNDER_EVALUATION status and is ready for scoring.
     *
     * @param tenderTitle the tender title
     * @param tenderRef the reference number
     * @param evaluatorEmails list of evaluator email addresses
     * @param evaluatorNames parallel list of evaluator full names
     * @param contextPath application context path
     * @param serverBase server base URL
     */
    public static void sendEvaluationStartNotification(
            String tenderTitle, String tenderRef,
            java.util.List<String> evaluatorEmails,
            java.util.List<String> evaluatorNames,
            String contextPath, String serverBase) {

        if (evaluatorEmails == null || evaluatorEmails.isEmpty()) {
            return;
        }

        String evalUrl = serverBase + contextPath + "/eval/dashboard";
        String subject = "ProcureGov — Evaluation Started: " + tenderRef;

        for (int i = 0; i < evaluatorEmails.size(); i++) {
            String email = evaluatorEmails.get(i);
            String name = evaluatorNames.get(i);
            if (email == null || email.trim().isEmpty()) {
                continue;
            }

            String body = "<!DOCTYPE html><html><head><meta charset='UTF-8'/></head>"
                    + "<body style='margin:0;padding:0;background:#0d1117;font-family:Arial,sans-serif;'>"
                    + "<div style='max-width:560px;margin:32px auto;background:#131a24;"
                    + "border-radius:12px;overflow:hidden;border:1px solid rgba(201,168,76,0.2);'>"
                    + "<div style='background:#1a2333;padding:24px 32px;border-bottom:2px solid #c9a84c;'>"
                    + "<h2 style='margin:0;color:#c9a84c;font-size:18px;'>ProcureGov</h2>"
                    + "<p style='margin:4px 0 0;color:#a09880;font-size:12px;'>"
                    + "Ministry of Public Works — Kingdom of Lesotho</p></div>"
                    + "<div style='padding:32px;'>"
                    + "<h3 style='color:#f0ebe0;margin:0 0 6px;font-size:16px;'>Evaluation Ready</h3>"
                    + "<p style='color:#a09880;font-size:13px;margin:0 0 20px;'>"
                    + "A tender is ready for bid evaluation. You have been appointed "
                    + "as an evaluator.</p>"
                    + "<p style='color:#c2bdb0;font-size:14px;'>Dear "
                    + "<strong style='color:#f0ebe0;'>" + name + "</strong>,</p>"
                    + "<div style='background:#1a2333;border-radius:8px;padding:18px;margin:18px 0;"
                    + "border-left:4px solid #c9a84c;'>"
                    + "<p style='margin:0 0 6px;font-weight:bold;color:#f0ebe0;font-size:15px;'>"
                    + tenderTitle + "</p>"
                    + "<p style='margin:0;color:#a09880;font-size:13px;line-height:1.6;'>"
                    + "<strong>Reference:</strong> " + tenderRef + "</p></div>"
                    + "<p style='color:#a09880;font-size:13px;line-height:1.7;'>"
                    + "Please log in to the evaluation dashboard to score the bids. "
                    + "All evaluators must submit their scores before the ranked "
                    + "leaderboard is revealed.</p>"
                    + "<div style='text-align:center;margin:24px 0;'>"
                    + "<a href='" + evalUrl + "' style='display:inline-block;background:#c9a84c;"
                    + "color:#0d1117;padding:12px 28px;border-radius:6px;text-decoration:none;"
                    + "font-weight:bold;font-size:14px;'>Go to Evaluation Dashboard</a></div>"
                    + "<hr style='border:none;border-top:1px solid rgba(255,255,255,0.07);margin:20px 0;'/>"
                    + "<p style='color:#5a5448;font-size:11px;line-height:1.6;'>"
                    + "Automated notification from ProcureGov. "
                    + "For queries: <a href='mailto:help.procuregov@gov.ls' style='color:#c9a84c;'>"
                    + "help.procuregov@gov.ls</a></p>"
                    + "</div></div></body></html>";

            try {
                sendEmail(email, subject, body);
                LOGGER.info("Evaluation start notification sent → " + email);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to send evaluation email to " + email, e);
            }
        }
    }

    /**
     * Sends award outcome emails to all suppliers who bid on an awarded tender.
     * Emails include the tender reference, outcome (WON / NOT WON), and a link
     * to the award notice page (routed through login with a redirect
     * parameter).
     *
     * @param notice the completed award notice
     * @param allBids all bids for the tender — each must have
     * {@code supplierEmail} set
     * @param contextPath application context path (e.g. /ProcureGov)
     * @param serverBase server base URL (e.g. http://localhost:8081)
     */
    public static void sendAwardNotifications(AwardNotice notice,
            List<Bid> allBids,
            String contextPath,
            String serverBase) {
        if (allBids == null || allBids.isEmpty()) {
            return;
        }

        String awardUrl = serverBase + contextPath
                + "/login?redirect=supplier%2Faward-notice%3FtenderId%3D"
                + notice.getTenderId();

        for (Bid bid : allBids) {
            if (bid.getSupplierEmail() == null || bid.getSupplierEmail().trim().isEmpty()) {
                LOGGER.warning("Skipping email — no address for bidId=" + bid.getBidId());
                continue;
            }
            boolean isWinner = bid.getBidId() == notice.getWinningBidId();
            String outcome = isWinner ? "WON" : "NOT WON";
            String subject = "ProcureGov Award Notice: " + notice.getTenderReferenceNo()
                    + " — Your bid was " + outcome;
            try {
                sendEmail(bid.getSupplierEmail(), subject,
                        buildEmailBody(bid.getSupplierName(), notice, outcome, awardUrl, isWinner));
                LOGGER.info("Award email sent → " + bid.getSupplierEmail() + " [" + outcome + "]");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING,
                        "Email failed for " + bid.getSupplierEmail(), e);
            }
        }
    }

    /**
     * Sends a single test email to verify SMTP configuration.
     *
     * @param toEmail recipient address to send the test to
     * @return {@code true} if the email was sent successfully
     */
    public static boolean sendTestEmail(String toEmail) {
        try {
            sendEmail(toEmail, "ProcureGov — SMTP Test",
                    "<p>SMTP configuration is working for <strong>ProcureGov</strong>.</p>"
                    + "<p style='color:#888;font-size:13px;'>Ministry of Public Works, Lesotho</p>");
            LOGGER.info("Test email sent to: " + toEmail);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Test email failed", e);
            return false;
        }
    }

    /**
     * Sends a password reset verification code to a user's email. Includes a
     * security warning if the recipient did not request the reset.
     *
     * @param toEmail recipient's email address
     * @param toName recipient's full name
     * @param code the 6-digit reset code
     * @param contextPath application context path (e.g. /ProcureGov)
     * @param serverBase server base URL (e.g. http://localhost:8081)
     */
    public static void sendPasswordResetCode(String toEmail, String toName,
            String code, String contextPath, String serverBase) {
        String resetUrl = serverBase + contextPath + "/verify-code?email=" + toEmail;
        String subject = "ProcureGov — Password Reset Code: " + code;

        String body
                = "<!DOCTYPE html><html><head><meta charset='UTF-8'/></head>"
                + "<body style='margin:0;padding:0;background:#0d1117;font-family:Arial,sans-serif;'>"
                + "<div style='max-width:560px;margin:32px auto;background:#131a24;"
                + "border-radius:12px;overflow:hidden;border:1px solid rgba(201,168,76,0.2);'>"
                + "<div style='background:#1a2333;padding:24px 32px;border-bottom:2px solid #c9a84c;'>"
                + "<h2 style='margin:0;color:#c9a84c;font-size:18px;'>ProcureGov</h2>"
                + "<p style='margin:4px 0 0;color:#a09880;font-size:12px;'>"
                + "Ministry of Public Works — Kingdom of Lesotho</p></div>"
                + "<div style='padding:32px;'>"
                + "<h3 style='color:#f0ebe0;margin:0 0 6px;font-size:16px;'>Password Reset Request</h3>"
                + "<p style='color:#a09880;font-size:13px;margin:0 0 20px;'>"
                + "A password reset was requested for your ProcureGov account.</p>"
                + "<p style='color:#c2bdb0;font-size:14px;'>Dear "
                + "<strong style='color:#f0ebe0;'>" + toName + "</strong>,</p>"
                + "<p style='color:#a09880;font-size:13px;line-height:1.7;margin:16px 0;'>"
                + "Use the verification code below to reset your password. "
                + "This code expires in <strong style='color:#f0ebe0;'>15 minutes</strong>.</p>"
                + "<div style='background:#1a2333;border-radius:8px;padding:24px;"
                + "text-align:center;margin:24px 0;border:1px solid rgba(201,168,76,0.2);'>"
                + "<p style='margin:0 0 8px;color:#a09880;font-size:11px;"
                + "text-transform:uppercase;letter-spacing:0.1em;'>Verification Code</p>"
                + "<p style='margin:0;font-family:monospace;font-size:36px;font-weight:700;"
                + "color:#c9a84c;letter-spacing:12px;'>" + code + "</p></div>"
                + "<div style='text-align:center;margin:24px 0;'>"
                + "<a href='" + resetUrl + "' style='display:inline-block;background:#c9a84c;"
                + "color:#0d1117;padding:12px 28px;border-radius:6px;text-decoration:none;"
                + "font-weight:bold;font-size:14px;'>Reset Your Password</a></div>"
                + "<div style='background:#1a2333;border-radius:8px;padding:16px;margin:16px 0;"
                + "border-left:4px solid #c94c4c;'>"
                + "<p style='margin:0;color:#e88888;font-size:12px;line-height:1.6;'>"
                + "<strong>⚠ If you did not request this password reset:</strong><br/>"
                + "Please report this immediately to "
                + "<a href='mailto:help.procuregov@gov.ls' style='color:#c9a84c;'>"
                + "help.procuregov@gov.ls</a>. "
                + "Do not share this code with anyone. The ICT Directorate will never "
                + "ask for your password or verification codes.</p></div>"
                + "<hr style='border:none;border-top:1px solid rgba(255,255,255,0.07);margin:20px 0;'/>"
                + "<p style='color:#5a5448;font-size:11px;line-height:1.6;'>"
                + "Automated notification from ProcureGov. "
                + "For queries: <a href='mailto:help.procuregov@gov.ls' style='color:#c9a84c;'>"
                + "help.procuregov@gov.ls</a></p>"
                + "</div></div></body></html>";

        try {
            sendEmail(toEmail, subject, body);
            LOGGER.info("Password reset code sent → " + toEmail);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,
                    "Failed to send password reset code to " + toEmail, e);
        }
    }

    /* ── Private helpers ────────────────────────────────────── */
    /**
     * Sends a single HTML email using Gmail SMTP with STARTTLS. Uses
     * fully-qualified {@code javax.mail} class names to avoid import conflicts
     * caused by sources JARs on the classpath.
     *
     * @param toEmail the recipient's email address
     * @param subject the email subject line
     * @param htmlBody the HTML email body
     * @throws Exception if the email cannot be sent
     */
    private static void sendEmail(String toEmail, String subject, String htmlBody)
            throws Exception {

        if (SMTP_USER.isEmpty() || SMTP_PASSWORD.isEmpty()) {
            throw new IllegalStateException(
                    "SMTP is not configured. Set PROCUREGOV_SMTP_USER and PROCUREGOV_SMTP_PASSWORD.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        javax.mail.Session session = javax.mail.Session.getInstance(props,
                new javax.mail.Authenticator() {
            @Override
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
            }
        });

        javax.mail.internet.MimeMessage msg
                = new javax.mail.internet.MimeMessage(session);

        try {
            msg.setFrom(new javax.mail.internet.InternetAddress(SMTP_USER, FROM_NAME, "UTF-8"));
        } catch (Exception e) {
            msg.setFrom(new javax.mail.internet.InternetAddress(SMTP_USER));
        }

        msg.setRecipients(javax.mail.Message.RecipientType.TO,
                javax.mail.internet.InternetAddress.parse(toEmail));
        msg.setSubject(subject, "UTF-8");
        msg.setContent(htmlBody, "text/html; charset=UTF-8");
        javax.mail.Transport.send(msg);
    }


    /**
     * Reads configuration from an environment variable first, then from a JVM
     * system property, and finally returns the supplied default value.
     *
     * @param key the environment variable / system property name
     * @param defaultValue fallback value when unset
     * @return the resolved configuration value
     */
    private static String config(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            value = System.getProperty(key);
        }
        return (value == null || value.trim().isEmpty()) ? defaultValue : value.trim();
    }

    /**
     * Builds the HTML email body for an award notification.
     *
     * @param supplierName the recipient supplier's company name
     * @param notice the award notice with tender details
     * @param outcome "WON" or "NOT WON"
     * @param awardUrl the URL to view the full award notice
     * @param isWinner {@code true} if this supplier won the contract
     * @return a complete HTML email body string
     */
    private static String buildEmailBody(String supplierName, AwardNotice notice,
            String outcome, String awardUrl, boolean isWinner) {
        String accentColor = isWinner ? "#4caf82" : "#c94c4c";
        String outcomeMsg = isWinner
                ? "Congratulations! Your bid has been selected and you have been awarded the contract."
                : "Thank you for participating. After a thorough evaluation, your bid was not selected.";

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'/></head>"
                + "<body style='margin:0;padding:0;background:#0d1117;font-family:Arial,sans-serif;'>"
                + "<div style='max-width:600px;margin:32px auto;background:#131a24;"
                + "border-radius:12px;overflow:hidden;border:1px solid rgba(201,168,76,0.2);'>"
                + "<div style='background:#1a2333;padding:24px 32px;border-bottom:2px solid #c9a84c;'>"
                + "<h2 style='margin:0;color:#c9a84c;font-size:18px;'>ProcureGov</h2>"
                + "<p style='margin:4px 0 0;color:#a09880;font-size:12px;'>"
                + "Ministry of Public Works — Kingdom of Lesotho</p></div>"
                + "<div style='padding:32px;'>"
                + "<h3 style='color:#f0ebe0;margin:0 0 6px;font-size:16px;'>Award Notice</h3>"
                + "<p style='color:#a09880;font-size:13px;margin:0 0 20px;'>"
                + notice.getTenderReferenceNo() + " — " + notice.getTenderTitle() + "</p>"
                + "<p style='color:#c2bdb0;font-size:14px;'>Dear <strong style='color:#f0ebe0;'>"
                + supplierName + "</strong>,</p>"
                + "<div style='background:#1a2333;border-radius:8px;padding:18px;margin:18px 0;"
                + "border-left:4px solid " + accentColor + ";'>"
                + "<p style='margin:0 0 6px;font-weight:bold;color:" + accentColor + ";font-size:15px;'>"
                + "Your outcome: " + outcome + "</p>"
                + "<p style='margin:0;color:#a09880;font-size:13px;line-height:1.6;'>"
                + outcomeMsg + "</p></div>"
                + "<div style='background:#1a2333;border-radius:8px;padding:16px;margin:16px 0;"
                + "border-left:4px solid #c9a84c;font-size:13px;'>"
                + "<span style='color:#a09880;'>Awarded to: </span>"
                + "<strong style='color:#f0ebe0;'>" + notice.getWinningSupplierName() + "</strong>"
                + "</div>"
                + "<div style='text-align:center;margin:24px 0;'>"
                + "<a href='" + awardUrl + "' style='display:inline-block;background:#c9a84c;"
                + "color:#0d1117;padding:12px 28px;border-radius:6px;text-decoration:none;"
                + "font-weight:bold;font-size:14px;'>View Full Award Notice</a></div>"
                + "<hr style='border:none;border-top:1px solid rgba(255,255,255,0.07);margin:20px 0;'/>"
                + "<p style='color:#5a5448;font-size:11px;line-height:1.6;'>"
                + "Automated notification from ProcureGov. "
                + "For queries: <a href='mailto:help.procuregov@gov.ls' style='color:#c9a84c;'>"
                + "help.procuregov@gov.ls</a></p>"
                + "</div></div></body></html>";
    }
}
