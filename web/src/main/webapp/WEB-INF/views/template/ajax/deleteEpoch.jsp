<%@page contentType="text/javascript" language="java" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
var toDelete = $('epoch-${command.epoch.id}')
toDelete.remove()

var epochDivs = $$('div.epoch');
epochDivs.each( function(elt)
{
    elt.style.width = '<tags:epochWidth epoch="${epoch}"/>';
    Element.removeClassName(elt, "last")
} )
Element.addClassName(epochDivs.last(), "last")

//changing the selected studySegment to the very first one on the first epoch
<jsgen:replaceHtml targetElement="selected-studySegment">
    <templ:studySegment studySegment="${template}" developmentRevision="${developmentRevision}" visible="true" canEdit="${canEdit}"/>
</jsgen:replaceHtml>

if (${not empty developmentRevision && canEdit}) {
    epochControls()
}

Element.addClassName("studySegment-${studySegment.id}-item", "selected")
SC.slideAndShow('selected-studySegment-content')

<templ:updateChanges changes="${revisionChanges}" revision="${developmentRevision}" />
<jsgen:replaceHtml targetElement="errorMessages">
    <tags:replaceErrorMessagesForTemplate/>
</jsgen:replaceHtml>
hideShowReleaseTemplateButton()
initializeNewStudySegment()