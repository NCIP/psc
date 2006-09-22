<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
	<link rel="stylesheet" type="text/css" href="/studycalendar/css/common.css"/>
    <title>Existing Studies</title>
</head>
<body>
<div id="body">
<security:secureOperation element="CreateStudyLink" operation="ACCESS">
<p><h3><a href="<c:url value="/pages/newStudy"/>">Create New Study</a></h3></p>
</security:secureOperation>
<br>
<br>
<h2>Existing Study List</h2>
<table>
    <c:forEach items="${studies}" var="study">
        <tr>
            <td><div class="tableRow"><a href="<c:url value="/pages/calendarTemplate?id=${study.id}"/>">${study.name}</a></div></td>
        </tr>
    </c:forEach>
</table>
</div>
</body>
</html>