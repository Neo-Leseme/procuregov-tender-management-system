<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Register as Supplier | ProcureGov</title>
  <link rel="icon" type="image/x-icon" href="images/images.png"/>
  <link rel="stylesheet" href="css/style.css"/>
</head>
<body>

<div class="auth-page">

  <!-- ══ LEFT — Visual Panel ════════════════════════════════ -->
  <aside class="auth-visual">
    <%-- Replace the src below with your chosen offline image path --%>
    <img
      class="auth-visual__img"
      src="images/image.jpeg"
      alt="Construction and infrastructure — Lesotho public works"
    />
    <div class="auth-visual__overlay"></div>

    <div class="auth-visual__content">
      <div class="auth-visual__badge">
        <svg width="10" height="10" viewBox="0 0 10 10" fill="none">
          <circle cx="5" cy="5" r="4" stroke="currentColor" stroke-width="1.5"/>
          <circle cx="5" cy="5" r="1.5" fill="currentColor"/>
        </svg>
        Supplier Registration &nbsp;·&nbsp; ProcureGov
      </div>

      <h2 class="auth-visual__title">
        Start Bidding on<br/>Government Tenders
      </h2>

      <p class="auth-visual__subtitle">
        Register your company once and gain access to all open
        tenders published by the Ministry of Public Works.
      </p>

      <div class="auth-visual__features">
        <div class="auth-feature">
          <span class="auth-feature__dot"></span>
          Registration is free and instant
        </div>
        <div class="auth-feature">
          <span class="auth-feature__dot"></span>
          Browse all open tenders immediately
        </div>
        <div class="auth-feature">
          <span class="auth-feature__dot"></span>
          Submit sealed electronic bids securely
        </div>
        <div class="auth-feature">
          <span class="auth-feature__dot"></span>
          Track your bid status in real-time
        </div>
        <div class="auth-feature">
          <span class="auth-feature__dot"></span>
          Email notifications when tenders are awarded
        </div>
      </div>
    </div>
  </aside>


  <!-- ══ RIGHT — Form Panel ════════════════════════════════ -->
  <main class="auth-form-panel auth-form-panel--register">

    <div class="auth-header">
      <a href="index.jsp" class="auth-header__brand">
        <div class="auth-header__brand-icon">PG</div>
        ProcureGov
      </a>
      <h2 style="margin-bottom:6px;">Create Supplier Account</h2>
      <p class="text-sm">All fields are mandatory unless stated otherwise.</p>
    </div>

    <%-- Error / success messages from AuthServlet --%>
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

    <form
      class="auth-form"
      action="${pageContext.request.contextPath}/register"
      method="POST"
      novalidate
      id="registerForm"
    >

      <!-- ── Section: Company Information ───────────────── -->
      <div style="margin-bottom:4px;">
        <span class="section-label" style="font-size:0.68rem;">Company / Individual Information</span>
      </div>

      <!-- Company Name -->
      <div class="form-group">
        <label class="form-label" for="companyName">Company / Individual Full Name</label>
        <div class="input-wrapper">
          <svg class="input-icon" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
            <path d="M19 21H5a2 2 0 0 1-2-2V7l7-4 7 4v12a2 2 0 0 1-2 2z"/>
            <path d="M9 21V12h6v9"/>
          </svg>
          <input
            class="form-input form-input--icon"
            type="text"
            id="companyName"
            name="companyName"
            placeholder="e.g. Neo Leseme Construction Ltd"
            value="<c:out value='${param.companyName}'/>"
            required maxlength="200"
          />
        </div>
      </div>

      <!-- Registration number — auto-generated note -->
      <div class="form-group">
        <label class="form-label" for="regNote">Registration Number</label>
        <input
          class="form-input"
          type="text"
          id="regNote"
          value="Auto-generated upon registration (e.g. SUP-20260001)"
          disabled
          style="opacity:0.5; cursor:not-allowed;"
        />
        <span class="form-hint">Your supplier registration number will be assigned automatically by the system.</span>
      </div>

      <!-- Physical Address -->
      <div class="form-group">
        <label class="form-label" for="physicalAddress">Physical Address</label>
        <div class="input-wrapper">
          <svg class="input-icon" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
            <circle cx="12" cy="10" r="3"/>
          </svg>
          <input
            class="form-input form-input--icon"
            type="text"
            id="physicalAddress"
            name="physicalAddress"
            placeholder="e.g. 14 Kingsway Road, Maseru, Lesotho"
            value="<c:out value='${param.physicalAddress}'/>"
            required maxlength="300"
          />
        </div>
      </div>

      <!-- Contact Number -->
      <div class="form-group">
        <label class="form-label" for="contactNumber">Contact Number</label>
        <div class="input-wrapper">
          <svg class="input-icon" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 1.27h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.77a16 16 0 0 0 6.29 6.29l.98-.98a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7a2 2 0 0 1 1.72 2.02z"/>
          </svg>
          <input
            class="form-input form-input--icon"
            type="tel"
            id="contactNumber"
            name="contactNumber"
            placeholder="+266 XXXX XXXX"
            value="<c:out value='${param.contactNumber}'/>"
            required maxlength="20"
          />
        </div>
      </div>

      <hr class="divider" style="margin: 12px 0;"/>

      <!-- ── Section: Account Credentials ──────────────── -->
      <div style="margin-bottom:4px;">
        <span class="section-label" style="font-size:0.68rem;">Account Credentials</span>
      </div>

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
            placeholder="you@company.co.ls"
            value="<c:out value='${param.email}'/>"
            autocomplete="email"
            required maxlength="200"
          />
        </div>
        <span class="form-hint">This email will be used for login and tender award notifications.</span>
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
            placeholder="Min. 8 characters"
            autocomplete="new-password"
            required minlength="8"
            style="padding-right:44px"
            oninput="checkStrength(this.value)"
          />
          <button type="button" class="input-toggle" id="togglePw1" aria-label="Toggle password visibility">
            <svg id="eye1" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
              <circle cx="12" cy="12" r="3"/>
            </svg>
          </button>
        </div>
        <!-- Password strength indicator -->
        <div class="password-strength" id="strengthBars">
          <div class="strength-bar" id="bar1"></div>
          <div class="strength-bar" id="bar2"></div>
          <div class="strength-bar" id="bar3"></div>
          <div class="strength-bar" id="bar4"></div>
        </div>
        <span class="form-hint" id="strengthLabel">Password must be at least 8 characters.</span>
      </div>

      <!-- Confirm Password -->
      <div class="form-group">
        <label class="form-label" for="confirmPassword">Confirm Password</label>
        <div class="input-wrapper">
          <svg class="input-icon" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
          </svg>
          <input
            class="form-input form-input--icon"
            type="password"
            id="confirmPassword"
            name="confirmPassword"
            placeholder="Re-enter password"
            autocomplete="new-password"
            required
            style="padding-right:44px"
            oninput="checkMatch()"
          />
          <button type="button" class="input-toggle" id="togglePw2" aria-label="Toggle confirm password visibility">
            <svg id="eye2" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
              <circle cx="12" cy="12" r="3"/>
            </svg>
          </button>
        </div>
        <span class="form-hint" id="matchLabel"></span>
      </div>

      <!-- Terms note -->
      <p class="text-xs" style="color:var(--clr-text-muted); line-height:1.6; margin-top:4px;">
        By registering you acknowledge that all information provided is accurate and
        you agree to abide by the Ministry of Public Works procurement regulations.
      </p>

      <button type="submit" class="btn btn-primary btn--full btn--lg" id="submitBtn">
        Create Supplier Account
      </button>

    </form>

    <div class="auth-footer-link" style="margin-top:var(--space-lg);">
      Already have an account? <a href="${pageContext.request.contextPath}/login">Sign in</a>
    </div>

  </main>
