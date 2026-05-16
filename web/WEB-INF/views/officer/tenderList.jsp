<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>All Tenders | ProcureGov</title>
  <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/images/images.png"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
</head>
<body>

<%@ include file="/WEB-INF/views/common/navbar.jsp" %>

<div class="page-content">
  <div class="container">

    <div class="page-header">
      <div>
        <span class="section-label">Tender Management</span>
        <h1 class="page-title">All Tenders</h1>
      </div>
      <a href="${pageContext.request.contextPath}/officer/tender/create" class="btn btn-primary">
        + New Tender
      </a>
    </div>

    <!-- Flash messages -->
    <c:if test="${not empty sessionScope.successMsg}">
      <div class="alert alert-success">${sessionScope.successMsg}</div>
      <c:remove var="successMsg" scope="session"/>
    </c:if>

    <!-- Filter bar — JSTL only, no scriptlets -->
    <form method="GET" action="${pageContext.request.contextPath}/officer/tenders" class="filter-bar">
      <div class="filter-bar__group">
        <label class="form-label">Status</label>
        <select class="form-select" name="status" onchange="this.form.submit()">
          <option value="">All Statuses</option>
          <c:forEach var="s" items="${statuses}">
            <option value="${s.name()}" <c:if test="${selectedStatus == s.name()}">selected</c:if>>
              ${s.name()}
            </option>
          </c:forEach>
        </select>
      </div>
      <div class="filter-bar__group">
        <label class="form-label">Category</label>
        <select class="form-select" name="category" onchange="this.form.submit()">
          <option value="">All Categories</option>
          <c:forEach var="cat" items="${categories}">
            <option value="${cat.name()}" <c:if test="${selectedCat == cat.name()}">selected</c:if>>
              ${cat.label}
            </option>
          </c:forEach>
        </select>
      </div>
      <c:if test="${not empty selectedStatus || not empty selectedCat}">
        <a href="${pageContext.request.contextPath}/officer/tenders" class="btn btn-ghost">
          Clear filters
        </a>
      </c:if>
    </form>

    <!-- Results count -->
    <p class="text-sm" style="margin-bottom:var(--space-md); color:var(--clr-text-muted);">
      Showing <strong>${tenders.size()}</strong> tender<c:if test="${tenders.size() != 1}">s</c:if>
      <c:if test="${not empty selectedStatus}"> with status <strong>${selectedStatus}</strong></c:if>
      <c:if test="${not empty selectedCat}"> in category <strong>${selectedCat}</strong></c:if>
    </p>

    <!-- Table -->
    <div class="table-card">
      <c:choose>
        <c:when test="${empty tenders}">
          <div class="empty-state">
            <p>No tenders match the selected filters.</p>
            <a href="${pageContext.request.contextPath}/officer/tenders" class="btn btn-outline" style="margin-top:12px;">Clear filters</a>
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
                  <th>Est. Value (M)</th>
                  <th>Closing</th>
                  <th>Bids</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="t" items="${tenders}">
                  <tr>
                    <td><span class="ref-no">${t.referenceNo}</span></td>
                    <td>${t.title}</td>
                    <td>${t.category.label}</td>
                    <td>
                      <fmt:formatNumber value="${t.estimatedValue}" type="number" groupingUsed="true" maxFractionDigits="0"/>
                    </td>
                    <td>
                      <fmt:formatDate value="${t.closingDatetime}" pattern="dd MMM yyyy"/>
                      <br/>
                      <span class="text-xs" style="color:var(--clr-text-muted);">
                        <fmt:formatDate value="${t.closingDatetime}" pattern="HH:mm"/>
                      </span>
                    </td>
                    <td>
                      <span class="bid-count">${t.bidCount}</span>
                    </td>
                    <td>
                      <span class="badge ${t.statusBadgeClass}">${t.status}</span>
                    </td>
                    <td style="white-space:nowrap;">
                      <a href="${pageContext.request.contextPath}/officer/tender/detail?id=${t.tenderId}"
                         class="btn btn-ghost" style="font-size:0.78rem; padding:5px 10px;">
                        View
                      </a>
                      <c:if test="${t.status == 'DRAFT'}">
                        <a href="${pageContext.request.contextPath}/officer/tender/edit?id=${t.tenderId}"
                           class="btn btn-outline" style="font-size:0.78rem; padding:5px 10px;">
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
