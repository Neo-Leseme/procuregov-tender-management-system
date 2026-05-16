<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        <title>Submit Bid — ${tender.referenceNo} | ProcureGov</title>
        <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/images/images.png"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
    </head>
    <body>

        <%@ include file="/WEB-INF/views/common/navbar.jsp" %>

        <div class="page-content">
            <div class="container">

                <!-- Breadcrumb -->
                <div class="breadcrumb">
                    <a href="${pageContext.request.contextPath}/supplier/dashboard">Dashboard</a>
                    <span>›</span>
                    <a href="${pageContext.request.contextPath}/supplier/tenders">Open Tenders</a>
                    <span>›</span>
                    <a href="${pageContext.request.contextPath}/supplier/tender/detail?id=${tender.tenderId}">
                        ${tender.referenceNo}
                    </a>
                    <span>›</span>
                    <span>Submit Bid</span>
                </div>

                <!-- Page header -->
                <div class="page-header">
                    <div>
                        <span class="section-label">Module 3 — Bid Submission</span>
                        <h1 class="page-title">Submit Your Bid</h1>
                        <p style="margin-top:4px; color:var(--clr-text-muted); font-size:0.88rem;">
                            ${tender.title} &nbsp;·&nbsp;
                            <span class="ref-no">${tender.referenceNo}</span>
                        </p>
                    </div>
                </div>

                <!-- Tender summary strip -->
                <div class="card" style="margin-bottom:var(--space-lg); padding:var(--space-lg);
                     display:flex; gap:var(--space-xl); flex-wrap:wrap; align-items:center;">
                    <div>
                        <div class="detail-label">Est. Value</div>
                        <div style="color:var(--clr-text-primary); font-weight:600; margin-top:4px; font-size:1rem;">
                            M <fmt:formatNumber value="${tender.estimatedValue}" type="number"
                                              groupingUsed="true" maxFractionDigits="0"/>
                        </div>
                    </div>
                    <div>
                        <div class="detail-label">Deadline</div>
                        <div style="color:var(--clr-danger); font-weight:600; margin-top:4px; font-size:1rem;">
                            <fmt:formatDate value="${tender.closingDatetime}" pattern="dd MMM yyyy HH:mm"/>
                        </div>
                    </div>
                    <div>
                        <div class="detail-label">Category</div>
                        <div style="color:var(--clr-text-primary); margin-top:4px;">${tender.category.label}</div>
                    </div>
                    <div style="margin-left:auto;">
                        <span class="badge badge-open">Open for Bidding</span>
                    </div>
                </div>

                <%-- ═══════════════════════════════════════════════════════
                     SERVER-SIDE ERROR — set by BidServlet on validation fail.
                     tender is always set before forwarding so no NPE occurs.
                     ═══════════════════════════════════════════════════════ --%>
                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger" id="serverError">
                        <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"
                             viewBox="0 0 24 24" style="flex-shrink:0;">
                        <circle cx="12" cy="12" r="10"/>
                        <line x1="12" y1="8" x2="12" y2="12"/>
                        <line x1="12" y1="16" x2="12.01" y2="16"/>
                        </svg>
                        <div>
                            <strong>Submission Error</strong><br/>
                            ${errorMsg}
                        </div>
                    </div>
                </c:if>

                <%-- Client-side file size error — shown by JS before form submits --%>
                <div class="alert alert-danger" id="fileSizeError" style="display:none;">
                    <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"
                         viewBox="0 0 24 24" style="flex-shrink:0;">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="8" x2="12" y2="12"/>
                    <line x1="12" y1="16" x2="12.01" y2="16"/>
                    </svg>
                    <div>
                        <strong>File Too Large</strong><br/>
                        <span id="fileSizeMsg">Your file exceeds the 10 MB limit. Please choose a smaller file.</span>
                    </div>
                </div>

                <!-- One-bid warning -->
                <div class="alert alert-warning" style="margin-bottom:var(--space-lg);">
                    <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"
                         viewBox="0 0 24 24" style="flex-shrink:0;">
                    <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3
                          L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                    <line x1="12" y1="9" x2="12" y2="13"/>
                    <line x1="12" y1="17" x2="12.01" y2="17"/>
                    </svg>
                    You may submit <strong>one bid per tender only</strong>.
                    Once submitted your bid cannot be edited or withdrawn.
                </div>

                <!-- ═══════════════════════════════════════════════════════
                     BID FORM
                     action → POST /supplier/bid/submit → BidServlet.doPost()
                     BidServlet handles ALL business logic, file size enforcement,
                     closing date check, and one-bid rule.
                     On error it sets errorMsg and forwards back here with tender set.
                     On success it does POST-Redirect-GET to /supplier/dashboard.
                     ═══════════════════════════════════════════════════════ -->
                <div class="form-card">
                    <form action="${pageContext.request.contextPath}/supplier/bid/submit"
                          method="POST"
                          enctype="multipart/form-data"
                          id="bidForm"
                          novalidate>

                        <%-- tenderId passed as hidden field so BidServlet knows which tender --%>
                        <input type="hidden" name="tenderId" value="${tender.tenderId}"/>

                        <!-- ── Bid Amount ──────────────────────────────────── -->
                        <div class="form-group">
                            <label class="form-label" for="bidAmount">
                                Bid Amount (Maloti) <span style="color:var(--clr-danger)">*</span>
                            </label>
                            <div class="input-wrapper">
                                <span class="input-icon"
                                      style="font-size:0.78rem; font-weight:700; color:var(--clr-text-muted);
                                      width:auto; left:14px;">M</span>
                                <input class="form-input form-input--icon"
                                       type="number"
                                       id="bidAmount"
                                       name="bidAmount"
                                       placeholder="0.00"
                                       min="1"
                                       step="0.01"
                                       value="${param.bidAmount}"
                                       required/>
                            </div>
                            <span class="form-hint">
                                Enter your total bid price in Lesotho Maloti (LSL).
                                The Ministry's estimated value is
                                M <fmt:formatNumber value="${tender.estimatedValue}" type="number"
                                                  groupingUsed="true" maxFractionDigits="0"/>.
                            </span>
                        </div>

                        <!-- ── Technical Compliance Statement ─────────────── -->
                        <div class="form-group">
                            <label class="form-label" for="complianceStatement">
                                Technical Compliance Statement
                                <span style="color:var(--clr-danger)">*</span>
                            </label>
                            <textarea class="form-input"
                                      id="complianceStatement"
                                      name="complianceStatement"
                                      rows="5"
                                      maxlength="600"
                                      placeholder="Describe how your company meets the technical specifications of this tender.
