<%-- This tag must be contained in a <form:form> tag --%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@attribute name="path"%>
<c:if test="${empty path}"><c:set var="path" value="*"/></c:if>
<form:errors path="${path}">
    <c:if test="${not empty messages}">
        <ul class="errors error">
            <c:forEach items="${messages}" var="message"><li>${message}</li></c:forEach>
        </ul>
    </c:if>
</form:errors>
