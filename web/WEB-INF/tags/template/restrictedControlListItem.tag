<%@tag%>
<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="logicAllowed" type="java.lang.Boolean"%>
<%@attribute name="url" required="true" %>
<%@attribute name="queryString"%>
<security:secureOperation element="${url}" operation="ACCESS">
<c:if test="${logicAllowed}">
    <c:set var="qs"><c:if test="${not empty queryString}">?${queryString}</c:if></c:set>
    <li><a href="<c:url value="${url}${qs}"/>" class="control"><jsp:doBody/></a></li>
</c:if>
</security:secureOperation>
