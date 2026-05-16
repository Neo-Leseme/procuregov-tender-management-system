<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Officer Dashboard | ProcureGov</title>
  <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/images/images.png"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
</head>
<body>

<%@ include file="/WEB-INF/views/common/navbar.jsp" %>

<div class="page-content">
  <div class="container">

    <!-- Page header -->
    <div class="page-header">
      <div>
        <span class="section-label">Procurement Officer</span>
        <h1 class="page-title">Dashboard</h1>
      </div>
      <a href="${pageContext.request.contextPath}/officer/tender/create" class="btn btn-primary">
        <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
          <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        New Tender
      </a>
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

    <!-- Summary cards -->
    <div class="stats-cards">

      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--draft">
          <svg width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__number">${draftCount}</div>
          <div class="stat-card__label">Draft</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--open">
          <svg width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__number">${openCount}</div>
          <div class="stat-card__label">Open</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--eval">
          <svg width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__number">${closedCount}</div>
          <div class="stat-card__label">Under Review</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--awarded">
          <svg width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__number">${awardedCount}</div>
          <div class="stat-card__label">Awarded</div>
        </div>
      </div>

    </div>

    <!-- Tender list table -->
    <div class="table-card">
      <div class="table-card__header">
        <h3>All Tenders</h3>
        <a href="${pageContext.request.contextPath}/officer/tenders" class="btn btn-ghost" style="font-size:0.82rem; padding:8px 14px;">
          View with filters
        </a>
      </div>

      <c:choose>
        <c:when test="${empty allTenders}">
          <div class="empty-state">
            <p>No tenders found. <a href="${pageContext.request.contextPath}/officer/tender/create">Create your first tender</a>.</p>
          </div>
        </c:when>
        <c:otherwise>
          <div class="table-wrapper">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Reference</th>
                  <th>Title</th>
                  <th>Category</th>
                  <th>Closing Date</th>
                  <th>Bids</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="t" items="${allTenders}">
                  <tr>
                    <td><span class="ref-no">${t.referenceNo}</span></td>
                    <td>${t.title}</td>
                    <td>${t.category.label}</td>
                    <td>
                      <fmt:formatDate value="${t.closingDatetime}" pattern="dd MMM yyyy HH:mm"/>
                    </td>
                    <td><span class="bid-count">${t.bidCount}</span></td>
                    <td>
                      <span class="badge ${t.statusBadgeClass}">
                        ${t.status}
                      </span>
                    </td>
                    <td>
                      <a href="${pageContext.request.contextPath}/officer/tender/detail?id=${t.tenderId}"
                         class="btn btn-ghost" style="font-size:0.78rem; padding:6px 12px;">
                        View
                      </a>
                      <c:if test="${t.status == 'DRAFT'}">
                        <a href="${pageContext.request.contextPath}/officer/tender/edit?id=${t.tenderId}"
                           class="btn btn-outline" style="font-size:0.78rem; padding:6px 12px;">
                          Edit
                        </a>
                      </c:if>
                    </td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </c:otherwise>
      </c:choose>
    </div>

  </div>
</div>

</body>
</html>
