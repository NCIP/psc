<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@attribute name="plannedCalendar" type="edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar"%>
<%@attribute name="selectedArm" type="edu.northwestern.bioinformatics.studycalendar.domain.Arm"%>
<div class="epochs-and-arms autoclear" style="height: ${2 + plannedCalendar.maxArmCount * 2}em">
    <tags:activityIndicator id="epochs-indicator"/>
    <c:forEach items="${plannedCalendar.epochs}" var="epoch" varStatus="status">
        <tags:epoch epoch="${epoch}" last="${status.last}" selectedArm="${selectedArm}"/>
    </c:forEach>
</div>