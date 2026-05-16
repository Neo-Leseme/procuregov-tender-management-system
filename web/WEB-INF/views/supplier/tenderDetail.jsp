<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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

    <div class="breadcrumb">
      <a href="${pageContext.request.contextPath}/supplier/dashboard">Dashboard</a>
      <span>›</span>
      <a href="${pageContext.request.contextPath}/supplier/tenders">Open Tenders</a>
      <span>›</span>
      <span>${tender.referenceNo}</span>
    </div>

    <c:if test="${not empty successMsg}"><div class="alert alert-success">${successMsg}</div></c:if>
    <c:if test="${not empty errorMsg}"><div class="alert alert-danger">${errorMsg}</div></c:if>

    <!-- Page header -->
    <div class="page-header">
      <div>
        <span class="ref-no">${tender.referenceNo}</span>
        <h1 class="page-title" style="margin-top:6px;">${tender.title}</h1>
        <span class="badge ${tender.statusBadgeClass}" style="margin-top:8px; display:inline-block;">
          ${tender.status}
        </span>
      </div>

      <!-- Action buttons -->
      <div style="display:flex; gap:12px; flex-wrap:wrap; align-items:center;">

        <%-- Download tender notice PDF — visible to all suppliers --%>
        <c:if test="${not empty tender.noticeFilePath}">
          <a href="${pageContext.request.contextPath}/files/download?type=notice&tenderId=${tender.tenderId}"
             class="btn btn-outline"
             target="_blank"
             title="Download the official tender notice document">
            <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="7 10 12 15 17 10"/>
              <line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            Download Tender Notice (PDF)
          </a>
        </c:if>

        <%-- Submit bid button — only if open and not yet bid --%>
        <c:if test="${isStillOpen && !hasBid}">
          <a href="${pageContext.request.contextPath}/supplier/bid/submit?tenderId=${tender.tenderId}"
             class="btn btn-primary">
            <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <path d="M22 2L11 13"/><path d="M22 2L15 22 11 13 2 9l20-7z"/>
            </svg>
            Submit Your Bid
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
            Tender Information
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
              M <fmt:formatNumber value="${tender.estimatedValue}" type="number"
                                  groupingUsed="true" maxFractionDigits="2"/>
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Submission Deadline</span>
            <span class="detail-value" style="color:var(--clr-danger); font-weight:500;">
              <fmt:formatDate value="${tender.closingDatetime}" pattern="dd MMMM yyyy 'at' HH:mm"/>
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Status</span>
            <span class="detail-value">
              <span class="badge ${tender.statusBadgeClass}">${tender.status}</span>
            </span>
          </div>

          <div style="margin-top:var(--space-lg);">
            <span class="detail-label">Description / Scope of Work</span>
            <p style="margin-top:8px; color:var(--clr-text-primary); line-height:1.9;">
              ${tender.description}
            </p>
          </div>
        </div>

        <%-- Tender notice document download card --%>
        <c:if test="${not empty tender.noticeFilePath}">
          <div class="card" style="border-left:3px solid var(--clr-gold);">
            <div style="display:flex; align-items:center; gap:var(--space-md);">
              <div style="width:48px; height:48px; background:rgba(201,168,76,0.12);
                          border:1px solid var(--clr-border); border-radius:var(--radius-md);
                          display:flex; align-items:center; justify-content:center;
                          flex-shrink:0;">
                <svg width="22" height="22" fill="none" stroke="var(--clr-gold)"
                     stroke-width="1.5" viewBox="0 0 24 24">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                  <polyline points="10 9 9 9 8 9"/>
                </svg>
              </div>
              <div style="flex:1;">
                <div style="font-weight:600; color:var(--clr-text-primary); margin-bottom:3px;">
                  Official Tender Notice Document
                </div>
                <div class="text-sm" style="color:var(--clr-text-muted);">
                  Download the full tender specifications, terms, and requirements.
                  Read carefully before submitting your bid.
                </div>
              </div>
              <a href="${pageContext.request.contextPath}/files/download?type=notice&tenderId=${tender.tenderId}"
                 class="btn btn-primary"
                 target="_blank">
                <svg width="16" height="16" fill="none" stroke="currentColor"
                     stroke-width="2" viewBox="0 0 24 24">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                  <polyline points="7 10 12 15 17 10"/>
                  <line x1="12" y1="15" x2="12" y2="3"/>
                </svg>
                Download PDF
              </a>
            </div>
          </div>
        </c:if>

        <c:if test="${empty tender.noticeFilePath}">
          <div class="card" style="border-left:3px solid var(--clr-border-subtle);">
            <p class="text-sm" style="color:var(--clr-text-muted); margin:0;">
              No tender notice document has been uploaded for this tender.
              Contact <a href="mailto:help.procuregov@gov.ls">help.procuregov@gov.ls</a>
              for further information.
            </p>
          </div>
        </c:if>
      </div>

      <!-- Right: Bid status panel -->
      <div>
        <div class="card">
          <h4 style="margin-bottom:var(--space-md);">Your Bid Status</h4>

          <c:choose>
            <c:when test="${hasBid}">
              <div class="alert alert-success">
                <svg width="16" height="16" fill="none" stroke="currentColor"
                     stroke-width="2" viewBox="0 0 24 24" style="flex-shrink:0;">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
                You have submitted a bid for this tender.
              </div>
              <p class="text-sm" style="color:var(--clr-text-muted); margin-top:8px;">
                You cannot submit another bid. Track your bid status on your dashboard.
              </p>
              <c:if test="${tender.status == 'AWARDED'}">
                <a href="${pageContext.request.contextPath}/supplier/award-notice?tenderId=${tender.tenderId}"
                   class="btn btn-outline btn--full" style="margin-top:12px;">
                  View Award Notice
                </a>
              </c:if>
            </c:when>

            <c:when test="${isStillOpen}">
              <p class="text-sm" style="margin-bottom:var(--space-md); color:var(--clr-text-secondary);">
                This tender is open. Download the notice document, review the requirements,
                then submit your bid before the deadline.
              </p>
              <a href="${pageContext.request.contextPath}/supplier/bid/submit?tenderId=${tender.tenderId}"
                 class="btn btn-primary btn--full">
                Submit Your Bid
              </a>
              <p class="text-xs" style="color:var(--clr-text-muted); margin-top:8px; text-align:center;">
                One bid per tender. Cannot be edited after submission.
              </p>
            </c:when>

            <c:otherwise>
              <div class="alert alert-warning">
                This tender is no longer accepting bids.
              </div>
              <c:if test="${tender.status == 'AWARDED'}">
                <a href="${pageContext.request.contextPath}/supplier/award-notice?tenderId=${tender.tenderId}"
                   class="btn btn-outline btn--full" style="margin-top:12px;">
                  View Award Notice
                </a>
              </c:if>
            </c:otherwise>
          </c:choose>
        </div>

        <!-- Key dates card -->
        <div class="card" style="margin-top:var(--space-lg);">
          <h4 style="margin-bottom:var(--space-md);">Key Dates</h4>
          <div class="detail-row">
            <span class="detail-label">Published</span>
            <span class="detail-value">
              <fmt:formatDate value="${tender.createdAt}" pattern="dd MMM yyyy"/>
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Closing</span>
            <span class="detail-value" style="color:var(--clr-danger);">
              <fmt:formatDate value="${tender.closingDatetime}" pattern="dd MMM yyyy HH:mm"/>
            </span>
          </div>
        </div>

      </div>
    </div>

  </div>
</div>

</body>
</html>