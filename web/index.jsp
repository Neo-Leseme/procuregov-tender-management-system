<%-- 
    Document   : index
    Created on : Apr 12, 2026, 9:44:46 PM
    Author     : masquerade
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <meta name="description" content="ProcureGov — The Kingdom of Lesotho's official government tender management portal. Publish, bid, evaluate and award contracts digitally."/>
  <title>ProcureGov | Ministry of Public Works — Lesotho</title>

  <!-- Favicon -->
  <link rel="icon" type="image/x-icon" href="images/images.png"/>

  <link rel="stylesheet" href="css/style.css"/>

  <!-- Social meta -->
  <meta property="og:title" content="ProcureGov — Lesotho Tender Management"/>
  <meta property="og:description" content="Digital tender management for the Ministry of Public Works, Kingdom of Lesotho."/>
</head>
<body>

<!-- ══════════════════════════════════════════════════════════
     NAVIGATION
     ══════════════════════════════════════════════════════════ -->
<nav class="navbar" id="navbar">
  <div class="container navbar__inner">
    <a href="index.jsp" class="navbar__brand">
      <div class="navbar__brand-icon">PG</div>
      ProcureGov
    </a>

    <ul class="navbar__nav">
      <li><a href="#about">About</a></li>
      <li><a href="#how-it-works">How it Works</a></li>
      <li><a href="#roles">Who Uses It</a></li>
      <li><a href="#contact">Contact</a></li>
    </ul>

    <div class="navbar__actions">
      <a href="login" class="btn btn-ghost">Sign In</a>
      <a href="register" class="btn btn-primary">Register as Supplier</a>
    </div>
  </div>
</nav>


<!-- ══════════════════════════════════════════════════════════
     HERO
     ══════════════════════════════════════════════════════════ -->
<section class="hero" id="home">
  <div class="hero__bg"></div>
  <div class="hero__overlay"></div>

  <div class="container">
    <div class="hero__content">

      <div class="hero__eyebrow animate-fade-up">
        <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
          <circle cx="6" cy="6" r="5" stroke="currentColor" stroke-width="1.5"/>
          <circle cx="6" cy="6" r="2" fill="currentColor"/>
        </svg>
        Kingdom of Lesotho &nbsp;·&nbsp; Ministry of Public Works
      </div>

      <h1 class="hero__title animate-fade-up delay-1">
        Government Tenders,<br/>
        <em>Transparent</em> &amp; Digital
      </h1>

      <p class="hero__subtitle animate-fade-up delay-2">
        ProcureGov digitises the full tender lifecycle — from publication through
        bid evaluation to contract award — giving suppliers a fair, transparent
        process and the Ministry complete oversight.
      </p>

      <div class="hero__actions animate-fade-up delay-3">
        <a href="register" class="btn btn-primary btn--lg">Register as Supplier</a>
        <a href="login" class="btn btn-outline btn--lg">Ministry Staff Login</a>
      </div>

    </div>
  </div>

  <div class="hero__scroll">
    <svg width="16" height="20" viewBox="0 0 16 20" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect x="1" y="1" width="14" height="18" rx="7" stroke="currentColor" stroke-width="1.5"/>
      <rect x="7" y="5" width="2" height="4" rx="1" fill="currentColor"/>
    </svg>
    Scroll
  </div>
</section>


<!-- ══════════════════════════════════════════════════════════
     STATS BAR
     ══════════════════════════════════════════════════════════ -->
<div class="stats-bar">
  <div class="container">
    <div class="stats-bar__grid">
      <div class="stat-item">
        <div class="stat-item__number">100%</div>
        <div class="stat-item__label">Digital Process</div>
      </div>
      <div class="stat-item">
        <div class="stat-item__number">3</div>
        <div class="stat-item__label">Weighted Criteria</div>
      </div>
      <div class="stat-item">
        <div class="stat-item__number">6</div>
        <div class="stat-item__label">Lifecycle Stages</div>
      </div>
      <div class="stat-item">
        <div class="stat-item__number">SHA-256</div>
        <div class="stat-item__label">Password Security</div>
      </div>
    </div>
  </div>
