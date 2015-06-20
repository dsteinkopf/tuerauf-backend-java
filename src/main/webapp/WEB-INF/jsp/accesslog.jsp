<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/template" %>
<%@ taglib prefix="util" tagdir="/WEB-INF/tags/util" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%--@elvariable id="page" type="org.springframework.data.domain.Page<net.steinkopf.tuerauf.data.AccessLog>"--%>
<%--@elvariable id="locationService" type="net.steinkopf.tuerauf.service.LocationService"--%>
<%--@elvariable id="_csrf" type="org.springframework.security.web.csrf.CsrfToken"--%>

<c:set var="pageTitle" value="Türauf - AccessLog"/>

<template:page pageTitle="${pageTitle}">

    <h1>${pageTitle}</h1>

    <util:pagination maxPages="${page.totalPages}"
                     maxElements="${page.totalElements}"
                     page="${page.number}"
                     size="${page.size}"/>

    <table class="accesslog">
        <tr class="head">
            <td>accessTimestamp</td>
            <td>accessType</td>
            <td>username</td>
            <td>geoy</td>
            <td>geox</td>
            <td>result</td>
            <td>direction</td>
            <td>meters away</td>
        </tr>

        <c:forEach items="${page.content}" var="accessLog" varStatus="status">
            <tr class="${status.count % 2 == 0 ? 'even' : 'odd'}">
                <td><fmt:formatDate value="${accessLog.accessTimestamp}" pattern="yyyy-MM-dd HH:mm:ss z"/></td>
                <td>${accessLog.accessType}</td>
                <td>${accessLog.user.username}</td>
                <td>${accessLog.geoy}</td>
                <td>${accessLog.geox}</td>
                <td>${accessLog.result}</td>
                <td>
                    <fmt:formatNumber value="${locationService.getDirectionFromHome(accessLog.geoy, accessLog.geox)}" maxFractionDigits="0"/>
                </td>
                <td>
                    <fmt:formatNumber value="${locationService.getDistanceFromHome(accessLog.geoy, accessLog.geox)}" maxFractionDigits="0"/>
                </td>
            </tr>
        </c:forEach>
    </table>

    <spring:url value="" var="nextPageUrl">
        <spring:param name="page" value="${page.number + 1}" />
    </spring:url>
    <spring:url value="" var="prevPageUrl">
        <spring:param name="page" value="${page.number - 1}" />
    </spring:url>

    <c:if test="${page.hasPrevious()}"><a href="${prevPageUrl}">previous page</a></c:if> -
    <c:if test="${page.hasNext()}"><a href="${nextPageUrl}">next page</a></c:if>

    <div style="margin-top: 3em">
        <spring:url value="/dashboard/" var="dashboardUrl"/>
        <a href="${dashboardUrl}">goto dashboard</a>
    </div>

</template:page>
