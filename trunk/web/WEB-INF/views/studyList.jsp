<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Existing Studies</title>
</head>
<body>
<h1>Existing Studies</h1>
<p><a href="<c:url value="/pages/newStudy"/>">New study</a></p>
<table>
    <c:forEach items="${studies}" var="study">
        <tr>
            <td><a href="<c:url value="/pages/calendarTemplate?id=${study.id}"/>">${study.name}</a></td>
        </tr>
    </c:forEach>
</table>
</body>
</html>