</div>


<!-- ══════════════════════════════════════════════════════════
     ABOUT
     ══════════════════════════════════════════════════════════ -->
<section class="section" id="about">
  <div class="container">
    <span class="section-label">About the System</span>
    <h2 class="section-title">Replacing Paper with Purpose</h2>
    <span class="gold-line"></span>
    <p class="section-body">
      The Ministry of Public Works previously managed tenders through paper submissions,
      physical notice boards, and email correspondence — leading to delays, lost documents,
      and limited transparency. ProcureGov replaces that entirely with a secure,
      role-controlled web portal built to serve the Kingdom of Lesotho's public procurement needs.
    </p>

    <div class="how-grid" id="how-it-works">
      <div class="how-card">
        <div class="how-card__number">01</div>
        <h4>Publish &amp; Discover</h4>
        <p>Procurement Officers create and publish tenders with full specifications.
           Registered suppliers browse and download tender notices instantly.</p>
      </div>
      <div class="how-card">
        <div class="how-card__number">02</div>
        <h4>Bid Electronically</h4>
        <p>Suppliers submit sealed electronic bids with supporting documents.
           The system enforces one bid per tender and closing deadlines server-side.</p>
      </div>
      <div class="how-card">
        <div class="how-card__number">03</div>
        <h4>Evaluate &amp; Award</h4>
        <p>An Evaluation Committee scores bids using a weighted three-criteria model.
           The system ranks results and the Officer formally awards the contract.</p>
      </div>
    </div>
  </div>
</section>


<!-- ══════════════════════════════════════════════════════════
     ROLES
     ══════════════════════════════════════════════════════════ -->
<section class="section" id="roles" style="background: var(--clr-bg-elevated); border-top: 1px solid var(--clr-border-subtle); border-bottom: 1px solid var(--clr-border-subtle);">
  <div class="container">
    <span class="section-label">Who Uses ProcureGov</span>
    <h2 class="section-title">Three Roles. One System.</h2>
    <span class="gold-line"></span>

    <div class="roles-grid">

      <!-- Supplier -->
      <div class="role-card">
        <div class="role-card__icon">
          <svg width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
            <path d="M19 21H5a2 2 0 0 1-2-2V7l7-4 7 4v12a2 2 0 0 1-2 2z"/>
            <path d="M9 21V12h6v9"/>
          </svg>
        </div>
        <span class="badge badge-gold" style="margin-bottom:12px">Supplier</span>
        <h4>Registered Companies &amp; Individuals</h4>
        <p class="text-sm" style="margin-top:8px">Businesses seeking government contracts in Lesotho.</p>
        <div class="role-card__perms">
          <div class="perm-item">Self-register with company details</div>
          <div class="perm-item">Browse all open published tenders</div>
          <div class="perm-item">Download official tender notice PDFs</div>
          <div class="perm-item">Submit one sealed bid per tender</div>
          <div class="perm-item">Track bid status and view award notices</div>
        </div>
      </div>

      <!-- Procurement Officer -->
      <div class="role-card">
        <div class="role-card__icon">
          <svg width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
            <path d="M9 12l2 2 4-4"/>
            <path d="M5 7H3a2 2 0 0 0-2 2v11a2 2 0 0 0 2 2h11a2 2 0 0 0 2-2v-2"/>
            <rect x="9" y="3" width="13" height="13" rx="2"/>
          </svg>
        </div>
        <span class="badge badge-gold" style="margin-bottom:12px">Procurement Officer</span>
        <h4>Ministry Officials — Tender Management</h4>
        <p class="text-sm" style="margin-top:8px">Directorate of ICT staff managing the full tender lifecycle.</p>
        <div class="role-card__perms">
          <div class="perm-item">Create and publish tenders</div>
          <div class="perm-item">Manage all lifecycle status transitions</div>
          <div class="perm-item">Score bids as part of eval committee</div>
          <div class="perm-item">Select winner from ranked leaderboard</div>
          <div class="perm-item">Generate formal award notices</div>
        </div>
      </div>

      <!-- Eval Committee -->
      <div class="role-card">
        <div class="role-card__icon">
          <svg width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </div>
        <span class="badge badge-gold" style="margin-bottom:12px">Evaluation Committee</span>
        <h4>Ministry Officials — Independent Scoring</h4>
        <p class="text-sm" style="margin-top:8px">Appointed evaluators providing independent bid scoring.</p>
        <div class="role-card__perms">
          <div class="perm-item">Access tenders in Closed status only</div>
          <div class="perm-item">Score technical compliance (0–100)</div>
          <div class="perm-item">System auto-calculates price &amp; timeline</div>
          <div class="perm-item">Cannot view others' scores before own</div>
          <div class="perm-item">View consolidated results after scoring</div>
        </div>
      </div>

    </div>
  </div>
