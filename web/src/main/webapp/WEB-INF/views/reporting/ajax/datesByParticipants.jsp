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


var subjectsFilterList = "";
<c:forEach items="${subjectsSelected}" var="subject">
	subjectsFilterList = "<div> " + subjectsFilterList + "${subject.lastName}, ${subject.firstName} </div>";
</c:forEach> 
$('subjectsFilterDisplay').innerHTML = subjectsFilterList;

<jsgen:insertHtml targetElement="reportBuilderForm" position="bottom">
<c:forEach items="${subjectsSelected}" var="selectedSubject">
	<input type="hidden" name="subjectsFilter" value="${selectedSubject.id}"/>
</c:forEach> 
</jsgen:insertHtml>
