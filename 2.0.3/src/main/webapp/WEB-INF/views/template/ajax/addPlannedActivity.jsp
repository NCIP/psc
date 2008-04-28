<%@ page contentType="text/javascript;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
<c:if test="${columnNumber >= 0}">
    $('grid[${rowNumber}].plannedActivities[${columnNumber}]').setAttribute('value', '${addedActivity.id}')
</c:if>
<templ:updateChanges changes="${revisionChanges}" revision="${developmentRevision}" />