</section>


<!-- ══════════════════════════════════════════════════════════
     LIFECYCLE
     ══════════════════════════════════════════════════════════ -->
<section class="section">
  <div class="container">
    <span class="section-label">Tender Lifecycle</span>
    <h2 class="section-title">From Draft to Award</h2>
    <span class="gold-line"></span>

    <p style="margin-bottom: var(--space-2xl); max-width:680px; color:var(--clr-text-muted); line-height:1.8;">
      Every tender in ProcureGov passes through six enforced stages.
      Status transitions are controlled server-side — no stage can be skipped or reversed.
    </p>

    <div style="width:100%; overflow-x:auto; padding:3rem 0 1rem;">
      <div style="position:relative; display:flex; align-items:flex-start; min-width:900px;">

        <!-- Main timeline line -->
        <div style="position:absolute; top:23px; left:7%; right:7%; height:2px; background:var(--clr-border); z-index:0;"></div>

        <!-- Draft -->
        <div style="flex:1; position:relative; z-index:1; text-align:center; padding:0 12px;">
          <div style="width:18px; height:18px; margin:14px auto 24px; border-radius:50%; background:var(--clr-gold); border:3px solid var(--clr-bg); box-shadow:0 0 0 4px var(--clr-gold-dim);"></div>
          <div style="font-family:var(--font-mono); font-size:0.75rem; letter-spacing:0.12em; text-transform:uppercase; color:var(--clr-gold); margin-bottom:8px;">
            Draft
          </div>
          <div style="font-size:0.85rem; color:var(--clr-text); margin-bottom:5px;">
            Officer creates tender
          </div>
          <div style="font-size:0.75rem; color:var(--clr-text-muted); font-style:italic;">
            by Officer
          </div>
        </div>

        <!-- Open -->
        <div style="flex:1; position:relative; z-index:1; text-align:center; padding:0 12px;">
          <div style="width:18px; height:18px; margin:14px auto 24px; border-radius:50%; background:var(--clr-gold); border:3px solid var(--clr-bg); box-shadow:0 0 0 4px var(--clr-gold-dim);"></div>
          <div style="font-family:var(--font-mono); font-size:0.75rem; letter-spacing:0.12em; text-transform:uppercase; color:var(--clr-gold); margin-bottom:8px;">
            Open
          </div>
          <div style="font-size:0.85rem; color:var(--clr-text); margin-bottom:5px;">
            Visible to suppliers
          </div>
          <div style="font-size:0.75rem; color:var(--clr-text-muted); font-style:italic;">
            by Officer
          </div>
        </div>

        <!-- Closed -->
        <div style="flex:1; position:relative; z-index:1; text-align:center; padding:0 12px;">
          <div style="width:18px; height:18px; margin:14px auto 24px; border-radius:50%; background:var(--clr-gold); border:3px solid var(--clr-bg); box-shadow:0 0 0 4px var(--clr-gold-dim);"></div>
          <div style="font-family:var(--font-mono); font-size:0.75rem; letter-spacing:0.12em; text-transform:uppercase; color:var(--clr-gold); margin-bottom:8px;">
            Closed
          </div>
          <div style="font-size:0.85rem; color:var(--clr-text); margin-bottom:5px;">
            Deadline passed
          </div>
          <div style="font-size:0.75rem; color:var(--clr-text-muted); font-style:italic;">
            by System
          </div>
        </div>

        <!-- Under Evaluation -->
        <div style="flex:1; position:relative; z-index:1; text-align:center; padding:0 12px;">
          <div style="width:18px; height:18px; margin:14px auto 24px; border-radius:50%; background:var(--clr-gold); border:3px solid var(--clr-bg); box-shadow:0 0 0 4px var(--clr-gold-dim);"></div>
          <div style="font-family:var(--font-mono); font-size:0.75rem; letter-spacing:0.12em; text-transform:uppercase; color:var(--clr-gold); margin-bottom:8px;">
            Under Evaluation
          </div>
          <div style="font-size:0.85rem; color:var(--clr-text); margin-bottom:5px;">
            Scoring begins
          </div>
          <div style="font-size:0.75rem; color:var(--clr-text-muted); font-style:italic;">
            by Officer
          </div>
        </div>

        <!-- Evaluated -->
        <div style="flex:1; position:relative; z-index:1; text-align:center; padding:0 12px;">
          <div style="width:18px; height:18px; margin:14px auto 24px; border-radius:50%; background:var(--clr-gold); border:3px solid var(--clr-bg); box-shadow:0 0 0 4px var(--clr-gold-dim);"></div>
          <div style="font-family:var(--font-mono); font-size:0.75rem; letter-spacing:0.12em; text-transform:uppercase; color:var(--clr-gold); margin-bottom:8px;">
            Evaluated
          </div>
          <div style="font-size:0.85rem; color:var(--clr-text); margin-bottom:5px;">
            All scores submitted
          </div>
          <div style="font-size:0.75rem; color:var(--clr-text-muted); font-style:italic;">
            by System
          </div>
        </div>

        <!-- Awarded -->
        <div style="flex:1; position:relative; z-index:1; text-align:center; padding:0 12px;">
          <div style="width:18px; height:18px; margin:14px auto 24px; border-radius:50%; background:var(--clr-gold); border:3px solid var(--clr-bg); box-shadow:0 0 0 4px var(--clr-gold-dim);"></div>
          <div style="font-family:var(--font-mono); font-size:0.75rem; letter-spacing:0.12em; text-transform:uppercase; color:var(--clr-gold); margin-bottom:8px;">
            Awarded
          </div>
          <div style="font-size:0.85rem; color:var(--clr-text); margin-bottom:5px;">
            Contract awarded
          </div>
          <div style="font-size:0.75rem; color:var(--clr-text-muted); font-style:italic;">
            by Officer
          </div>
        </div>

      </div>
    </div>
  </div>
