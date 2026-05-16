<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Reset Password | ProcureGov</title>
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
      <h2 class="auth-visual__title">Set a New Password</h2>
      <p class="auth-visual__subtitle">
        Choose a strong password for your account.
        You will be redirected to the login page afterwards.
      </p>
      <div class="auth-visual__features">
        <div class="auth-feature">
          <span class="auth-feature__dot"></span>
          Minimum 8 characters
        </div>
        <div class="auth-feature">
          <span class="auth-feature__dot"></span>
          Mix letters, numbers, and symbols
        </div>
        <div class="auth-feature">
          <span class="auth-feature__dot"></span>
          Do not reuse your old password
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
      <h2 style="margin-bottom:8px;">Create new password</h2>
      <p class="text-sm">For <strong style="color:var(--clr-gold);">
        <c:out value="${resetEmail}"/></strong></p>
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

    <form class="auth-form" action="${pageContext.request.contextPath}/reset-password"
          method="POST" novalidate>

      <input type="hidden" name="email" value="<c:out value='${resetEmail}'/>"/>
      <input type="hidden" name="code" value="<c:out value='${resetCode}'/>"/>

      <!-- New Password -->
      <div class="form-group">
        <label class="form-label" for="password">New Password</label>
        <div class="input-wrapper">
          <svg class="input-icon" fill="none" stroke="currentColor" stroke-width="1.5"
               viewBox="0 0 24 24">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
          </svg>
          <input class="form-input form-input--icon"
                 type="password" id="password" name="password"
                 placeholder="••••••••" minlength="8"
                 autocomplete="new-password" required
                 style="padding-right:44px"/>
          <button type="button" class="input-toggle" id="togglePassword"
                  aria-label="Toggle password visibility">
            <svg id="eyeIcon" width="18" height="18" fill="none" stroke="currentColor"
                 stroke-width="1.5" viewBox="0 0 24 24">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
              <circle cx="12" cy="12" r="3"/>
            </svg>
          </button>
        </div>
        <span class="form-hint">Minimum 8 characters</span>
      </div>

      <!-- Confirm Password -->
      <div class="form-group">
        <label class="form-label" for="confirmPassword">Confirm New Password</label>
        <div class="input-wrapper">
          <svg class="input-icon" fill="none" stroke="currentColor" stroke-width="1.5"
               viewBox="0 0 24 24">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
          </svg>
          <input class="form-input form-input--icon"
                 type="password" id="confirmPassword" name="confirmPassword"
                 placeholder="••••••••" minlength="8"
                 autocomplete="new-password" required/>
        </div>
      </div>

      <button type="submit" class="btn btn-primary btn--full btn--lg" style="margin-top:8px;">
        Reset Password
      </button>
    </form>

    <div class="auth-footer-link" style="margin-top:var(--space-xl);">
      <a href="${pageContext.request.contextPath}/login"
         style="font-size:2.00rem; ">
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

<script>
  const toggle  = document.getElementById('togglePassword');
  const pwInput = document.getElementById('password');
  const eyeIcon = document.getElementById('eyeIcon');

  const eyeOpen   = `<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>`;
  const eyeClosed = `<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/>`;

  toggle.addEventListener('click', function() {
    const isPass = pwInput.type === 'password';
    pwInput.type = isPass ? 'text' : 'password';
    eyeIcon.innerHTML = isPass ? eyeClosed : eyeOpen;
  });
</script>

</body>
</html>