<%@ page contentType="text/javascript;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
<c:if test="${columnNumber >= 0}">
    $('grid[${rowNumber}].eventIds[${columnNumber}]').setAttribute('value', '${id}')
    $('grid[${rowNumber}].eventIds[${moveFrom}]').setAttribute('value', '-1')
</c:if>
<templ:updateChanges changes="${revisionChanges}" revision="${developmentRevision}" />