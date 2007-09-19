<%@tag%>
<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="url" required="true" %>
<%@attribute name="queryString"%>
<%@attribute name="cssClass"%>
<security:secureOperation element="${url}">
<c:set var="qs"><c:if test="${not empty queryString}">?${queryString}</c:if></c:set>
<li><a href="<c:url value="${url}${qs}"/>" class="${cssClass}"><jsp:doBody/></a></li>
</security:secureOperation>
