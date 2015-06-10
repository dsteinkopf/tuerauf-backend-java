<!DOCTYPE html>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<html lang="en">
<head>
    <meta charset='utf-8' />
    <title>Türauf</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/main.css" type="text/css" />
</head>

<body>

<h1>Türauf - Dashboard</h1>

<div id="flash-message">${message}</div>


<form id="activateAllNewForm" action="activateAllNew" method="post" commandName="activateAllNew">
    <input type="submit" name="submit" value="activate all new users"/>
    <input type="hidden"
           name="${_csrf.parameterName}"
           value="${_csrf.token}"/>
</form>

</body>
</html>
