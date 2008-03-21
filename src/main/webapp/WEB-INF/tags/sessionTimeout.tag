<%@tag%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty pageContext.request.userPrincipal}">
    <%--<c:set var="sessionTimeout" value="${pageContext.session.maxInactiveInterval}"/>--%>
    <%--<c:set var="sessionTimeoutWarning" value="${sessionTimeout - 5000}"/>--%>
    <c:set var="sessionTimeout" value="10"/>
    <c:set var="sessionTimeoutWarning" value="${sessionTimeout - 5}"/>
    <tags:includeScriptaculous/>
    <script type="text/javascript">
        new SC.HttpSessionExpirationManager(${sessionTimeoutWarning}, ${sessionTimeout}, '<c:url value="/pages/ping"/>', '<c:url value="/j_acegi_security_check"/>')
    </script>
    <%--<script type="text/javascript">new SC.HttpSessionExpirationManager()</script>--%>
</c:if>
