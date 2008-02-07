<%@tag%>
<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="role" required="true" %>
<%@attribute name="queryString"%>
<%@attribute name="cssClass"%>
   <security:roleSecureOperation element="${role}">
<c:set var="qs"><c:if test="${not empty queryString}">?${queryString}</c:if></c:set>
    <a href="<c:url value="${role}${qs}"/>" class="${cssClass}"><jsp:doBody/></a>
</security:roleSecureOperation>