<%@page contentType="text/javascript" language="java" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags/schedule" %>
<jsgen:insertHtml targetElement="scheduled-studySegments-list" position="bottom">
    <sched:scheduledStudySegmentsListItem scheduledStudySegment="${scheduledStudySegment}"/>
</jsgen:insertHtml>
// defined in the page
registerSelectStudySegmentHandler('select-scheduled-studySegment-${scheduledStudySegment.id}')
DEFAULT_DATES['PER_PROTOCOL'] = '<tags:formatDate value="${nextPerProtocolDate}"/>'
if ($('mode-radio-per-protocol').checked) {
    $('start-date-input').value = DEFAULT_DATES['PER_PROTOCOL']
}