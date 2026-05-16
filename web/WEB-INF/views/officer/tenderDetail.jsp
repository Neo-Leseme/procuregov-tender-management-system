<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        <title>${tender.referenceNo} | ProcureGov</title>
        <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/images/images.png"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
    </head>
    <body>

        <%@ include file="/WEB-INF/views/common/navbar.jsp" %>

        <div class="page-content">
            <div class="container">

                <!-- Breadcrumb -->
                <div class="breadcrumb">
                    <a href="${pageContext.request.contextPath}/officer/dashboard">Dashboard</a>
                    <span>›</span>
                    <a href="${pageContext.request.contextPath}/officer/tenders">Tenders</a>
                    <span>›</span>
                    <span>${tender.referenceNo}</span>
                </div>

                <!-- Flash messages -->
                <c:if test="${not empty sessionScope.successMsg}">
                    <div class="alert alert-success">${sessionScope.successMsg}</div>
                    <c:remove var="successMsg" scope="session"/>
                </c:if>
                <c:if test="${not empty sessionScope.errorMsg}">
                    <div class="alert alert-danger">${sessionScope.errorMsg}</div>
                    <c:remove var="errorMsg" scope="session"/>
                </c:if>

                <!-- Header row -->
                <div class="page-header">
                    <div>
                        <span class="ref-no" style="font-size:0.9rem;">${tender.referenceNo}</span>
                        <h1 class="page-title" style="margin-top:6px;">${tender.title}</h1>
                        <span class="badge ${tender.statusBadgeClass}" style="margin-top:8px; display:inline-block;">
                            ${tender.status}
                        </span>
                    </div>
                    <div style="display:flex; gap:12px; flex-wrap:wrap; align-items:center;">
                        <c:if test="${tender.status == 'DRAFT'}">
                            <a href="${pageContext.request.contextPath}/officer/tender/edit?id=${tender.tenderId}"
                               class="btn btn-outline">Edit Tender</a>
                        </c:if>
                        <%-- FIXED: type=notice parameter required by FileDownloadServlet --%>
                        <c:if test="${not empty tender.noticeFilePath}">
                            <a href="${pageContext.request.contextPath}/files/download?type=notice&tenderId=${tender.tenderId}"
                               class="btn btn-ghost" target="_blank">
                                <svg width="14" height="14" fill="none" stroke="currentColor"
                                     stroke-width="2" viewBox="0 0 24 24">
                                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                                    <polyline points="7 10 12 15 17 10"/>
                                    <line x1="12" y1="15" x2="12" y2="3"/>
                                </svg>
                                Download Notice PDF
                            </a>
                        </c:if>
                    </div>
                </div>

                <div class="detail-grid">

                    <!-- Left: Tender info -->
                    <div>
                        <div class="card" style="margin-bottom:var(--space-lg);">
                            <h4 style="margin-bottom:var(--space-lg); padding-bottom:var(--space-md);
                                       border-bottom:1px solid var(--clr-border-subtle);">
                                Tender Details
                            </h4>

                            <div class="detail-row">
                                <span class="detail-label">Reference No.</span>
                                <span class="detail-value ref-no">${tender.referenceNo}</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Category</span>
                                <span class="detail-value">${tender.category.label}</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Estimated Value</span>
                                <span class="detail-value">
                                    M <fmt:formatNumber value="${tender.estimatedValue}"
                                                        type="number" groupingUsed="true"
                                                        maxFractionDigits="2"/>
                                </span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Submission Deadline</span>
                                <span class="detail-value">
                                    <fmt:formatDate value="${tender.closingDatetime}"
                                                    pattern="dd MMMM yyyy 'at' HH:mm"/>
                                </span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Created By</span>
                                <span class="detail-value">${tender.createdByName}</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Created On</span>
                                <span class="detail-value">
                                    <fmt:formatDate value="${tender.createdAt}"
                                                    pattern="dd MMM yyyy HH:mm"/>
                                </span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Total Bids</span>
                                <span class="detail-value"><strong>${tender.bidCount}</strong></span>
                            </div>

                            <div style="margin-top:var(--space-lg);">
                                <span class="detail-label">Description</span>
                                <p style="margin-top:8px; color:var(--clr-text-primary);
                                          line-height:1.8;">${tender.description}</p>
                            </div>
                        </div>
                    </div>

                    <!-- Right: Lifecycle + actions -->
                    <div>

                        <div class="card" style="margin-bottom:var(--space-lg);">
                            <h4 style="margin-bottom:var(--space-md);">Tender Lifecycle</h4>

                            <div class="lifecycle-mini">
                                <c:forEach var="s" items="${statuses}">
                                    <div class="lifecycle-mini__step
                                        <c:choose>
                                            <c:when test="${tender.status == s}"> active</c:when>
                                            <c:when test="${tender.status == 'OPEN'
                                                           && (s == 'DRAFT')}"> done</c:when>
                                            <c:when test="${tender.status == 'CLOSED'
                                                           && (s == 'DRAFT' || s == 'OPEN')}"> done</c:when>
                                            <c:when test="${tender.status == 'UNDER_EVALUATION'
                                                           && (s == 'DRAFT' || s == 'OPEN'
                                                               || s == 'CLOSED')}"> done</c:when>
                                            <c:when test="${tender.status == 'EVALUATED'
                                                           && s != 'AWARDED'}"> done</c:when>
                                            <c:when test="${tender.status == 'AWARDED'}"> done</c:when>
                                        </c:choose>
                                        ">
                                        <div class="lifecycle-mini__dot"></div>
                                        <span class="lifecycle-mini__label">${s.name()}</span>
                                    </div>
                                </c:forEach>
                            </div>

                            <div style="margin-top:var(--space-lg);">
                                <c:choose>

                                    <%-- DRAFT → publish --%>
                                    <c:when test="${tender.status == 'DRAFT'}">
                                        <p class="text-sm" style="margin-bottom:var(--space-md);">
                                            Publish this tender to make it visible to all suppliers.
                                        </p>
                                        <form method="POST"
                                              action="${pageContext.request.contextPath}/officer/tender/status">
                                            <input type="hidden" name="tenderId"
                                                   value="${tender.tenderId}"/>
                                            <input type="hidden" name="targetStatus" value="OPEN"/>
                                            <button type="submit" class="btn btn-primary btn--full"
                                                    onclick="return confirm('Publish this tender? It will be visible to all suppliers and cannot be edited.');">
                                                Publish Tender (→ Open)
                                            </button>
                                        </form>
                                    </c:when>

                                    <%-- OPEN — closes automatically --%>
                                    <c:when test="${tender.status == 'OPEN'}">
                                        <div class="alert alert-info">
                                            This tender is open for bidding.
                                            It will close automatically at the deadline.
                                        </div>
                                    </c:when>

                                    <%-- CLOSED → begin evaluation --%>
                                    <c:when test="${tender.status == 'CLOSED'}">
                                        <p class="text-sm" style="margin-bottom:var(--space-md);">
                                            Bidding is closed. Start the evaluation process
                                            when the committee is ready.
                                        </p>
                                        <form method="POST"
                                              action="${pageContext.request.contextPath}/officer/tender/status">
                                            <input type="hidden" name="tenderId"
                                                   value="${tender.tenderId}"/>
                                            <input type="hidden" name="targetStatus"
                                                   value="UNDER_EVALUATION"/>
                                            <button type="submit" class="btn btn-primary btn--full">
                                                Begin Evaluation (→ Under Evaluation)
                                            </button>
                                        </form>
                                    </c:when>

                                    <%-- UNDER_EVALUATION — auto-transitions when all scored --%>
                                    <c:when test="${tender.status == 'UNDER_EVALUATION'}">
                                        <div class="alert alert-info" style="margin-bottom:12px;">
                                            <p> Evaluation in progress. The status moves to
                                            <strong>Evaluated</strong> automatically once all
                                            committee members have scored all bids.</p>
                                        </div>
                                        <%-- Officer can view the panel while scoring is ongoing --%>
                                        <a href="${pageContext.request.contextPath}/officer/eval/panel?tenderId=${tender.tenderId}"
                                           class="btn btn-outline btn--full">
                                            View Evaluation Panel
                                        </a>
                                    </c:when>

                                    <%-- EVALUATED → go to eval panel to award --%>
                                    <c:when test="${tender.status == 'EVALUATED'}">
                                        <p class="text-sm" style="margin-bottom:var(--space-md);">
                                            All bids have been scored. View the ranked leaderboard
                                            and award the contract.
                                        </p>
                                        <%--
                                            IMPORTANT: This links to /officer/eval/panel
                                            which is mapped in web.xml to EvalServlet.
                                            EvalServlet.showOfficerEvalPanel() loads the ranked bids
                                            and forwards to evalPanel.jsp which contains the award form.
                                            The award form POSTs to /officer/tender/award
                                            which is also mapped to EvalServlet.processAward().
                                        --%>
                                        <a href="${pageContext.request.contextPath}/officer/eval/panel?tenderId=${tender.tenderId}"
                                           class="btn btn-primary btn--full">
                                            View Ranked Results &amp; Award Contract
                                        </a>
                                    </c:when>

                                    <%-- AWARDED — done --%>
                                    <c:when test="${tender.status == 'AWARDED'}">
                                        <div class="alert alert-success">
                                            Contract has been awarded.
                                            The award notice is visible to all bidding suppliers.
                                        </div>
                                    </c:when>

                                </c:choose>
                            </div>
                        </div>

                        <!-- Quick links -->
                        <div class="card">
                            <h4 style="margin-bottom:var(--space-md);">Quick Links</h4>
                            <div style="display:flex; flex-direction:column; gap:8px;">
                                <a href="${pageContext.request.contextPath}/officer/tenders"
                                   class="btn btn-ghost btn--full">
                                    ← Back to Tender List
                                </a>
                                <c:if test="${tender.status == 'UNDER_EVALUATION'
                                             || tender.status == 'EVALUATED'
                                             || tender.status == 'AWARDED'}">
                                    <a href="${pageContext.request.contextPath}/officer/eval/panel?tenderId=${tender.tenderId}"
                                       class="btn btn-outline btn--full">
                                        Evaluation Panel
                                    </a>
                                </c:if>
                            </div>
                        </div>

                    </div>
                </div>

            </div>
        </div>

    </body>
</html>
