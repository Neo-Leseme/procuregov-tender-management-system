<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Evaluation Panel — ${tender.referenceNo} | ProcureGov</title>
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
      <a href="${pageContext.request.contextPath}/officer/tender/detail?id=${tender.tenderId}">
        ${tender.referenceNo}
      </a>
      <span>›</span>
      <span>Evaluation Panel</span>
    </div>

    <!-- Flash messages -->
    <c:if test="${not empty successMsg}">
      <div class="alert alert-success">${successMsg}</div>
    </c:if>
    <c:if test="${not empty errorMsg}">
      <div class="alert alert-danger">${errorMsg}</div>
    </c:if>

    <!-- Page header -->
    <div class="page-header">
      <div>
        <span class="section-label">Module 4 — Bid Evaluation</span>
        <h1 class="page-title">Evaluation Panel</h1>
        <p style="margin-top:6px; color:var(--clr-text-muted); font-size:0.9rem;">
          ${tender.title} &nbsp;·&nbsp;
          <span class="ref-no">${tender.referenceNo}</span> &nbsp;·&nbsp;
          <span class="badge ${tender.statusBadgeClass}">${tender.status}</span>
        </p>
      </div>
    </div>

    <!-- Scoring formula info card -->
    <div class="card" style="margin-bottom:var(--space-lg); border-left:3px solid var(--clr-gold);">
      <h4 style="margin-bottom:var(--space-md);">Weighted Scoring Formula</h4>
      <div style="display:grid; grid-template-columns:repeat(3,1fr); gap:var(--space-lg);">
        <div>
          <div class="formula-label">Price Score (40%)</div>
          <div class="formula-text">( Lowest Bid ÷ This Bid ) × 100</div>
          <div class="formula-note">Auto-calculated — evaluator does not enter this</div>
        </div>
        <div>
          <div class="formula-label">Technical Score (35%)</div>
          <div class="formula-text">Evaluator enters 0 – 100</div>
          <div class="formula-note">Manual entry per evaluator</div>
        </div>
        <div>
          <div class="formula-label">Timeline Score (25%)</div>
          <div class="formula-text">( Shortest Days ÷ This Days ) × 100</div>
          <div class="formula-note">Auto-calculated — evaluator does not enter this</div>
        </div>
      </div>
      <div style="margin-top:var(--space-md); padding-top:var(--space-md); border-top:1px solid var(--clr-border-subtle); font-family:var(--font-mono); font-size:0.82rem; color:var(--clr-gold);">
        Final Score = Average of all evaluators' [ (Price × 0.40) + (Technical × 0.35) + (Timeline × 0.25) ]
      </div>
    </div>

    <!-- Ranked Leaderboard -->
    <div class="table-card" style="margin-bottom:var(--space-xl);">
      <div class="table-card__header">
        <h3>Ranked Bid Leaderboard</h3>
        <span class="text-sm" style="color:var(--clr-text-muted);">
          Sorted by Final Score — highest first
        </span>
      </div>

      <c:choose>
        <c:when test="${empty rankedBids}">
          <div class="empty-state">
            <p>No bids were submitted for this tender.</p>
          </div>
        </c:when>
        <c:otherwise>
          <div class="table-wrapper">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Rank</th>
                  <th>Supplier</th>
                  <th>Bid Amount (M)</th>
                  <th>Delivery Days</th>
                  <th>Price Score</th>
                  <th>Tech Score</th>
                  <th>Timeline Score</th>
                  <th>Final Score</th>
                  <th>Status</th>
                  <th>Document</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="bid" items="${rankedBids}">
                  <%-- Get scores for this bid from the map --%>
                  <c:set var="bidScores" value="${allScores[bid.bidId]}"/>
                  <tr class="${bid.rank == 1 ? 'top-rank-row' : ''}">

                    <td>
                      <c:choose>
                        <c:when test="${bid.rank == 1}">
                          <span class="rank-badge rank-1">🥇 1st</span>
                        </c:when>
                        <c:when test="${bid.rank == 2}">
                          <span class="rank-badge rank-2">2nd</span>
                        </c:when>
                        <c:when test="${bid.rank == 3}">
                          <span class="rank-badge rank-3">3rd</span>
                        </c:when>
                        <c:otherwise>
                          <span class="rank-badge">${bid.rank}th</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <strong style="color:var(--clr-text-primary);">${bid.supplierName}</strong>
                    </td>

                    <td>
                      <fmt:formatNumber value="${bid.bidAmount}" type="number" groupingUsed="true" maxFractionDigits="2"/>
                    </td>

                    <td>${bid.deliveryDays} days</td>

                    <%-- Show averaged scores if available --%>
                    <c:choose>
                      <c:when test="${not empty bidScores}">
                        <td>
                          <fmt:formatNumber value="${bidScores[0].priceScore}" maxFractionDigits="2" minFractionDigits="2"/>
                        </td>
                        <td>
                          <%-- Average technical score across evaluators --%>
                          <c:set var="techTotal" value="0"/>
                          <c:forEach var="sc" items="${bidScores}">
                            <c:set var="techTotal" value="${techTotal + sc.technicalScore}"/>
                          </c:forEach>
                          <fmt:formatNumber value="${techTotal / bidScores.size()}" maxFractionDigits="2" minFractionDigits="2"/>
                        </td>
                        <td>
                          <fmt:formatNumber value="${bidScores[0].timelineScore}" maxFractionDigits="2" minFractionDigits="2"/>
                        </td>
                        <td>
                          <c:choose>
                            <c:when test="${bid.finalScore != null}">
                              <strong style="color:var(--clr-gold); font-family:var(--font-mono);">
                                <fmt:formatNumber value="${bid.finalScore}" maxFractionDigits="2" minFractionDigits="2"/>
                              </strong>
                            </c:when>
                            <c:otherwise>—</c:otherwise>
                          </c:choose>
                        </td>
                      </c:when>
                      <c:otherwise>
                        <td colspan="4" style="color:var(--clr-text-muted); font-style:italic;">
                          Not yet scored
                        </td>
                      </c:otherwise>
                    </c:choose>

                    <td>
                      <c:choose>
                        <c:when test="${bid.status == 'AWARDED'}">
                          <span class="badge badge-awarded">Awarded</span>
                        </c:when>
                        <c:when test="${bid.status == 'NOT_AWARDED'}">
                          <span class="badge badge-closed">Not Awarded</span>
                        </c:when>
                        <c:otherwise>
                          <span class="badge badge-eval">${bid.status}</span>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <c:if test="${not empty bid.documentPath}">
                        <a href="${pageContext.request.contextPath}/files/download?type=bid&bidId=${bid.bidId}"
                           target="_blank"
                           class="btn btn-ghost"
                           style="font-size:0.75rem; padding:5px 10px;"
                           title="Download supplier's supporting document">
                          <svg width="12" height="12" fill="none" stroke="currentColor"
                               stroke-width="2" viewBox="0 0 24 24">
                            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                            <polyline points="7 10 12 15 17 10"/>
                            <line x1="12" y1="15" x2="12" y2="3"/>
                          </svg>
                          Download
                        </a>
                      </c:if>
                      <c:if test="${empty bid.documentPath}">
                        <span class="text-xs" style="color:var(--clr-text-muted);">None</span>
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

    <!-- Award Form — EVALUATED tenders only. System selects winner automatically. -->
    <c:choose>
      <c:when test="${tender.status == 'EVALUATED' && empty existingAward}">
        <div class="card" style="margin-top:var(--space-xl);
             border:2px solid var(--clr-gold); border-radius:var(--radius-lg);">

          <!-- Header -->
          <div style="display:flex; align-items:center; gap:var(--space-md);
                      margin-bottom:var(--space-lg); padding-bottom:var(--space-lg);
                      border-bottom:1px solid var(--clr-border-subtle);">
            <div style="width:44px; height:44px; background:rgba(201,168,76,0.12);
                        border:1px solid var(--clr-border); border-radius:var(--radius-md);
                        display:flex; align-items:center; justify-content:center; flex-shrink:0;">
              <svg width="20" height="20" fill="none" stroke="var(--clr-gold)"
                   stroke-width="1.5" viewBox="0 0 24 24">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
            </div>
            <div>
              <h3 style="margin:0 0 4px;">Award Contract</h3>
              <p class="text-sm" style="margin:0; color:var(--clr-text-muted);">
                The system has determined the winner based on final evaluation scores.
                Review the result below, provide a justification, then confirm.
              </p>
            </div>
          </div>

          <%-- System-determined winner: rank 1 from the leaderboard --%>
          <c:set var="winner" value="${rankedBids[0]}"/>

          <!-- Winner display — read-only, officer cannot change this -->
          <div style="background:var(--clr-bg-elevated);
                      border:1px solid var(--clr-border-subtle);
                      border-left:4px solid var(--clr-gold);
                      border-radius:var(--radius-md);
                      padding:var(--space-lg); margin-bottom:var(--space-xl);">

            <div class="detail-label" style="margin-bottom:var(--space-md);">
              System-Selected Winner
              <span style="font-size:0.72rem; color:var(--clr-text-muted); font-weight:400; margin-left:8px;">
                (Highest composite score — not editable)
              </span>
            </div>

            <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(150px,1fr));
                        gap:var(--space-lg);">
              <div>
                <div class="detail-label">Supplier</div>
                <div style="color:var(--clr-gold); font-weight:700;
                            font-size:1.05rem; margin-top:5px;">
                  ${winner.supplierName}
                </div>
              </div>
              <div>
                <div class="detail-label">Final Score</div>
                <div style="color:var(--clr-text-primary); font-weight:700;
                            font-size:1.05rem; margin-top:5px;">
                  <fmt:formatNumber value="${winner.finalScore}" maxFractionDigits="2"/>
                  <span style="font-size:0.75rem; color:var(--clr-text-muted);">/ 100</span>
                </div>
              </div>
              <div>
                <div class="detail-label">Bid Amount (Contract Value)</div>
                <div style="color:var(--clr-text-primary); font-weight:700;
                            font-size:1.05rem; margin-top:5px;">
                  M <fmt:formatNumber value="${winner.bidAmount}" type="number"
                                      groupingUsed="true" maxFractionDigits="2"/>
                </div>
              </div>
              <div>
                <div class="detail-label">Rank</div>
                <div style="color:var(--clr-text-primary); font-weight:700;
                            font-size:1.05rem; margin-top:5px;">
                  #1 of ${rankedBids.size()}
                </div>
              </div>
            </div>
          </div>

          <!-- Award form — only field is justification -->
          <form method="POST"
                action="${pageContext.request.contextPath}/officer/tender/award"
                id="awardForm">

            <input type="hidden" name="tenderId" value="${tender.tenderId}"/>
            <%-- No winningBidId or awardedValue — EvalServlet determines these from scores --%>

            <div class="form-group" style="margin-bottom:var(--space-xl);">
              <label class="form-label" for="justification">
                Official Award Justification
                <span style="color:var(--clr-danger)">*</span>
              </label>
              <textarea class="form-input"
                        id="justification"
                        name="justification"
                        rows="4"
                        maxlength="600"
                        required
                        placeholder="State the official justification for awarding this contract to the highest-scoring bidder. Reference the evaluation criteria, scoring outcome, and compliance with Ministry procurement policy..."
                        style="resize:vertical; min-height:110px;"></textarea>
              <span class="form-hint">
                This justification will appear on the published award notice visible to all
                bidding suppliers. Maximum 600 characters.
              </span>
            </div>

            <div style="display:flex; gap:var(--space-md); flex-wrap:wrap;">
              <button type="submit"
                      class="btn btn-primary btn--lg"
                      onclick="return confirm(
                        'Award this contract to ${winner.supplierName}?

