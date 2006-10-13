<%@page contentType="text/javascript" language="java" %>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags/schedule" %>
<jsgen:insertHtml targetElement="scheduled-arms-list" position="bottom">
    <sched:scheduledArmsListItem scheduledArm="${scheduledArm}"/>
</jsgen:insertHtml>
// defined in the page
registerSelectArmHandler('select-scheduled-arm-${scheduledArm.id}')