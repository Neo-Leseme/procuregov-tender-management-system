<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Forgot Password | ProcureGov</title>
  <link rel="icon" type="image/x-icon" href="images/images.png"/>
  <link rel="stylesheet" href="css/style.css"/>
</head>
<body>

<div class="auth-page">

  <!-- ══ LEFT — Visual Panel ════════════════════════════════ -->
  <aside class="auth-visual">
    <img class="auth-visual__img" src="images/Lesotho.jpg"
         alt="Government building — Ministry of Public Works, Lesotho"/>
    <div class="auth-visual__overlay"></div>
    <div class="auth-visual__content">
      <div class="auth-visual__badge">
        <svg width="10" height="10" viewBox="0 0 10 10" fill="none">
          <circle cx="5" cy="5" r="4" stroke="currentColor" stroke-width="1.5"/>
          <circle cx="5" cy="5" r="1.5" fill="currentColor"/>
        </svg>
        Ministry of Public Works &nbsp;·&nbsp; Lesotho
      </div>
      <h2 class="auth-visual__title">Account Recovery</h2>
      <p class="auth-visual__subtitle">
        Enter your registered email address. If it matches our records,
        a 6‑digit verification code will be sent to your inbox.
      </p>
      <div class="auth-visual__features">
        <div class="auth-feature">
          <span class="auth-feature__dot"></span>
          Check your spam folder if you don't see it
        </div>
        <div class="auth-feature">
          <span class="auth-feature__dot"></span>
          Code expires after 15 minutes
        </div>
        <div class="auth-feature">
          <span class="auth-feature__dot"></span>
          If you didn't request this, report it
        </div>
      </div>
    </div>
  </aside>

  <!-- ══ RIGHT — Form Panel ════════════════════════════════ -->
  <main class="auth-form-panel">

    <div class="auth-header">
      <a href="index.jsp" class="auth-header__brand">
        <div class="auth-header__brand-icon">PG</div>
        ProcureGov
      </a>
      <h2 style="margin-bottom:8px;">Forgot your password?</h2>
      <p class="text-sm">We'll send a verification code to your email</p>
    </div>

    <%-- Flash messages --%>
    <c:if test="${not empty errorMsg}">
      <div class="alert alert-danger">
        <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"
             viewBox="0 0 24 24" style="flex-shrink:0;margin-top:2px">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/>
          <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        ${errorMsg}
      </div>
    </c:if>

    <c:if test="${not empty successMsg}">
      <div class="alert alert-success">
        <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"
             viewBox="0 0 24 24" style="flex-shrink:0;margin-top:2px">
          <polyline points="20 6 9 17 4 12"/>
        </svg>
        ${successMsg}
      </div>
    </c:if>

    <form class="auth-form" action="${pageContext.request.contextPath}/forgot-password"
          method="POST" novalidate>

      <div class="form-group">
        <label class="form-label" for="email">Registered Email Address</label>
        <div class="input-wrapper">
          <svg class="input-icon" fill="none" stroke="currentColor" stroke-width="1.5"
               viewBox="0 0 24 24">
            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
            <polyline points="22,6 12,13 2,6"/>
          </svg>
          <input class="form-input form-input--icon"
                 type="email" id="email" name="email"
                 placeholder="you@example.com"
                 value="<c:out value='${param.email}'/>"
                 autocomplete="email" required/>
        </div>
      </div>

      <button type="submit" class="btn btn-primary btn--full btn--lg" style="margin-top:8px;">
        Send Verification Code
      </button>
    </form>

    <div class="auth-footer-link" style="margin-top:var(--space-xl);">
      <a href="${pageContext.request.contextPath}/login"
         style="font-size:0.88rem; color:var(--clr-text-muted);">
        ← Back to Sign In
      </a>
    </div>

    <div class="auth-footer-link" style="margin-top:32px;">
      <p class="text-xs" style="color:var(--clr-text-muted); line-height:1.6;">
        If you did not request a password reset, please report it immediately to<br/>
        <a href="mailto:help.procuregov@gov.ls" style="font-size:inherit;">
          help.procuregov@gov.ls
        </a>
      </p>
    </div>

  </main>
</div>

</body>
</html>