This action is FINAL and cannot be undone.
All bidding suppliers will be notified of the outcome.');">
                <svg width="16" height="16" fill="none" stroke="currentColor"
                     stroke-width="2" viewBox="0 0 24 24" style="flex-shrink:0;">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
                Confirm &amp; Award Contract
              </button>
              <a href="${pageContext.request.contextPath}/officer/tender/detail?id=${tender.tenderId}"
                 class="btn btn-ghost btn--lg">Cancel</a>
            </div>

          </form>
        </div>
      </c:when>

      <c:when test="${tender.status == 'AWARDED' && not empty existingAward}">
        <!-- Award notice summary -->
        <div class="card" style="border-top:3px solid var(--clr-success);">
          <h3 style="margin-bottom:var(--space-lg); color:var(--clr-success);">Contract Awarded</h3>
          <div class="detail-row">
            <span class="detail-label">Winner</span>
            <span class="detail-value"><strong>${existingAward.winningSupplierName}</strong></span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Awarded Value</span>
            <span class="detail-value">
              M <fmt:formatNumber value="${existingAward.awardedValue}" type="number" groupingUsed="true" maxFractionDigits="2"/>
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Award Date</span>
            <span class="detail-value">
              <fmt:formatDate value="${existingAward.awardDate}" pattern="dd MMMM yyyy 'at' HH:mm"/>
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Awarded By</span>
            <span class="detail-value">${existingAward.officerName}</span>
          </div>
          <div style="margin-top:var(--space-lg);">
            <span class="detail-label">Justification</span>
            <p style="margin-top:8px; color:var(--clr-text-primary); line-height:1.8;">
              ${existingAward.justification}
            </p>
          </div>
        </div>
      </c:when>
    </c:choose>

  </div>
</div>

</body>
</html>