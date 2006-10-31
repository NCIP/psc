<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
<head>
    <title>Sites For Assigning Participant Coordinators</title>
    <tags:includeScriptaculous/>
    <style type="text/css">
        div.label {
            width: 10%;
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
<h1>Sites For Assigning Participant Coordinators</h1>
<br>
<br>

<table>
    <c:forEach items="${sites}" var="site">
        <tr>
            <td><strong>${site.name}   </strong><a href="<c:url value="/pages/assignParticipantCoordinatorsToSite?id=${site.id}"/>">  Assign Participant Coordinators to Site  </a></td>
        	<td>   <a href="<c:url value="/pages/siteParticipantCoordinatorList?id=${site.id}"/>">  Choose Participant Coordinators To Assign Study Templates  </a></td>
        </tr>
    </c:forEach>
</table>



</body>
</html>