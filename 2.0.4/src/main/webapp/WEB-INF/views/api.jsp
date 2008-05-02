<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head><title>API execution response</title></head>
<body>
    <c:choose>
        <c:when test="${empty exception}">
            <p>Execution successful</p>
        </c:when>
        <c:otherwise>
            <p>Execution failed: ${exception.message}</p>
        </c:otherwise>
    </c:choose>
    <c:if test="${not empty result}">
        Result: ${result} <%-- TODO: make this more useful.  XML is a possibility. --%>
    </c:if>
</body>
</html>