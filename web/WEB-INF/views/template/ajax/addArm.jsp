<%@page contentType="text/javascript;charset=UTF-8" %>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
<jsgen:insertHtml targetElement="epoch-${arm.epoch.id}-arms" position="bottom">
    <tags:armItem arm="${arm}"/>
</jsgen:insertHtml>
$('epochs-container').style.height = '<tags:epochHeight plannedCalendar="${arm.epoch.plannedCalendar}"/>'
createArmControls($("arm-${arm.id}-item"))
registerSelectArmHandler($("arm-${arm.id}"))
SC.highlight("arm-${arm.id}-item")
updateAllArmsControlVisibility(${arm.epoch.id})
SC.Main.registerClickForwarder($("arm-${arm.id}-item"))
<templ:updateChanges changes="${revisionChanges}" revision="${developmentRevision}" />
