<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
<head>
    <title>Manage sites</title>
    <tags:includeScriptaculous/>
    <style type="text/css">
        div.label {
            width: 35%;
        }
        div.submit {
            text-align: right;
        }
        form {
            width: 20em;
        }
    </style>
</head>
<body>
<h1>Manage Sites</h1>

<a href="<c:url value="/pages/studyList"/>">Calendar Menu</a>.<br>

<p><a href="<c:url value="/pages/newSite"/>">Create New Site</a></p>

<table>
    <c:forEach items="${sites}" var="site">
        <tr>
            <td>${site.name}  <a href="<c:url value="/pages/assignSiteCoordinator?id=${site.id}"/>">  Assign Site Coordinators to Site  </a></td>
        </tr>
    </c:forEach>
</table>



</body>
</html>