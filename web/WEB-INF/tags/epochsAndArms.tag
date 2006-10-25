<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@attribute name="id" type="java.lang.String" required="false"%>
<%@attribute name="plannedCalendar" type="edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar"%>
<%@attribute name="selectedArm" type="edu.northwestern.bioinformatics.studycalendar.domain.Arm"%>
<div <c:if test="${not empty id}">id="${id}"</c:if> class="epochs-and-arms autoclear" style="height: <tags:epochHeight plannedCalendar="${plannedCalendar}"/>">
    <tags:activityIndicator id="epochs-indicator"/>
    <c:forEach items="${plannedCalendar.epochs}" var="epoch" varStatus="status">
        <tags:epoch epoch="${epoch}" last="${status.last}" selectedArm="${selectedArm}"/>
    </c:forEach>
</div>