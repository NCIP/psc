<%@page contentType="text/javascript;charset=UTF-8" %>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
<jsgen:insertHtml targetElement="epoch-${studySegment.epoch.id}-studySegments" position="bottom">
    <tags:studySegmentItem studySegment="${studySegment}" />
</jsgen:insertHtml>
$('epochs-container').style.height = '<tags:epochHeight plannedCalendar="${studySegment.epoch.plannedCalendar}"/>'
createStudySegmentControls($("studySegment-${studySegment.id}-item"))
registerSelectStudySegmentHandler($("studySegment-${studySegment.id}"))
updateAllStudySegmentsControlVisibility(${studySegment.epoch.id})
SC.Main.registerClickForwarder($("studySegment-${studySegment.id}-item"))
<templ:updateChanges changes="${revisionChanges}" revision="${developmentRevision}" />
<jsgen:replaceHtml targetElement="errorMessages">
    <tags:replaceErrorMessagesForTemplate/>
</jsgen:replaceHtml>
