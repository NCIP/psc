<%@page contentType="text/javascript"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

function replaceOptions(selectElt, text, value) {
	var select = $(selectElt)
	select.options.length = 0
	for (var i = 0 ; i < text.length ; i++) {
		select.options[i] = new Option(text[i], value[i]);
	}
}

replaceOptions('assignedCoordinators', [
<c:forEach items="${assigned}" var="pc">
	"${pc.lastName}, ${pc.firstName}",
</c:forEach>], [
<c:forEach items="${assigned}" var="pc">
	"${pc.userId}",
</c:forEach>]);

replaceOptions('availableCoordinators', [
<c:forEach items="${available}" var="pc">
	"${pc.lastName}, ${pc.firstName}",
</c:forEach>], [
<c:forEach items="${available}" var="pc">
	"${pc.userId}",
</c:forEach>]);

SC.slideAndShow('assignmentForm')