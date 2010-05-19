<%@page contentType="text/javascript" language="java" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags/schedule"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
var toDelete = $('studySegment-${command.studySegment.id}-item')
SC.highlight(toDelete)
toDelete.remove()
$('epochs-container').style.height = '<tags:epochHeight plannedCalendar="${epoch.plannedCalendar}"/>'
updateAllStudySegmentsControlVisibility(${epoch.id})
<templ:updateChanges changes="${revisionChanges}" revision="${developmentRevision}" />
<jsgen:replaceHtml targetElement="selected-studySegment">
    <templ:studySegment studySegment="${template}" developmentRevision="${developmentRevision}" visible="true" canEdit="${canEdit}"/>
</jsgen:replaceHtml>
Element.addClassName("select-studySegment-${studySegment.id}", "selected")
SC.slideAndShow('selected-studySegment-content')

<jsgen:replaceHtml targetElement="errorMessages">
    <tags:replaceErrorMessagesForTemplate/>
</jsgen:replaceHtml>
if (${not empty developmentRevision && canEdit}) {
    epochControlls()
}
hideShowReleaseTemplateButton()
initializeNewStudySegment()