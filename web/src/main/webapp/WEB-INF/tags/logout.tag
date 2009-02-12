<%@ tag %><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty user}"><a href="<c:url value="/public/logout"/>">Logout</a></c:if>