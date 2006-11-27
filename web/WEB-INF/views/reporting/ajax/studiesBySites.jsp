<%@page contentType="text/javascript"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

function replaceOptions(selectElt, text, value) {
	var select = $(selectElt)
	select.options.length = 0
	for (var i = 0 ; i < text.length ; i++) {
		select.options[i] = new Option(text[i], value[i]);
	}
}

replaceOptions('studies', [
<c:forEach items="${studies}" var="study">
	"${study.name}",
</c:forEach>], [
<c:forEach items="${studies}" var="study">
	"${study.id}",
</c:forEach>]);

replaceOptions('sitesFilter', [
<c:forEach items="${sitesSelected}" var="site">
	"${site.name}",
</c:forEach>], [
<c:forEach items="${sitesSelected}" var="site">
	"${site.id}",
</c:forEach>]);

SC.slideAndShow('studySelectorForm');
