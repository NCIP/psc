<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@attribute name="arm" required="true" type="edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm" %>
<%@attribute name="visible" type="java.lang.Boolean" %>
<h2 id="selected-arm-header">${arm.name}</h2>
<div class="content" id="selected-arm-content"<c:if test="${not visible}"> style="display: none"</c:if>>
    <div class="legend">
        <h3>Legend</h3>
        <ul>
            <li class="scheduled even"><a>Scheduled</a></li>
            <li class="occurred  odd" ><a>Occurred</a></li>
            <li class="canceled even" ><a>Canceled</a></li>
        </ul>
    </div>
    <c:forEach items="${arm.eventsByDate}" var="entry" varStatus="status">
        <div class="day autoclear ${commons:parity(status.index)}">
            <h3><tags:formatDate value="${entry.key}"/></h3>
            <ul>
                <c:forEach items="${entry.value}" var="event">
                    <li class="${event.currentState.mode.name}">
                        <a href="<c:url value="/pages/scheduleEvent?event=${event.id}"/>" title="Event ${event.currentState.mode.name}; click to change">${event.plannedEvent.activity.name}</a>
                        <c:if test="${not empty event.plannedEvent.details}">(${event.plannedEvent.details})</c:if> 
                    </li>
                </c:forEach>
            </ul>
        </div>
    </c:forEach>
</div>
