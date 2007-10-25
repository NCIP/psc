<%@page contentType="text/javascript" %>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
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

<%--createArmControls($("arm-${epoch.arms[0].id}-item"))--%>

updateAllEpochsControlVisibility()
createAllArmControls()
SC.Main.registerClickForwarder($("arm-${epoch.arms[0].id}-item"))
<templ:updateChanges changes="${revisionChanges}" revision="${developmentRevision}" />
