<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Existing Studies</title>
</head>
<body>
<h1>Existing Studies</h1>
<table>
    <c:forEach items="${studies}" var="study">
        <tr>
            <td>${study.name}</td>
        </tr>
    </c:forEach>
</table>
</body>
</html>