<%@ page contentType="text/javascript;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${columnNumber >= 0}">
    $('grid[${rowNumber}].eventIds[${columnNumber}]1').value = '${id}'
</c:if>