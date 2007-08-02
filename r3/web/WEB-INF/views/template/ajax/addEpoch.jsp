<%@page contentType="text/javascript" %>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
var existingEpochs = $$('div.epoch')
existingEpochs.each( function(elt) { elt.style.width = '<tags:epochWidth epoch="${epoch}"/>' } )
var lastExisting = existingEpochs[existingEpochs.length - 1]
Element.removeClassName(lastExisting, "last")
<jsgen:insertHtml targetElement="epochs-container" position="bottom">
    <tags:epoch epoch="${epoch}" selectedArm="${null}" last="${true}"/>
</jsgen:insertHtml>
SC.highlight("epoch-${epoch.id}")
createEpochControls($("epoch-${epoch.id}-header"))
registerSelectArmHandler($("arm-${epoch.arms[0].id}"))
createArmControls($("arm-${epoch.arms[0].id}-item"))
updateAllEpochsControlVisibility()
SC.Main.registerClickForwarder($("arm-${epoch.arms[0].id}-item"))
