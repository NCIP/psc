<%@page contentType="text/javascript" language="java" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
var toDelete = $('arm-${command.arm.id}-item')
SC.highlight(toDelete)
toDelete.remove()
$('epochs-container').style.height = '<tags:epochHeight plannedCalendar="${epoch.plannedCalendar}"/>'
updateAllArmsControlVisibility(${epoch.id})
<templ:updateChanges changes="${revisionChanges}" revision="${developmentRevision}" />
