<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<div>Simple Report</div>
<c:forEach items="${scheduledEvents}" var="event" varStatus="status">
	<div>	
		<c:out value="${event.eventId}"/>
		<c:out value="${event.eventName}"/>
		<c:out value="${event.currentState}"/>
	</div>
	<div>
		--<c:out value="${event.date}"/>
		--<c:out value="${event.armName}"/>
		--<c:out value="${event.epochName}"/>
	</div>
	<div>
		---<c:out value="${event.participantName}"/>
		----<c:out value="${event.studyName}"/>
		----<c:out value="${event.siteName}"/>
	</div>
</c:forEach>

