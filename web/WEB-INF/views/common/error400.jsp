<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Bad Request — ProcureGov</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
</head>
<body>
    <main class="container" style="max-width:760px; margin:80px auto; padding:32px;">
        <div class="card" style="padding:32px; text-align:center;">
            <h1>400 — Bad Request</h1>
            <p>The request could not be processed because it was missing required information or contained invalid data.</p>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/login">Return to Login</a>
        </div>
    </main>
</body>
</html>
