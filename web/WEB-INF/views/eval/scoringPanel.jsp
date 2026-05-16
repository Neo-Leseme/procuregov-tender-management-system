<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Scoring Panel — ${tender.referenceNo} | ProcureGov</title>
  <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/images/images.png"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
</head>
<body>

<%@ include file="/WEB-INF/views/common/navbar.jsp" %>

<div class="page-content">
  <div class="container">

    <div class="breadcrumb">
      <c:choose>
        <c:when test="${loggedInUser.role == 'PROCUREMENT_OFFICER'}">
          <a href="${pageContext.request.contextPath}/officer/dashboard">Dashboard</a>
        </c:when>
        <c:otherwise>
          <a href="${pageContext.request.contextPath}/eval/dashboard">Dashboard</a>
        </c:otherwise>
      </c:choose>
      <span>›</span>
      <span>Scoring Panel — ${tender.referenceNo}</span>
    </div>

    <div class="page-header">
      <div>
        <span class="section-label">Module 4 — Bid Evaluation</span>
        <h1 class="page-title">Scoring Panel</h1>
        <p style="margin-top:4px; color:var(--clr-text-muted); font-size:0.88rem;">
          ${tender.title} &nbsp;·&nbsp;
          <span class="ref-no">${tender.referenceNo}</span>
        </p>
      </div>
    </div>

    <c:if test="${not empty successMsg}"><div class="alert alert-success">${successMsg}</div></c:if>
    <c:if test="${not empty errorMsg}"><div class="alert alert-danger">${errorMsg}</div></c:if>

    <!-- Scoring rules reminder -->
    <div class="card" style="margin-bottom:var(--space-xl); border-left:3px solid var(--clr-gold);">
      <h4 style="margin-bottom:var(--space-md);">Scoring Instructions</h4>
      <div style="display:grid; grid-template-columns:repeat(3,1fr); gap:var(--space-lg);">
        <div>
          <div class="formula-label">Price Score (40%)</div>
          <div class="formula-text">Auto-calculated by system</div>
          <div class="formula-note">You do not enter this</div>
        </div>
        <div>
          <div class="formula-label">Technical Compliance (35%)</div>
          <div class="formula-text">You enter: 0 – 100</div>
          <div class="formula-note">Based on the compliance statement</div>
        </div>
        <div>
          <div class="formula-label">Timeline Score (25%)</div>
          <div class="formula-text">Auto-calculated by system</div>
          <div class="formula-note">You do not enter this</div>
        </div>
      </div>
      <div class="alert alert-info" style="margin-top:var(--space-md);">
        You cannot view other evaluators' scores until you have submitted your own for each bid.
      </div>
    </div>

    <!-- Bid cards -->
    <c:choose>
      <c:when test="${empty bids}">
        <div class="empty-state">
          <p>No bids were submitted for this tender.</p>
        </div>
      </c:when>
      <c:otherwise>
        <c:forEach var="bid" items="${bids}" varStatus="loop">
          <div class="scoring-bid-card ${bid.hasBeenScored ? 'already-scored' : ''}">

            <div class="scoring-bid-card__header">
              <div>
                <span class="detail-label">Bid #${loop.count}</span>
                <h4 style="margin-top:4px;">${bid.supplierName}</h4>
              </div>
              <c:choose>
                <c:when test="${bid.hasBeenScored}">
                  <span class="badge badge-success">✓ Scored</span>
                </c:when>
                <c:otherwise>
                  <span class="badge badge-eval">Awaiting Your Score</span>
                </c:otherwise>
              </c:choose>
            </div>

            <!-- Bid summary — always visible -->
            <div style="display:grid; grid-template-columns:repeat(3,1fr); gap:var(--space-lg); margin-bottom:var(--space-lg);">
              <div>
                <div class="detail-label">Bid Amount</div>
                <div style="color:var(--clr-text-primary); font-size:1.1rem; font-weight:600; margin-top:4px;">
                  M <fmt:formatNumber value="${bid.bidAmount}" type="number" groupingUsed="true" maxFractionDigits="2"/>
                </div>
              </div>
              <div>
                <div class="detail-label">Delivery Days</div>
                <div style="color:var(--clr-text-primary); font-size:1.1rem; font-weight:600; margin-top:4px;">
                  ${bid.deliveryDays} days
                </div>
              </div>
              <div>
                <div class="detail-label">Submitted</div>
                <div style="color:var(--clr-text-secondary); margin-top:4px;">
                  <fmt:formatDate value="${bid.submittedAt}" pattern="dd MMM yyyy HH:mm"/>
                </div>
              </div>
            </div>

            <!-- Compliance statement -->
            <div style="margin-bottom:var(--space-lg);">
              <div class="detail-label">Technical Compliance Statement</div>
              <p style="margin-top:8px; color:var(--clr-text-primary); line-height:1.8;
                        background:var(--clr-bg-elevated); padding:var(--space-md);
                        border-radius:var(--radius-md); border:1px solid var(--clr-border-subtle);">
                ${bid.complianceStatement}
              </p>
            </div>

            <!-- Supporting document download -->
            <c:if test="${not empty bid.documentPath}">
              <div style="margin-bottom:var(--space-lg);">
                <div class="detail-label">Supporting Document</div>
                <a href="${pageContext.request.contextPath}/files/download?type=bid&bidId=${bid.bidId}"
                   target="_blank"
                   class="btn btn-ghost"
                   style="margin-top:8px; font-size:0.82rem; padding:8px 16px; display:inline-flex; align-items:center; gap:6px;">
                  <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                    <polyline points="7 10 12 15 17 10"/>
                    <line x1="12" y1="15" x2="12" y2="3"/>
                  </svg>
                  Download Supplier Document
                </a>
                <p class="text-xs" style="margin-top:4px; color:var(--clr-text-muted);">
                  Supplier's uploaded supporting document (PDF/DOCX).
                </p>
              </div>
            </c:if>

            <!-- Score form OR already scored confirmation -->
            <c:choose>
              <c:when test="${!bid.hasBeenScored}">
                <div style="background:var(--clr-bg-elevated); border:1px solid var(--clr-border);
                            border-radius:var(--radius-md); padding:var(--space-lg); margin-top:var(--space-md);">
                  <h4 style="margin-bottom:var(--space-md); color:var(--clr-gold);">
                    Enter Your Technical Compliance Score
                  </h4>
                  <form method="POST"
                        action="${pageContext.request.contextPath}/eval/score/submit"
                        id="scoreForm_${bid.bidId}">
                    <input type="hidden" name="bidId"    value="${bid.bidId}"/>
                    <input type="hidden" name="tenderId" value="${tender.tenderId}"/>

                    <div class="form-group" style="max-width:300px;">
                      <label class="form-label" for="tech_${bid.bidId}">
                        Technical Compliance Score (0 – 100)
                        <span style="color:var(--clr-danger)">*</span>
                      </label>
                      <input class="form-input"
                             type="number"
                             id="tech_${bid.bidId}"
                             name="technicalScore"
                             min="0" max="100" step="0.01"
                             placeholder="e.g. 85"
                             required/>
                      <span class="form-hint">Price and timeline scores are auto-calculated.</span>
                    </div>

                    <button type="submit" class="btn btn-primary"
                            onclick="return confirm('Submit your score for ${bid.supplierName}? This cannot be changed.');">
                      Submit Score for this Bid
                    </button>
                  </form>
                </div>
              </c:when>
              <c:otherwise>
                <div class="alert alert-success">
                  ✓ You have submitted your score for this bid. Other evaluators' scores
                  will be visible on the officer's evaluation panel once all scoring is complete.
                </div>
              </c:otherwise>
            </c:choose>

          </div>
        </c:forEach>
      </c:otherwise>
    </c:choose>

    <c:choose>
      <c:when test="${loggedInUser.role == 'PROCUREMENT_OFFICER'}">
        <a href="${pageContext.request.contextPath}/officer/dashboard"
           class="btn btn-ghost" style="margin-top:var(--space-lg);">
          ← Back to Dashboard
        </a>
      </c:when>
      <c:otherwise>
        <a href="${pageContext.request.contextPath}/eval/dashboard"
           class="btn btn-ghost" style="margin-top:var(--space-lg);">
          ← Back to Dashboard
        </a>
      </c:otherwise>
    </c:choose>

  </div>
</div>

</body>
</html>