</section>


<!-- ══════════════════════════════════════════════════════════
     CONTACT
     ══════════════════════════════════════════════════════════ -->
<section class="section contact-section" id="contact">
  <div class="container">
    <div class="contact-grid">

      <div>
        <span class="section-label">Get Help</span>
        <h2 class="section-title">Contact &amp; Support</h2>
        <span class="gold-line"></span>
        <p style="margin-bottom:var(--space-xl);">
          For technical support, supplier registration assistance, or questions
          about active tenders, reach out to the Ministry's ICT Directorate using
          the details below.
        </p>

        <div class="contact-info__item">
          <div class="contact-info__icon">
            <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
              <polyline points="22,6 12,13 2,6"/>
            </svg>
          </div>
          <div>
            <div class="contact-info__label">Email Support</div>
            <a href="mailto:help.procuregov@gov.ls" class="contact-info__value">help.procuregov@gov.ls</a>
          </div>
        </div>

        <div class="contact-info__item">
          <div class="contact-info__icon">
            <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
          </div>
          <div>
            <div class="contact-info__label">Ministry Address</div>
            <div class="contact-info__value">Directorate of ICT, Ministry of Public Works<br/>Maseru, Kingdom of Lesotho</div>
          </div>
        </div>

        <div class="contact-info__item">
          <div class="contact-info__icon">
            <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
          </div>
          <div>
            <div class="contact-info__label">Support Hours</div>
            <div class="contact-info__value">Monday – Friday, 08:00 – 17:00 SAST</div>
          </div>
        </div>

        <!-- Social links -->
        <div>
          <div class="contact-info__label" style="margin-bottom:var(--space-md);">Follow Us @ProcureGov</div>
          <div class="social-links">

            <!-- Facebook -->
            <a href="https://facebook.com/ProcureGov" target="_blank" rel="noopener" class="social-link" title="Facebook — @ProcureGov">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                <path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"/>
              </svg>
            </a>

            <!-- Instagram -->
            <a href="https://instagram.com/ProcureGov" target="_blank" rel="noopener" class="social-link" title="Instagram — @ProcureGov">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <rect x="2" y="2" width="20" height="20" rx="5" ry="5"/>
                <circle cx="12" cy="12" r="4"/>
                <circle cx="17.5" cy="6.5" r="1" fill="currentColor" stroke="none"/>
              </svg>
            </a>

            <!-- X / Twitter -->
            <a href="https://x.com/ProcureGov" target="_blank" rel="noopener" class="social-link" title="X — @ProcureGov">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"/>
              </svg>
            </a>

          </div>
        </div>
      </div>

      <!-- Info / Help card -->
      <div>
        <div class="card" style="margin-bottom:var(--space-lg);">
          <h4 style="margin-bottom:var(--space-md);">Supplier Registration Guide</h4>
          <p class="text-sm" style="margin-bottom:var(--space-md);">
            To participate in government tenders you must first register as a supplier.
            Have the following ready before you begin:
          </p>
          <div class="role-card__perms">
            <div class="perm-item">Company / individual full legal name</div>
            <div class="perm-item">Valid business registration number if applicable</div>
            <div class="perm-item">Physical address in Lesotho</div>
            <div class="perm-item">Active contact number</div>
            <div class="perm-item">Email address for notifications</div>
          </div>
          <a href="register" class="btn btn-primary btn--full" style="margin-top:var(--space-lg);">Register Now</a>
        </div>

        <div class="card">
          <h4 style="margin-bottom:var(--space-md);">Ministry Staff</h4>
          <p class="text-sm" style="margin-bottom:var(--space-md);">
            Procurement Officers and Evaluation Committee Members are set up
            directly by the ICT Directorate. Contact your system administrator
            to have your account created.
          </p>
          <a href="login" class="btn btn-outline btn--full">Staff Login</a>
        </div>
      </div>

    </div>
  </div>
</section>


<!-- ══════════════════════════════════════════════════════════
     FOOTER
     ══════════════════════════════════════════════════════════ -->
<footer class="footer">
  <div class="container footer__inner">
    <div class="footer__copy">
      &copy; <span id="currentYear">2026</span> ProcureGov — Ministry of Public Works, Kingdom of Lesotho.
      All rights reserved.
    </div>
    <nav class="footer__links">
      <a href="#about">About</a>
      <a href="#how-it-works">How It Works</a>
      <a href="#contact">Support</a>
      <a href="login">Login</a>
    </nav>
  </div>
</footer>

<script>
  /* Navbar shadow on scroll */
  const navbar = document.getElementById('navbar');

  window.addEventListener('scroll', () => {
    navbar.style.background = window.scrollY > 60
      ? 'rgba(13,17,23,0.97)'
      : 'rgba(13,17,23,0.85)';
  });

  /* Dynamic footer year without JSP scriptlets */
  const currentYearElement = document.getElementById('currentYear');
  if (currentYearElement) {
    currentYearElement.textContent = new Date().getFullYear();
  }
</script>

</body>
</html>
