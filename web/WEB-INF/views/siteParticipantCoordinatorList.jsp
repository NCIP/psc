<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
<head>
    <title>Site Participant Coordinator List</title>
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
<h1>Participant Coordinator List For Site ${site.name}</h1>

<table>
    <c:forEach items="${participantcoordinators}" var="participantcoordinator">
        <tr>
            <td><strong>${participantcoordinator.name} </strong><a href="<c:url value="/pages/assignTemplatesToOneParticipantCoordinator?siteId=${site.id}&pcId=${participantcoordinator.userId}"/>">  Assign Study Templates  </a></td>
        </tr>
    </c:forEach>
</table>

</body>
</html>