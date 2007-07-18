<%@page contentType="text/javascript;charset=UTF-8"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags/schedule"%>
<jsgen:replaceHtml targetElement="selected-arm">
    <sched:scheduledArm arm="${arm}" modes="${modes}"/>
</jsgen:replaceHtml>
Element.addClassName("select-arm-${arm.id}", "selected")
SC.slideAndShow('selected-arm-content')
Event.observe('changeEventStatus', "change", function(e) {
    $('batch-change-events-status-indicator').reveal()
    Event.stop(e)
    SC.asyncSubmit('batch-change-events-status-form', {
        onComplete: function() {
            $('batch-change-events-status-indicator').conceal()
        }
    })
})
