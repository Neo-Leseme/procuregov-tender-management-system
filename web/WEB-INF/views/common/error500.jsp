<%-- 
    Document   : error500
    Created on : Apr 11, 2026, 11:46:11 PM
    Author     : masquerade
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>System Error | ProcureGov</title>
  <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/images/images.png"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
</head>
<body>
<div class="error-page">
  <div class="error-code">500</div>
  <h2 style="margin-bottom:12px;">Internal System Error</h2>
  <p style="margin-bottom:32px; max-width:480px;">
    An unexpected error occurred. Our team has been notified.
    Please try again shortly, or contact ICT support at
    <a href="mailto:help.procuregov@gov.ls">help.procuregov@gov.ls</a>
    if the problem persists.
  </p>
  <div style="display:flex; gap:16px; flex-wrap:wrap; justify-content:center;">
    <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Return to Home</a>
    <a href="javascript:history.back()" class="btn btn-outline">Go Back</a>
  </div>
  <p class="text-xs" style="margin-top:48px; color:var(--clr-text-muted);">
    ProcureGov — Ministry of Public Works, Kingdom of Lesotho
  </p>
</div>
</body>
</html>