<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags/schedule"%>
<%@attribute name="currentStudySegment" type="edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment" %>
<%@attribute name="scheduledStudySegment" type="edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment" %>
<li id="select-studySegment-${scheduledStudySegment.id}"<c:if test="${currentStudySegment.id == scheduledStudySegment.id}"> class="selected"</c:if>>
    <a href="<sched:scheduledStudySegmentSelectUrl scheduledStudySegmentId="${scheduledStudySegment.id}"/>" id="select-scheduled-studySegment-${scheduledStudySegment.id}">${scheduledStudySegment.name}</a>
</li>