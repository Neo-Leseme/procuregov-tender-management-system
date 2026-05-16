<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Open Tenders | ProcureGov</title>
  <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/images/images.png"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
</head>
<body>

<%@ include file="/WEB-INF/views/common/navbar.jsp" %>

<div class="page-content">
  <div class="container">

    <div class="page-header">
      <div>
        <span class="section-label">Module 3 — Bid Submission</span>
        <h1 class="page-title">Open Tenders</h1>
        <p style="margin-top:4px; color:var(--clr-text-muted); font-size:0.88rem;">
          All tenders currently open for bid submission.
        </p>
      </div>
    </div>

    <c:choose>
      <c:when test="${empty openTenders}">
        <div class="empty-state" style="background:var(--clr-bg-card); border:1px solid var(--clr-border-subtle); border-radius:var(--radius-lg); padding:var(--space-3xl);">
          <p>No tenders are currently open. Check back soon.</p>
          <a href="${pageContext.request.contextPath}/supplier/dashboard"
             class="btn btn-ghost" style="margin-top:12px;">Back to Dashboard</a>
        </div>
      </c:when>
      <c:otherwise>
        <div class="tenders-grid">
          <c:forEach var="t" items="${openTenders}">
            <c:set var="hasBid" value="${requestScope['bid_'.concat(t.tenderId)]}"/>
            <div class="tender-browse-card ${hasBid ? 'tender-browse-card--bid' : ''}">
              <div class="tender-browse-card__header">
                <span class="ref-no">${t.referenceNo}</span>
                <c:choose>
                  <c:when test="${hasBid}">
                    <span class="badge badge-success">✓ Bid Submitted</span>
                  </c:when>
                  <c:otherwise>
                    <span class="badge badge-open">Open</span>
                  </c:otherwise>
                </c:choose>
              </div>

              <h3 class="tender-browse-card__title">${t.title}</h3>

              <p class="tender-browse-card__desc">
                ${t.description.length() > 120 ? t.description.substring(0,120).concat('...') : t.description}
              </p>

              <div class="tender-browse-card__meta">
                <div class="meta-item">
                  <span class="meta-label">Category</span>
                  <span class="meta-value">${t.category.label}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">Est. Value</span>
                  <span class="meta-value">
                    M <fmt:formatNumber value="${t.estimatedValue}" type="number" groupingUsed="true" maxFractionDigits="0"/>
                  </span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">Closes</span>
                  <span class="meta-value">
                    <fmt:formatDate value="${t.closingDatetime}" pattern="dd MMM yyyy"/>
                  </span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">Time</span>
                  <span class="meta-value">
                    <fmt:formatDate value="${t.closingDatetime}" pattern="HH:mm"/>
                  </span>
                </div>
              </div>

              <div style="display:flex; gap:10px; margin-top:var(--space-lg); flex-wrap:wrap;">
                <a href="${pageContext.request.contextPath}/supplier/tender/detail?id=${t.tenderId}"
                   class="btn btn-outline" style="flex:1; text-align:center; min-width:100px;">
                  View Details
                </a>
                <%-- Download notice PDF if available --%>
                <c:if test="${not empty t.noticeFilePath}">
                  <a href="${pageContext.request.contextPath}/files/download?type=notice&tenderId=${t.tenderId}"
                     class="btn btn-ghost" target="_blank"
                     style="flex:1; text-align:center; min-width:100px;"
                     title="Download Tender Notice PDF">
                    <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                      <polyline points="7 10 12 15 17 10"/>
                      <line x1="12" y1="15" x2="12" y2="3"/>
                    </svg>
                    Notice PDF
                  </a>
                </c:if>
                <c:if test="${!hasBid}">
                  <a href="${pageContext.request.contextPath}/supplier/bid/submit?tenderId=${t.tenderId}"
                     class="btn btn-primary" style="flex:1; text-align:center; min-width:100px;">
                    Submit Bid
                  </a>
                </c:if>
              </div>
            </div>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>

  </div>
</div>

</body>
</html>