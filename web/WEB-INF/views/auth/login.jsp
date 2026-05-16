<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Sign In | ProcureGov</title>
  <link rel="icon" type="image/x-icon" href="images/images.png"/>
  <link rel="stylesheet" href="css/style.css"/>
</head>
<body>

<div class="auth-page">

  <!-- ══ LEFT — Visual Panel ════════════════════════════════ -->
  <aside class="auth-visual">
    <img
      class="auth-visual__img"
      src="images/Lesotho.jpg"
      alt="Government building — Ministry of Public Works, Lesotho"
    />
    <div class="auth-visual__overlay"></div>

    <div class="auth-visual__content">
      <div class="auth-visual__badge">
        <svg width="10" height="10" viewBox="0 0 10 10" fill="none">
          <circle cx="5" cy="5" r="4" stroke="currentColor" stroke-width="1.5"/>
          <circle cx="5" cy="5" r="1.5" fill="currentColor"/>
        </svg>
        Ministry of Public Works &nbsp;·&nbsp; Lesotho
      </div>

      <h2 class="auth-visual__title">
        Transparent Procurement<br/>for a Stronger Lesotho
      </h2>

      <p class="auth-visual__subtitle">
        ProcureGov brings the full government tender lifecycle
        online — fair, secure, and fully auditable.
      </p>

      <div class="auth-visual__features">
        <div class="auth-feature">
          <span class="auth-feature__dot"></span>
          End-to-end digital tender management
        </div>
        <div class="auth-feature">
          <span class="auth-feature__dot"></span>
          SHA-256 encrypted credentials
        </div>
        <div class="auth-feature">
          <span class="auth-feature__dot"></span>
          Role-based access for all user types
        </div>
        <div class="auth-feature">
          <span class="auth-feature__dot"></span>
          Weighted, committee-scored evaluation
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

      <h2 style="margin-bottom:8px;">Welcome back</h2>
      <p class="text-sm">Sign in to your account to continue</p>
    </div>

    <%-- Flash messages from AuthServlet --%>
    <c:if test="${not empty errorMsg}">
      <div class="alert alert-danger">
        <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" style="flex-shrink:0;margin-top:2px">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        ${errorMsg}
      </div>
    </c:if>

    <c:if test="${not empty successMsg}">
      <div class="alert alert-success">
        <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" style="flex-shrink:0;margin-top:2px">
          <polyline points="20 6 9 17 4 12"/>
        </svg>
        ${successMsg}
      </div>
    </c:if>

    <form class="auth-form" action="${pageContext.request.contextPath}/login" method="POST" novalidate>

      <!-- Email -->
      <div class="form-group">
        <label class="form-label" for="email">Email Address</label>
        <div class="input-wrapper">
          <svg class="input-icon" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
            <polyline points="22,6 12,13 2,6"/>
          </svg>
          <input
            class="form-input form-input--icon"
            type="email"
            id="email"
            name="email"
            placeholder="you@example.com"
            value="<c:out value='${param.email}'/>"
            autocomplete="email"
            required
          />
        </div>
      </div>

      <!-- Password -->
      <div class="form-group">
        <label class="form-label" for="password">Password</label>
        <div class="input-wrapper">
          <svg class="input-icon" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
          </svg>
          <input
            class="form-input form-input--icon"
            type="password"
            id="password"
            name="password"
            placeholder="••••••••"
            autocomplete="current-password"
            required
            style="padding-right:44px"
          />
          <button type="button" class="input-toggle" id="togglePassword" aria-label="Toggle password visibility">
            <svg id="eyeIcon" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
              <circle cx="12" cy="12" r="3"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- Lockout warning -->
      <c:if test="${loginAttempts >= 2}">
        <div class="alert alert-warning" style="font-size:0.82rem;">
          <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" style="flex-shrink:0">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
            <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
          Warning: ${loginAttempts} failed attempt(s). Account locks after 3 consecutive failures.
        </div>
      </c:if>

      <button type="submit" class="btn btn-primary btn--full btn--lg" style="margin-top:8px;">
        Sign In to ProcureGov
      </button>

      <!-- Forgot password link -->
      <div style="text-align:left; margin-top:-4px;">
          Forgot your password?
        <a href="${pageContext.request.contextPath}/forgot-password"
           style="font-size:2.00 rem;">
          Reset Password
        </a>
      </div>

    </form>

    <div class="divider-text">or</div>

    <div class="auth-footer-link">
      New supplier? <a href="${pageContext.request.contextPath}/register">Create an account</a>
    </div>

    <div class="auth-footer-link" style="margin-top:32px;">
      <p class="text-xs" style="color:var(--clr-text-muted); line-height:1.6;">
        Ministry staff accounts are provisioned by the ICT Directorate.<br/>
        Contact <a href="mailto:help.procuregov@gov.ls" style="font-size:inherit;">help.procuregov@gov.ls</a> for access.
      </p>
    </div>

  </main>
</div>

<script>
  const toggle   = document.getElementById('togglePassword');
  const pwInput  = document.getElementById('password');
  const eyeIcon  = document.getElementById('eyeIcon');

  const eyeOpen  = `<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>`;
  const eyeClosed= `<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/>`;

  toggle.addEventListener('click', () => {
    const isPass = pwInput.type === 'password';
    pwInput.type = isPass ? 'text' : 'password';
    eyeIcon.innerHTML = isPass ? eyeClosed : eyeOpen;
  });
</script>

</body>
</html>