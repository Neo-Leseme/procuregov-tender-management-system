<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Verify Code | ProcureGov</title>
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
      <h2 class="auth-visual__title">Verify Your Identity</h2>
      <p class="auth-visual__subtitle">
        A 6-digit code has been sent to
        <strong style="color:var(--clr-gold);">
          <c:out value="${resetEmail}"/>
        </strong>.
        Enter it below to continue.
      </p>
      <div class="auth-visual__features">
        <div class="auth-feature">
          <span class="auth-feature__dot"></span>
          Code is valid for 15 minutes
        </div>
        <div class="auth-feature">
          <span class="auth-feature__dot"></span>
          Check your spam/junk folder
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
      <h2 style="margin-bottom:8px;">Enter Verification Code</h2>
      <p class="text-sm">
        We emailed a 6-digit code to <c:out value="${resetEmail}"/>
      </p>
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

    <form class="auth-form" action="${pageContext.request.contextPath}/verify-code"
          method="POST" novalidate>

      <input type="hidden" name="email" value="<c:out value='${resetEmail}'/>"/>

      <div class="form-group text-center">
        <label class="form-label" style="text-align:center;display:block;">
          Verification Code
        </label>

        <!-- Single hidden input that collects all 6 digits -->
        <input type="hidden" name="code" id="fullCode"/>

        <div class="code-input-group" id="codeGroup">
          <input type="text" class="code-digit" maxlength="1" inputmode="numeric"
                 pattern="[0-9]" autocomplete="off" data-index="0"/>
          <input type="text" class="code-digit" maxlength="1" inputmode="numeric"
                 pattern="[0-9]" autocomplete="off" data-index="1"/>
          <input type="text" class="code-digit" maxlength="1" inputmode="numeric"
                 pattern="[0-9]" autocomplete="off" data-index="2"/>
          <input type="text" class="code-digit" maxlength="1" inputmode="numeric"
                 pattern="[0-9]" autocomplete="off" data-index="3"/>
          <input type="text" class="code-digit" maxlength="1" inputmode="numeric"
                 pattern="[0-9]" autocomplete="off" data-index="4"/>
          <input type="text" class="code-digit" maxlength="1" inputmode="numeric"
                 pattern="[0-9]" autocomplete="off" data-index="5"/>
        </div>
      </div>

      <button type="submit" class="btn btn-primary btn--full btn--lg" style="margin-top:16px;">
        Verify Code
      </button>
    </form>

    <div class="auth-footer-link" style="margin-top:var(--space-lg);">
      <a href="${pageContext.request.contextPath}/forgot-password"
         style="font-size:1.88rem;">
        ← Try a different email
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
  (function() {
    const digits  = document.querySelectorAll('.code-digit');
    const full    = document.getElementById('fullCode');

    function buildFull() {
      let code = '';
      digits.forEach(function(d) { code += d.value; });
      full.value = code;
    }

    digits.forEach(function(input, idx) {
      input.addEventListener('input', function() {
        this.value = this.value.replace(/[^0-9]/g, '').slice(0, 1);
        buildFull();
        if (this.value && idx < digits.length - 1) {
          digits[idx + 1].focus();
        }
      });

      input.addEventListener('keydown', function(e) {
        if (e.key === 'Backspace' && !this.value && idx > 0) {
          digits[idx - 1].focus();
        }
        if (e.key === 'ArrowLeft' && idx > 0) {
          e.preventDefault();
          digits[idx - 1].focus();
        }
        if (e.key === 'ArrowRight' && idx < digits.length - 1) {
          e.preventDefault();
          digits[idx + 1].focus();
        }
      });

      input.addEventListener('paste', function(e) {
        e.preventDefault();
        var pasted = (e.clipboardData || window.clipboardData).getData('text');
        pasted = pasted.replace(/[^0-9]/g, '').slice(0, 6);
        for (var i = 0; i < digits.length; i++) {
          digits[i].value = pasted[i] || '';
        }
        buildFull();
        var focusIdx = Math.min(pasted.length, digits.length - 1);
        digits[focusIdx].focus();
      });
    });
  })();
</script>

</body>
</html>