<%@tag%><%@attribute name="anchors" type="java.util.List"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<div id="breadcrumbs">
<c:forEach items="${anchors}" var="a" varStatus="status">
    <a href="<c:url value="${a.url}"/>" <c:if test="${status.last}">id="current-page-breadcrumb"</c:if>>${a.text}</a>
    <c:if test="${not status.last}">/</c:if>
</c:forEach>
<c:if test="${empty anchors}"><a href="<c:url value="/"/>" id="current-page-breadcrumb">Home</a></c:if>
</div>