<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  navbar.jsp — shared navigation bar included on every authenticated page.
  Usage: <%@ include file="/WEB-INF/views/common/navbar.jsp" %>
--%>
<nav class="app-navbar">
  <div class="container app-navbar__inner">

    <!-- Brand -->
    <a href="${pageContext.request.contextPath}/" class="app-navbar__brand">
      <div class="navbar__brand-icon">PG</div>
      ProcureGov
    </a>

    <!-- Role-based nav links -->
    <ul class="app-navbar__nav">
      <c:choose>
        <c:when test="${loggedInUser.role == 'PROCUREMENT_OFFICER'}">
          <li><a href="${pageContext.request.contextPath}/officer/dashboard">Dashboard</a></li>
          <li><a href="${pageContext.request.contextPath}/officer/tenders">All Tenders</a></li>
          <li><a href="${pageContext.request.contextPath}/officer/tender/create">New Tender</a></li>
        </c:when>
        <c:when test="${loggedInUser.role == 'SUPPLIER'}">
          <li><a href="${pageContext.request.contextPath}/supplier/dashboard">Dashboard</a></li>
          <li><a href="${pageContext.request.contextPath}/supplier/tenders">Browse Tenders</a></li>
        </c:when>
        <c:when test="${loggedInUser.role == 'EVAL_COMMITTEE'}">
          <li><a href="${pageContext.request.contextPath}/eval/dashboard">Dashboard</a></li>
        </c:when>
      </c:choose>
    </ul>

    <!-- User info + logout -->
    <div class="app-navbar__user">
      <div class="app-navbar__user-info">
        <span class="app-navbar__user-name">${loggedInUser.fullName}</span>
        <span class="app-navbar__user-role">
          <c:choose>
            <c:when test="${loggedInUser.role == 'PROCUREMENT_OFFICER'}">Procurement Officer</c:when>
            <c:when test="${loggedInUser.role == 'EVAL_COMMITTEE'}">Evaluation Committee</c:when>
            <c:otherwise>Supplier</c:otherwise>
          </c:choose>
        </span>
      </div>
      <a href="${pageContext.request.contextPath}/logout" class="btn btn-ghost" style="font-size:0.82rem; padding:8px 14px;">
        <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
          <polyline points="16 17 21 12 16 7"/>
          <line x1="21" y1="12" x2="9" y2="12"/>
        </svg>
        Logout
      </a>
    </div>

  </div>
</nav>