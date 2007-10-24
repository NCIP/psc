<%@page contentType="text/javascript" language="java" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags/schedule"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
var toDelete = $('arm-${command.arm.id}-item')
SC.highlight(toDelete)
toDelete.remove()
$('epochs-container').style.height = '<tags:epochHeight plannedCalendar="${epoch.plannedCalendar}"/>'
updateAllArmsControlVisibility(${epoch.id})
<templ:updateChanges changes="${revisionChanges}" revision="${developmentRevision}" />
<jsgen:replaceHtml targetElement="selected-arm">
    <templ:arm arm="${template}" developmentRevision="${developmentRevision}"/>
</jsgen:replaceHtml>
Element.addClassName("select-arm-${arm.id}", "selected")
SC.slideAndShow('selected-arm-content')

