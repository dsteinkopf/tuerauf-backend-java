<%@ tag body-content="scriptless" trimDirectiveWhitespaces="true" %>
<%@ attribute name="pageTitle" required="false" rtexprvalue="true" %>

<!DOCTYPE html>


<html lang="en">
<head>
    <meta charset='utf-8'/>
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/main.css" type="text/css"/>

    <%-- this is not recommended for online/production (see http://lesscss.org/) --%>
    <link rel="stylesheet/less" type="text/css" href="${pageContext.request.contextPath}/less/main.less"/>
    <script src="//cdnjs.cloudflare.com/ajax/libs/less.js/2.5.0/less.min.js"></script>
</head>

<body>

<%-- Inject the page body here --%>
<jsp:doBody/>

</body>
</html>
