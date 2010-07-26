<%@tag%>
<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="showIf" required="true" %>
<%@attribute name="url" required="true" %>
<%@attribute name="cssClass"%>
<c:if test="${showIf}">
    <li><a href="<c:url value="${url}"/>" class="${cssClass}"><jsp:doBody/></a></li>
</c:if>
