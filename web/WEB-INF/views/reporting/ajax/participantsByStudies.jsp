<%@page contentType="text/javascript"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

function replaceOptions(selectElt, text, value) {
	var select = $(selectElt)
	select.options.length = 0
	for (var i = 0 ; i < text.length ; i++) {
		select.options[i] = new Option(text[i], value[i]);
	}
}

replaceOptions('participants', [
<c:forEach items="${participants}" var="participant">
	"${participant.name}",
</c:forEach>], [
<c:forEach items="${participants}" var="participant">
	"${participant.id}",
</c:forEach>]);

SC.slideAndShow('participantSelectorForm');

