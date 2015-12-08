<%@ tag body-content="scriptless" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="pageTitle" required="false" rtexprvalue="true" %>
<%@ attribute name="onLoad" required="false" rtexprvalue="true" %>

<!DOCTYPE html>


<html lang="en">
<head>
    <meta charset='utf-8'/>
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="../static/main.css" type="text/css"/>

    <%-- this is not recommended for online/production (see http://lesscss.org/) --%>
    <link rel="stylesheet/less" type="text/css" href="../less/main.less"/>
    <script src="//cdnjs.cloudflare.com/ajax/libs/less.js/2.5.3/less.min.js"></script>


    <script src="//code.jquery.com/jquery-2.1.4.min.js"></script>

    <script type="text/javascript">
        var myglob = {};
    </script>
</head>

<body<c:if test="${not empty onLoad}"> onload="${onLoad}"</c:if>>

<%-- Inject the page body here --%>
<jsp:doBody/>

<!-- Footer -->
<div class="versioninfo">
    implementationBuild: ${implementationBuild} -
    implementationBuildTime: ${implementationBuildTime}
<%-- -
implementationVersion: ${implementationVersion} -
implementationJdk: ${implementationJdk}
--%>
</div>

</body>
</html>
