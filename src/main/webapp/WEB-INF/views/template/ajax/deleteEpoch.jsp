<%@page contentType="text/javascript" language="java" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
var toDelete = $('epoch-${command.epoch.id}')
toDelete.remove()

var epochDivs = $$('div.epoch');
epochDivs.each( function(elt) { elt.style.width = '<tags:epochWidth epoch="${command.epoch}"/>'; Element.removeClassName(elt, "last") } )
Element.addClassName(epochDivs.last(), "last")

//changing the selected arm to the very first one on the first epoch
<jsgen:replaceHtml targetElement="selected-arm">
    <templ:arm arm="${template}" developmentRevision="${developmentRevision}"/>
</jsgen:replaceHtml>

Element.addClassName("arm-${arm.id}-item", "selected")
SC.slideAndShow('selected-arm-content')

<templ:updateChanges changes="${revisionChanges}" revision="${developmentRevision}" />
