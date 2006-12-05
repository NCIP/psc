<%@page contentType="text/javascript"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>

function replaceOptions(selectElt, text, value) {
	var select = $(selectElt)
	select.options.length = 0
	for (var i = 0 ; i < text.length ; i++) {
		select.options[i] = new Option(text[i], value[i]);
	}
}

SC.slideAndShow('dateRangeSelectorForm');


var participantsFilterList = ""; 
<c:forEach items="${participantsSelected}" var="participant"> 
	participantsFilterList = "<div> " + participantsFilterList + "${participant.lastName}, ${participant.firstName} </div>"; 
</c:forEach> 
$('participantsFilterDisplay').innerHTML = participantsFilterList;    

<jsgen:insertHtml targetElement="reportBuilderForm" position="bottom">
<c:forEach items="${participantsSelected}" var="selectedParticipant"> 
	<input type="hidden" name="participantsFilter" value="${selectedParticipant.id}"/> 
</c:forEach> 
</jsgen:insertHtml>
