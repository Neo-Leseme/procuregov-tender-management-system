<%-- 
    Document   : error404
    Created on : Apr 11, 2026, 11:45:55 PM
    Author     : masquerade
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Page Not Found | ProcureGov</title>
  <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/images/images.png"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
</head>
<body>
<div class="error-page">
  <div class="error-code">404</div>
  <h2 style="margin-bottom:12px;">Page Not Found</h2>
  <p style="margin-bottom:32px; max-width:420px;">
    The page you are looking for does not exist or has been moved.
    If you believe this is an error, please contact
    <a href="mailto:help.procuregov@gov.ls">help.procuregov@gov.ls</a>.
  </p>
  <div style="display:flex; gap:16px; flex-wrap:wrap; justify-content:center;">
    <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Return to Home</a>
    <a href="${pageContext.request.contextPath}/login" class="btn btn-outline">Sign In</a>
  </div>
  <p class="text-xs" style="margin-top:48px; color:var(--clr-text-muted);">
    ProcureGov — Ministry of Public Works, Kingdom of Lesotho
  </p>
</div>
</body>
</html>