Include: relevant past experience, certifications held, available equipment or personnel,
and your approach to delivering the scope of work..."
                                      required
                                      oninput="updateCharCount(this)"
                                      style="resize:vertical; min-height:120px;"></textarea>
                            <div style="display:flex; justify-content:space-between; margin-top:5px;">
                                <span class="form-hint">
                                    Evaluators will score this section (0–100). Max 600 characters.
                                </span>
                                <span id="charCount"
                                      class="text-xs"
                                      style="color:var(--clr-text-muted); white-space:nowrap; margin-left:12px;">
                                    0 / 600
                                </span>
                            </div>
                            <div id="charWarning"
                                 style="display:none; font-size:0.78rem; color:var(--clr-warning); margin-top:3px;">
                                Approaching the 600 character limit.
                            </div>
                        </div>

                        <!-- ── Proposed Delivery Timeline ─────────────────── -->
                        <div class="form-group">
                            <label class="form-label" for="deliveryDays">
                                Proposed Delivery Timeline (Days)
                                <span style="color:var(--clr-danger)">*</span>
                            </label>
                            <input class="form-input"
                                   type="number"
                                   id="deliveryDays"
                                   name="deliveryDays"
                                   placeholder="e.g. 90"
                                   min="1"
                                   max="1825"
                                   value="${param.deliveryDays}"
                                   required/>
                            <span class="form-hint">
                                Number of calendar days from contract award date to full completion.
                                Shorter timelines score higher — the system auto-calculates the timeline score.
                            </span>
                        </div>

                        <!-- ── Supporting Document ────────────────────────── -->
                        <div class="form-group">
                            <label class="form-label" for="bidDocument">
                                Supporting Document <span style="color:var(--clr-danger)">*</span>
                            </label>

                            <!-- File size rule banner -->
                            <div style="background:var(--clr-bg-elevated); border:1px solid var(--clr-border-subtle);
                                 border-radius:var(--radius-md); padding:var(--space-md);
                                 margin-bottom:var(--space-sm); font-size:0.82rem;">
                                <div style="display:flex; align-items:center; gap:8px; color:var(--clr-text-secondary);">
                                    <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"
                                         viewBox="0 0 24 24" style="flex-shrink:0; color:var(--clr-gold);">
                                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                                    <polyline points="14 2 14 8 20 8"/>
                                    </svg>
                                    <span>
                                        Accepted formats: <strong>PDF, DOCX</strong> &nbsp;·&nbsp;
                                        Maximum size: <strong style="color:var(--clr-gold);">10 MB</strong>
                                    </span>
                                </div>
                                <div style="color:var(--clr-text-muted); margin-top:5px; font-size:0.78rem;">
                                    Attach your company profile, tax clearance certificate, relevant experience, or technical proposal.
                                    Files over 10 MB will be rejected — compress large PDFs before uploading.
                                </div>
                            </div>

                            <input class="form-input"
                                   type="file"
                                   id="bidDocument"
                                   name="bidDocument"
                                   accept=".pdf,.docx"
                                   onchange="checkFileSize(this)"
                                   style="padding:10px; cursor:pointer;"
                                   required/>

                            <!-- File feedback shown by JS -->
                            <div id="fileStatus" style="margin-top:6px; font-size:0.82rem; display:none;"></div>
                        </div>

                        <!-- ── Declaration checkbox ───────────────────────── -->
                        <div class="form-group"
                             style="background:var(--clr-bg-elevated); border:1px solid var(--clr-border-subtle);
                             border-radius:var(--radius-md); padding:var(--space-lg);">
                            <label style="display:flex; align-items:flex-start; gap:12px; cursor:pointer;">
                                <input type="checkbox"
                                       id="declaration"
                                       required
                                       style="margin-top:3px; flex-shrink:0; accent-color:var(--clr-gold);
                                       width:16px; height:16px;"/>
                                <span class="text-sm" style="color:var(--clr-text-secondary); line-height:1.7;">
                                    I declare that all information provided in this bid is accurate and complete.
                                    I confirm that my company fully meets the requirements specified in the tender notice,
                                    and that I have the authority to submit this bid on behalf of my organisation.
                                    I understand that submitting false information may result in disqualification.
                                </span>
                            </label>
                        </div>

                        <!-- ── Submit / Cancel ────────────────────────────── -->
                        <div style="display:flex; gap:var(--space-md); margin-top:var(--space-xl); flex-wrap:wrap;">
                            <button type="submit"
                                    class="btn btn-primary btn--lg"
                                    id="submitBtn">
                                <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"
                                     viewBox="0 0 24 24">
                                <path d="M22 2L11 13"/><path d="M22 2L15 22 11 13 2 9l20-7z"/>
                                </svg>
                                Submit Sealed Bid
                            </button>
                            <a href="${pageContext.request.contextPath}/supplier/tender/detail?id=${tender.tenderId}"
                               class="btn btn-ghost btn--lg">
                                Cancel
                            </a>
                        </div>

                    </form>
                </div>

            </div>
        </div>

        <script>
            /* ══════════════════════════════════════════════════════════
             Constants
             ══════════════════════════════════════════════════════════ */
            const MAX_FILE_MB = 10;
            const MAX_FILE_BYTES = MAX_FILE_MB * 1024 * 1024;

            /* ══════════════════════════════════════════════════════════
             Character counter for compliance statement
             ══════════════════════════════════════════════════════════ */
            function updateCharCount(el) {
                const count = el.value.length;
                const label = document.getElementById('charCount');
                const warning = document.getElementById('charWarning');

                label.textContent = count + ' / 600';

                if (count > 570) {
                    label.style.color = 'var(--clr-danger)';
                    warning.style.display = 'block';
                    warning.style.color = 'var(--clr-danger)';
                    warning.textContent = (600 - count) + ' character(s) remaining.';
                } else if (count > 480) {
                    label.style.color = 'var(--clr-warning)';
                    warning.style.display = 'block';
                    warning.style.color = 'var(--clr-warning)';
                    warning.textContent = (600 - count) + ' characters remaining.';
                } else {
                    label.style.color = 'var(--clr-text-muted)';
                    warning.style.display = 'none';
                }
            }

            /* ══════════════════════════════════════════════════════════
             Client-side file size check (runs before form submits)
             This is a UX guard only — BidServlet enforces the rule
             server-side regardless of what the browser does.
             ══════════════════════════════════════════════════════════ */
            function checkFileSize(input) {
                const errorBox = document.getElementById('fileSizeError');
                const errorMsg = document.getElementById('fileSizeMsg');
                const fileStatus = document.getElementById('fileStatus');
                const submitBtn = document.getElementById('submitBtn');

                if (!input.files || input.files.length === 0) {
                    errorBox.style.display = 'none';
                    fileStatus.style.display = 'none';
                    submitBtn.disabled = false;
                    return;
                }

                const file = input.files[0];
                const sizeMB = (file.size / (1024 * 1024)).toFixed(2);
                const lowerName = file.name.toLowerCase();
                const isAllowedType = lowerName.endsWith('.pdf') || lowerName.endsWith('.docx');
                const isValid = file.size <= MAX_FILE_BYTES && isAllowedType;

                if (!isAllowedType) {
                    errorBox.style.display = 'flex';
                    errorMsg.textContent =
                            'Unsupported file type. Upload PDF or DOCX only.';
                    fileStatus.style.display = 'block';
                    fileStatus.style.color = 'var(--clr-danger)';
                    fileStatus.innerHTML = '✗ ' + file.name + ' — unsupported file type.';
                    submitBtn.disabled = true;
                    errorBox.scrollIntoView({behavior: 'smooth', block: 'center'});

                } else if (!isValid) {
                    // Show error banner
                    errorBox.style.display = 'flex';
                    errorMsg.textContent =
                            'Your file "' + file.name + '" is ' + sizeMB + ' MB. '
                            + 'The maximum allowed is ' + MAX_FILE_MB + ' MB. '
                            + 'Please compress the file or choose a smaller document.';

                    // Red file status
                    fileStatus.style.display = 'block';
                    fileStatus.style.color = 'var(--clr-danger)';
                    fileStatus.innerHTML =
                            '✗ ' + file.name + ' (' + sizeMB + ' MB) — exceeds 10 MB limit.';

                    // Disable submit to prevent upload attempt
                    submitBtn.disabled = true;

                    // Scroll to the error
                    errorBox.scrollIntoView({behavior: 'smooth', block: 'center'});

                } else {
                    // Clear error, show green confirmation
                    errorBox.style.display = 'none';
                    fileStatus.style.display = 'block';
                    fileStatus.style.color = 'var(--clr-success)';
                    fileStatus.innerHTML =
                            '✓ ' + file.name + ' (' + sizeMB + ' MB) — ready to upload.';
                    submitBtn.disabled = false;
                }
            }

            /* ══════════════════════════════════════════════════════════
             Form submit guard
             ══════════════════════════════════════════════════════════ */
            document.getElementById('bidForm').addEventListener('submit', function (e) {

                // 1. Declaration must be checked
                if (!document.getElementById('declaration').checked) {
                    e.preventDefault();
                    alert('Please tick the declaration checkbox before submitting.');
                    return;
                }

                // 2. Supporting document is mandatory and must pass size/type checks.
                const fileInput = document.getElementById('bidDocument');
                if (!fileInput.files || fileInput.files.length === 0) {
                    e.preventDefault();
                    alert('Please upload a supporting document before submitting.');
                    return;
                }
                const lowerName = fileInput.files[0].name.toLowerCase();
                if (!(lowerName.endsWith('.pdf') || lowerName.endsWith('.docx'))
                        || fileInput.files[0].size > MAX_FILE_BYTES) {
                    e.preventDefault();
                    checkFileSize(fileInput);
                    return;
                }

                // 3. Confirm before final submission
                if (!confirm(
                        'Submit your sealed bid for ' + '${tender.referenceNo}' + '?\n\n'
                        + 'You cannot edit or withdraw your bid after submission.'
                        )) {
                    e.preventDefault();
                    return;
                }

                // 4. Disable button to prevent double submission
                const btn = document.getElementById('submitBtn');
                btn.disabled = true;
                btn.textContent = 'Submitting…';
            });

            /* ══════════════════════════════════════════════════════════
             On page load — scroll to server error if present,
             and restore char count if statement was echoed back
             ══════════════════════════════════════════════════════════ */
            window.addEventListener('DOMContentLoaded', function () {
                const serverError = document.getElementById('serverError');
                if (serverError) {
                    serverError.scrollIntoView({behavior: 'smooth', block: 'center'});
                }

                const stmt = document.getElementById('complianceStatement');
                if (stmt && stmt.value.length > 0) {
                    updateCharCount(stmt);
                }
            });
        </script>

    </body>
</html>