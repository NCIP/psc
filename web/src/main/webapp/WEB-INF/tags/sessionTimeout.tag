<%@tag%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty currentUser}">
    <c:set var="sessionTimeout" value="${pageContext.session.maxInactiveInterval}"/>
    <c:set var="sessionTimeoutWarning" value="${sessionTimeout - 300}"/>
    <tags:includeScriptaculous/>
    <script type="text/javascript">
        //changes for the last url tag, since empty value was interptete as a base URL on IE7
        new SC.HttpSessionExpirationManager(${sessionTimeoutWarning}, ${sessionTimeout}, '<c:url value="/pages/ping"/>', location.href)
    </script>
</c:if>
