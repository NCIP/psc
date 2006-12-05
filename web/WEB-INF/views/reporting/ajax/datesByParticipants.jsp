<%@page contentType="text/javascript"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

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

var participantsFilterElements = new Array(); 
<c:forEach items="${participantsSelected}" var="selectedParticipant"> 
	participantsFilterElements.push(${selectedParticipant.id}); 
</c:forEach> 
$('participantsFilter').value = participantsFilterElements