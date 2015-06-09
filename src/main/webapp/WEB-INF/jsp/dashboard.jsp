<!DOCTYPE html>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<html lang="en">

<body>

<h1>T&uuml;rauf - Dashboard</h1>

<div id="flash-message" style="color: red; margin-bottom: 2em;">${message}</div>

<form id="activateAllNewForm" action="activateAllNew" method="post" commandName="activateAllNew">
    <input type="submit" name="submit" value="activate all new user now" />
    <input type="hidden"
           name="${_csrf.parameterName}"
           value="${_csrf.token}"/>
</form>

</body>
</html>
