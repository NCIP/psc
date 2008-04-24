<%@tag%><%@attribute name="name" required="true"%><%@attribute name="dynamic" required="false" %><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:if test="${dynamic}"><c:set var="fileName" value="${name}.css.jsp"/> </c:if>
<c:if test="${not dynamic}"><c:set var="fileName" value="${name}.css"/> </c:if> 

<link rel="stylesheet" type="text/css" href="<c:url value="/css/${fileName}"/>"/>