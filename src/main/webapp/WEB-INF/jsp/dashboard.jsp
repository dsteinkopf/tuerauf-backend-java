<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/template" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%--@elvariable id="users" type="java.util.List<net.steinkopf.tuerauf.data.User>"--%>
<%--@elvariable id="message" type="java.lang.String"--%>
<%--@elvariable id="_csrf" type="org.springframework.security.web.csrf.CsrfToken"--%>

<c:set var="pageTitle" value="Türauf - Dashboard"/>

<template:page pageTitle="${pageTitle}">

    <h1>${pageTitle}</h1>

    <div id="flash-message">${message}</div>

    <table class="users">
        <tr class="head">
            <td>serialId</td>
            <td>username</td>
            <td>pin</td>
            <td>installationId</td>
            <td>active</td>
            <td>newUser</td>
            <td>created</td>
            <td>modified</td>
        </tr>

        <c:forEach var="user" items="${users}">
            <c:if test="${not user.active}">
                <c:set var="inactiveUserClass" value="inactive"/>
            </c:if>
            <c:if test="${user.newUser}">
                <c:set var="newUserClass" value="new"/>
            </c:if>
            <tr class="user ${inactiveUserClass} ${newUserClass}">
                <td>${user.serialId}</td>
                <td>${user.username}<c:if test="${not empty user.usernameOld}"> (was: ${user.usernameOld})</c:if></td>
                <td>${user.pin}<c:if test="${not empty user.pinOld}"> (was: ${user.pinOld})</c:if></td>
                <td>${user.installationId}</td>
                <td><c:if test="${user.active}">x</c:if></td>
                <td><c:if test="${user.newUser}">x</c:if></td>
                <td><fmt:formatDate value="${user.creationTime}" pattern="yyyy-MM-dd HH:mm"/></td>
                <td><fmt:formatDate value="${user.modificationTime}" pattern="yyyy-MM-dd HH:mm"/></td>
            </tr>
        </c:forEach>
    </table>

    User count: ${users.size()}<br/>
    <br/>

    <form id="activateAllNewForm" action="activateAllNew" method="post" commandName="activateAllNew">
        <input type="submit" name="submit" value="activate all new users"/>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    </form>


    <br/>
    Send pins of active users to Arduino:<br/>

    <form id="sendPinsToArduino" action="sendPinsToArduino" method="post" commandName="sendPinsToArduino">
        <input type="submit" name="submit" value="send pins to arduino"/>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    </form>

</template:page>
