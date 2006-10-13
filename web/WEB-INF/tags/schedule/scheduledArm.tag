<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@attribute name="arm" required="true" type="edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm" %>
<%@attribute name="visible" type="java.lang.Boolean" %>
<h3 id="selected-arm-header">${arm.name}</h3>
<div class="content" id="selected-arm-content"<c:if test="${not visible}"> style="display: none"</c:if>>
    <c:forEach items="${arm.eventsByDate}" var="entry">
        <div class="day autoclear">
            <h4><tags:formatDate value="${entry.key}"/></h4>
            <ul>
                <c:forEach items="${entry.value}" var="event">
                    <li>${event.plannedEvent.activity.name}</li>
                </c:forEach>
            </ul>
        </div>
    </c:forEach>
</div>
