<%@page contentType="text/javascript" language="java" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags/schedule" %>
<jsgen:insertHtml targetElement="scheduled-arms-list" position="bottom">
    <sched:scheduledArmsListItem scheduledArm="${scheduledArm}"/>
</jsgen:insertHtml>
// defined in the page
registerSelectArmHandler('select-scheduled-arm-${scheduledArm.id}')
DEFAULT_DATES['PER_PROTOCOL'] = '<tags:formatDate value="${nextPerProtocolDate}"/>'
if ($('mode-radio-per-protocol').checked) {
    $('start-date-input').value = DEFAULT_DATES['PER_PROTOCOL']
}