</div>

<script>
  /* ── Password visibility toggles ─────────────────────── */
  function makeToggle(btnId, inputId, eyeId) {
    document.getElementById(btnId).addEventListener('click', () => {
      const input = document.getElementById(inputId);
      const icon  = document.getElementById(eyeId);
      const isPass = input.type === 'password';
      input.type = isPass ? 'text' : 'password';
      icon.innerHTML = isPass
        ? `<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/>`
        : `<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>`;
    });
  }
  makeToggle('togglePw1', 'password', 'eye1');
  makeToggle('togglePw2', 'confirmPassword', 'eye2');

  /* ── Password strength ────────────────────────────────── */
  function checkStrength(val) {
    const bars  = [document.getElementById('bar1'), document.getElementById('bar2'),
                   document.getElementById('bar3'), document.getElementById('bar4')];
    const label = document.getElementById('strengthLabel');
    bars.forEach(b => { b.className = 'strength-bar'; });

    let score = 0;
    if (val.length >= 8)  score++;
    if (/[A-Z]/.test(val)) score++;
    if (/[0-9]/.test(val)) score++;
    if (/[^A-Za-z0-9]/.test(val)) score++;

    const levels = ['active-weak','active-weak','active-medium','active-strong'];
    const names  = ['Too short','Weak','Fair','Strong'];
    if (val.length === 0) { label.textContent = 'Password must be at least 8 characters.'; return; }
    for (let i = 0; i < score; i++) bars[i].classList.add(levels[score - 1]);
    label.textContent = names[score - 1] || '';
  }

  /* ── Password match ───────────────────────────────────── */
  function checkMatch() {
    const pw   = document.getElementById('password').value;
    const cpw  = document.getElementById('confirmPassword').value;
    const lbl  = document.getElementById('matchLabel');
    if (cpw.length === 0) { lbl.textContent = ''; return; }
    if (pw === cpw) {
      lbl.textContent = '✓ Passwords match';
      lbl.style.color = 'var(--clr-success)';
    } else {
      lbl.textContent = '✗ Passwords do not match';
      lbl.style.color = 'var(--clr-danger)';
    }
  }

  /* ── Client-side submit guard ─────────────────────────── */
  document.getElementById('registerForm').addEventListener('submit', function(e) {
    const pw  = document.getElementById('password').value;
    const cpw = document.getElementById('confirmPassword').value;
    if (pw !== cpw) {
      e.preventDefault();
      alert('Passwords do not match. Please re-enter.');
    }
  });
</script>

</body>
</html>