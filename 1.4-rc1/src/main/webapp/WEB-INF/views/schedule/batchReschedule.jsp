<%@page contentType="text/javascript"%>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags/schedule" %>
selectStudySegment('<sched:scheduledStudySegmentSelectUrl scheduledStudySegmentId="${empty param.scheduledStudySegment ? scheduledCalendar.currentStudySegment.id : param.scheduledStudySegment}"/>')