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

replaceOptions('participants', [
<c:forEach items="${participants}" var="participant">
	"${participant.lastName}, ${participant.firstName}",
</c:forEach>], [
<c:forEach items="${participants}" var="participant">
	"${participant.id}",
</c:forEach>]);

SC.slideAndShow('participantSelectorForm');

var studiesFilterList = ""; 
<c:forEach items="${studiesSelected}" var="selectedStudy"> 
	studiesFilterList = "<div> " + studiesFilterList + "${selectedStudy.name} </div>"; 
</c:forEach> 
$('studiesFilterDisplay').innerHTML = studiesFilterList;     

<jsgen:insertHtml targetElement="reportBuilderForm" position="bottom">
<c:forEach items="${studiesSelected}" var="selectedStudy"> 
	<input type="hidden" name="studiesFilter" value="${selectedStudy.id}"/> 
</c:forEach> 
</jsgen:insertHtml>