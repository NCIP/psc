<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="currentArm" type="edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm" %>
<%@attribute name="scheduledArm" type="edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm" %>
<li id="select-arm-${scheduledArm.id}"<c:if test="${currentArm.id == scheduledArm.id}"> class="selected"</c:if>>
    <a href="<c:url value="/pages/schedule/select?arm=${scheduledArm.id}"/>" id="select-scheduled-arm-${scheduledArm.id}">${scheduledArm.name}</a>
</li>