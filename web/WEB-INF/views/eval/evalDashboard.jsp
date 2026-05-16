<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        <title>Evaluator Dashboard | ProcureGov</title>
        <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/images/images.png"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
    </head>
    <body>

        <%@ include file="/WEB-INF/views/common/navbar.jsp" %>

        <div class="page-content">
            <div class="container">

                <div class="page-header">
                    <div>
                        <span class="section-label">Evaluation Committee</span>
                        <h1 class="page-title">Evaluator Dashboard</h1>
                        <p style="margin-top:4px; color:var(--clr-text-muted); font-size:0.88rem;">
                            Score bids for tenders assigned to you. You can only access tenders in Closed or Under Evaluation status.
                        </p>
                    </div>
                </div>

                <!-- Under Evaluation tenders -->
                <div class="table-card" style="margin-bottom:var(--space-xl);">
                    <div class="table-card__header">
                        <h3>Under Evaluation — Requires Your Scores</h3>
                        <span class="badge badge-eval">${underEvalTenders.size()} tender(s)</span>
                    </div>
                    <c:choose>
                        <c:when test="${empty underEvalTenders}">
                            <div class="empty-state"><p>No tenders are currently under evaluation.</p></div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-wrapper">
                                <table class="data-table">
                                    <thead>
                                        <tr>
                                            <th>Reference</th>
                                            <th>Title</th>
                                            <th>Category</th>
                                            <th>Total Bids</th>
                                            <th>Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="t" items="${underEvalTenders}">
                                            <tr>
                                                <td><span class="ref-no">${t.referenceNo}</span></td>
                                                <td>${t.title}</td>
                                                <td>${t.category.label}</td>
                                                <td><span class="bid-count">${t.bidCount}</span></td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/eval/panel?tenderId=${t.tenderId}"
                                                       class="btn btn-primary" style="font-size:0.78rem; padding:6px 14px;">
                                                        Score Bids
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

                <!-- Closed tenders (accessible but awaiting Under Evaluation trigger) -->
                <div class="table-card" style="margin-bottom:var(--space-xl);">
                    <div class="table-card__header">
                        <h3>Closed — Awaiting Evaluation Start</h3>
                        <span class="badge badge-closed">${closedTenders.size()} tender(s)</span>
                    </div>
                    <c:choose>
                        <c:when test="${empty closedTenders}">
                            <div class="empty-state"><p>No closed tenders awaiting evaluation.</p></div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-wrapper">
                                <table class="data-table">
                                    <thead>
                                        <tr>
                                            <th>Reference</th>
                                            <th>Title</th>
                                            <th>Category</th>
                                            <th>Bids</th>
                                            <th>Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="t" items="${closedTenders}">
                                            <tr>
                                                <td><span class="ref-no">${t.referenceNo}</span></td>
                                                <td>${t.title}</td>
                                                <td>${t.category.label}</td>
                                                <td><span class="bid-count">${t.bidCount}</span></td>
                                                <td><span class="badge badge-closed">Closed</span></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Evaluated / completed -->
                <c:if test="${not empty evaluatedTenders}">
                    <div class="table-card">
                        <div class="table-card__header">
                            <h3>Completed Evaluations</h3>
                            <span class="badge badge-evaluated">${evaluatedTenders.size()} tender(s)</span>
                        </div>
                        <div class="table-wrapper">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Reference</th>
                                        <th>Title</th>
                                        <th>Status</th>
                                        <th>View Scores</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="t" items="${evaluatedTenders}">
                                        <tr>
                                            <td><span class="ref-no">${t.referenceNo}</span></td>
                                            <td>${t.title}</td>
                                            <td><span class="badge badge-evaluated">${t.status}</span></td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/eval/panel?tenderId=${t.tenderId}"
                                                   class="btn btn-ghost" style="font-size:0.78rem; padding:5px 10px;">
                                                    View
                                                </a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </c:if>

            </div>
        </div>

    </body>
</html>