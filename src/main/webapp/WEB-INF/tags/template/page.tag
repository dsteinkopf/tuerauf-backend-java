<%@ tag body-content="scriptless" trimDirectiveWhitespaces="true" %>
<%@ attribute name="pageTitle" required="false" rtexprvalue="true" %>

<!DOCTYPE html>


<html lang="en">
<head>
    <meta charset='utf-8'/>
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="../static/main.css" type="text/css"/>

    <%-- this is not recommended for online/production (see http://lesscss.org/) --%>
    <link rel="stylesheet/less" type="text/css" href="../less/main.less"/>
    <script src="//cdnjs.cloudflare.com/ajax/libs/less.js/2.5.0/less.min.js"></script>
</head>

<body>

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
