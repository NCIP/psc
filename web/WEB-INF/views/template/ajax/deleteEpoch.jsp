<%@page contentType="text/javascript" language="java" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
var toDelete = $('epoch-${command.epoch.id}')
SC.highlight(toDelete)
toDelete.remove()

var epochDivs = $$('div.epoch');
epochDivs.each( function(elt) { elt.style.width = '<tags:epochWidth epoch="${command.epoch}"/>'; Element.removeClassName(elt, "last") } )
Element.addClassName(epochDivs.last, "last")
