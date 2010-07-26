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

<jsgen:replaceHtml targetElement="selected-studySegment">
    <templ:studySegment studySegment="${template}" developmentRevision="${developmentRevision}" canEdit="${canEdit}"/>
</jsgen:replaceHtml>

//finding any selected study segments and remove selection
var arrayOfSelectedSegments = $('epochs-container').getElementsByClassName('studySegment selected')
if (arrayOfSelectedSegments !=null && arrayOfSelectedSegments.length >0) {
    for (var i=0; i<arrayOfSelectedSegments.length; i++) {
        $('epochs-container').getElementsByClassName('studySegment selected')[i].removeClassName('selected')
    }
}

//finding the value to select, based on provided Id. If this value is null, selecting the last study segment from epoch and select it
var valueToSelect = $('studySegment-${studySegment.id}-item')
if (valueToSelect == null) {
    var arrayOfSegments = $('epochs-container').getElementsBySelector( 'li.studySegment')
    if (arrayOfSegments !=null && arrayOfSegments.length>0) {
        arrayOfSegments[arrayOfSegments.length-1].addClassName("selected")
    }
} else {
    Element.addClassName("studySegment-${studySegment.id}-item", "selected")
}
SC.slideAndShow('selected-studySegment-content')

<templ:updateChanges changes="${revisionChanges}" revision="${developmentRevision}" />
<jsgen:replaceHtml targetElement="errorMessages">
    <tags:replaceErrorMessagesForTemplate/>
</jsgen:replaceHtml>
if (${not empty developmentRevision && canEdit}) {
    epochControls()
}
hideShowReleaseTemplateButton()
