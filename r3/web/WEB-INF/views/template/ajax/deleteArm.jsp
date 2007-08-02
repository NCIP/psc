<%@page contentType="text/javascript" language="java" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
var toDelete = $('arm-${command.arm.id}-item')
SC.highlight(toDelete)
toDelete.remove()

$('epochs-container').style.height = '<tags:epochHeight plannedCalendar="${command.arm.epoch.plannedCalendar}"/>'
updateAllArmsControlVisibility(${command.arm.epoch.id})
