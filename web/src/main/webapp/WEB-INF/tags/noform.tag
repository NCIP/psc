<%-- This tag has the same side-effects as the spring form:form tag, but does not actually write
a <form> HTML tag --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:nestedPath path="command">
    <jsp:doBody/>
</spring:nestedPath>