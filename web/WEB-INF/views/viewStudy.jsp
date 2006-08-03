<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <title>View study</title>
</head>
<body>
<h1>View study</h1>
<p>
    Study: ${study.name}
</p>
<c:if test="${fn:length(study.arms) != 1}">
<p>
    Arms: <c:forEach items="${study.arms}" var="arm">${arm.name} </c:forEach>
</p>
</c:if>

</body>
</html>