<%@page contentType="text/javascript;charset=UTF-8"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags/schedule"%>
<jsgen:replaceHtml targetElement="selected-studySegment">
    <sched:scheduledStudySegment studySegment="${studySegment}"/>
</jsgen:replaceHtml>
Element.addClassName("select-studySegment-${studySegment.id}", "selected")
SC.slideAndShow('selected-studySegment-content')
updateFormForState()
registerStateBasedFormMutator()
registerBatchRescheduleHandlers()
registerSubheaderCollapse()
registerCheckAllEvents()
registerUncheckAllEvents()
registerCheckAllConditionalEvents()