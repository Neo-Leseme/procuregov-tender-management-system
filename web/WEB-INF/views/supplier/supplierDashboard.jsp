<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Supplier Dashboard | ProcureGov</title>
  <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/images/images.png"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
</head>
<body>

<%@ include file="/WEB-INF/views/common/navbar.jsp" %>

<div class="page-content">
  <div class="container">

    <div class="page-header">
      <div>
        <span class="section-label">Supplier Portal</span>
        <h1 class="page-title">Welcome, ${loggedInUser.fullName}</h1>
        <c:if test="${not empty supplier}">
          <p style="margin-top:4px; color:var(--clr-text-muted); font-size:0.88rem;">
            Reg. No: <span class="ref-no">${supplier.registrationNo}</span>
          </p>
        </c:if>
      </div>
      <a href="${pageContext.request.contextPath}/supplier/tenders" class="btn btn-primary">
        Browse Open Tenders
      </a>
    </div>

    <!-- Flash messages -->
    <c:if test="${not empty successMsg}">
      <div class="alert alert-success">${successMsg}</div>
    </c:if>
    <c:if test="${not empty errorMsg}">
      <div class="alert alert-danger">${errorMsg}</div>
    </c:if>

    <!-- Open tenders summary -->
    <div class="table-card" style="margin-bottom:var(--space-xl);">
      <div class="table-card__header">
        <h3>Currently Open Tenders</h3>
        <a href="${pageContext.request.contextPath}/supplier/tenders"
           class="btn btn-ghost" style="font-size:0.82rem; padding:8px 14px;">View all</a>
      </div>
      <c:choose>
        <c:when test="${empty openTenders}">
          <div class="empty-state"><p>No tenders are currently open for bidding.</p></div>
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
                  <th>Closes</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="t" items="${openTenders}">
                  <tr>
                    <td><span class="ref-no">${t.referenceNo}</span></td>
                    <td>${t.title}</td>
                    <td>${t.category.label}</td>
                    <td><fmt:formatNumber value="${t.estimatedValue}" type="number" groupingUsed="true" maxFractionDigits="0"/></td>
                    <td>
                      <fmt:formatDate value="${t.closingDatetime}" pattern="dd MMM yyyy HH:mm"/>
                    </td>
                    <td>
                      <a href="${pageContext.request.contextPath}/supplier/tender/detail?id=${t.tenderId}"
                         class="btn btn-primary" style="font-size:0.78rem; padding:6px 14px;">
                        View &amp; Bid
                      </a>
                    </td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </c:otherwise>
      </c:choose>
    </div>

    <!-- My submitted bids -->
    <div class="table-card">
      <div class="table-card__header">
        <h3>My Submitted Bids</h3>
      </div>
      <c:choose>
        <c:when test="${empty myBids}">
          <div class="empty-state">
            <p>You have not submitted any bids yet.</p>
            <a href="${pageContext.request.contextPath}/supplier/tenders"
               class="btn btn-outline" style="margin-top:12px;">Browse Open Tenders</a>
          </div>
        </c:when>
        <c:otherwise>
          <div class="table-wrapper">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Tender ID</th>
                  <th>Bid Amount (M)</th>
                  <th>Delivery Days</th>
                  <th>Submitted</th>
                  <th>Status</th>
                  <th>Award Notice</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="b" items="${myBids}">
                  <tr>
                    <td>
                      <a href="${pageContext.request.contextPath}/supplier/tender/detail?id=${b.tenderId}"
                         class="ref-no">Tender #${b.tenderId}</a>
                    </td>
                    <td><fmt:formatNumber value="${b.bidAmount}" type="number" groupingUsed="true" maxFractionDigits="2"/></td>
                    <td>${b.deliveryDays} days</td>
                    <td><fmt:formatDate value="${b.submittedAt}" pattern="dd MMM yyyy HH:mm"/></td>
                    <td>
                      <c:choose>
                        <c:when test="${b.status == 'AWARDED'}">
                          <span class="badge badge-awarded">✓ Won</span>
                        </c:when>
                        <c:when test="${b.status == 'NOT_AWARDED'}">
                          <span class="badge badge-closed">Not Awarded</span>
                        </c:when>
                        <c:when test="${b.status == 'UNDER_EVALUATION'}">
                          <span class="badge badge-eval">Under Evaluation</span>
                        </c:when>
                        <c:otherwise>
                          <span class="badge badge-eval">${b.status}</span>
                        </c:otherwise>
                      </c:choose>
                    </td>
                    <td>
                      <%-- Show award notice for any outcome once tender is awarded --%>
                      <c:if test="${b.status == 'AWARDED' || b.status == 'NOT_AWARDED'}">
                        <a href="${pageContext.request.contextPath}/supplier/award-notice?tenderId=${b.tenderId}"
                           class="btn btn-ghost" style="font-size:0.78rem; padding:5px 10px;">
                          View Award Notice
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