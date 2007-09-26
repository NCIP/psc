<%@page contentType="text/javascript;charset=UTF-8"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags/schedule"%>
<jsgen:replaceHtml targetElement="selected-arm">
    <sched:scheduledArm arm="${arm}"/>
</jsgen:replaceHtml>
Element.addClassName("select-arm-${arm.id}", "selected")
SC.slideAndShow('selected-arm-content')
updateFormForState()
registerStateBasedFormMutator()
registerBatchRescheduleHandlers()
registerSubheaderCollapse()
registerCheckAllEvents()
registerUncheckAllEvents()