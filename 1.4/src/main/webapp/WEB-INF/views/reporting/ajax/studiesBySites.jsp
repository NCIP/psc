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

replaceOptions('studies', [
<c:forEach items="${studies}" var="study">
	"${study.assignedIdentifier}",
</c:forEach>], [
<c:forEach items="${studies}" var="study">
	"${study.id}",
</c:forEach>]);


SC.slideAndShow('studySelectorForm');

var sitesFilterList = ""; 
<c:forEach items="${sitesSelected}" var="selectedSite"> 
	sitesFilterList = "<div> " + sitesFilterList + "${selectedSite.name} </div>"; 
</c:forEach> 
$('sitesFilterDisplay').innerHTML = sitesFilterList;

<jsgen:insertHtml targetElement="reportBuilderForm" position="bottom">
<c:forEach items="${sitesSelected}" var="selectedSite"> 
	<input type="hidden" name="sitesFilter" value="${selectedSite.id}"/> 
</c:forEach> 
</jsgen:insertHtml>
