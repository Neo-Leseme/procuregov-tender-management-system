<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Award Notice — ${award.tenderReferenceNo} | ProcureGov</title>
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
      <span>Award Notice</span>
    </div>

    <c:choose>
      <c:when test="${empty award}">
        <div class="empty-state">
          <p>Award notice not found.</p>
          <a href="${pageContext.request.contextPath}/officer/dashboard"
             class="btn btn-outline" style="margin-top:12px;">Back to Dashboard</a>
        </div>
      </c:when>

      <c:otherwise>

        <!-- Official notice header -->
        <div class="award-notice-header">
          <div class="award-notice-header__ministry">
            <div class="navbar__brand-icon" style="width:52px; height:52px; font-size:1.2rem;">PG</div>
            <div>
              <div style="font-family:var(--font-display); font-size:1.1rem; font-weight:600;">ProcureGov</div>
              <div style="font-size:0.78rem; color:var(--clr-text-muted); text-transform:uppercase; letter-spacing:0.08em;">
                Ministry of Public Works — Kingdom of Lesotho
              </div>
            </div>
          </div>
          <div>
            <span class="badge badge-awarded" style="font-size:0.82rem; padding:6px 16px;">
              Contract Awarded
            </span>
          </div>
        </div>

        <!-- Award notice card -->
        <div class="award-notice-card">

          <div class="award-notice-title">
            <span class="section-label">Official Award Notice</span>
            <h2 style="margin-top:6px;">${award.tenderTitle}</h2>
            <span class="ref-no" style="margin-top:8px; display:inline-block;">
              ${award.tenderReferenceNo}
            </span>
          </div>

          <hr class="divider"/>

          <!-- Key award details -->
          <div class="award-details-grid">

            <div class="award-detail-item">
              <div class="award-detail-item__label">Awarded To</div>
              <div class="award-detail-item__value award-detail-item__value--winner">
                ${award.winningSupplierName}
              </div>
            </div>

            <div class="award-detail-item">
              <div class="award-detail-item__label">Contract Value</div>
              <div class="award-detail-item__value">
                <span style="font-size:0.85rem; color:var(--clr-text-muted);">M</span>
                <fmt:formatNumber value="${award.awardedValue}" type="number"
                                  groupingUsed="true" maxFractionDigits="2"/>
              </div>
            </div>

            <div class="award-detail-item">
              <div class="award-detail-item__label">Award Date</div>
              <div class="award-detail-item__value">
                <fmt:formatDate value="${award.awardDate}" pattern="dd MMMM yyyy"/>
              </div>
            </div>

            <div class="award-detail-item">
              <div class="award-detail-item__label">Awarded By</div>
              <div class="award-detail-item__value">${award.officerName}</div>
            </div>

          </div>

          <hr class="divider"/>

          <!-- Justification -->
          <div>
            <span class="detail-label">Award Justification</span>
            <p style="margin-top:var(--space-md); color:var(--clr-text-primary);
                      line-height:1.9; font-size:0.95rem; max-width:680px;">
              ${award.justification}
            </p>
          </div>

          <hr class="divider"/>

          <!-- Footer note -->
          <p class="text-xs" style="color:var(--clr-text-muted); line-height:1.7;">
            This is an official award notice published by the Directorate of ICT,
            Ministry of Public Works, Kingdom of Lesotho via ProcureGov.
            For queries contact
            <a href="mailto:help.procuregov@gov.ls">help.procuregov@gov.ls</a>.
          </p>

        </div>

        <!-- Action buttons -->
        <div style="display:flex; gap:var(--space-md); margin-top:var(--space-xl); flex-wrap:wrap;">
          <c:if test="${loggedInUser.role == 'PROCUREMENT_OFFICER'}">
            <a href="${pageContext.request.contextPath}/officer/eval/panel?tenderId=${award.tenderId}"
               class="btn btn-outline">
              ← Back to Evaluation Panel
            </a>
            <a href="${pageContext.request.contextPath}/officer/dashboard"
               class="btn btn-ghost">
              Dashboard
            </a>
          </c:if>
          <c:if test="${loggedInUser.role == 'SUPPLIER'}">
            <a href="${pageContext.request.contextPath}/supplier/dashboard"
               class="btn btn-ghost">
              ← Back to Dashboard
            </a>
          </c:if>
        </div>

      </c:otherwise>
    </c:choose>

  </div>
</div>

</body>
</html>
