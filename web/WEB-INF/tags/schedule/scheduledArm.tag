<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@attribute name="arm" required="true" type="edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm" %>
<%@attribute name="visible" type="java.lang.Boolean" %>
<%@attribute name="modes" type="java.util.Collection" %>

<form id="batch-form" action="<c:url value="/pages/schedule/batch"/>">
<input type="hidden" name="scheduledCalendar" value="${calendar.id}"/>
<h2 id="selected-arm-header">${arm.name}
    <label id="new-mode-selector-group">
        <select name="newMode" id="new-mode-selector">
            <option></option>
            <option value="1">Keep as scheduled</option>
            <option value="2">Mark occurred</option>
            <option value="3">Mark canceled</option>
        </select>
    </label>
    <label id="new-date-input-group">and shift date by <input type="text" name="dateOffset" value="7" size="4"/> days.</label>
    <label id="new-reason-input-group">
        Why? <input type="text" name="newReason"/>
        <tags:activityIndicator id="batch-indicator"/>
        <input type="submit" value="Submit"/>
    </label>
    <tags:activityIndicator id="batch-change-events-status-indicator"/>
</h2>

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
                        <input type="checkbox" value="${event.id}" name="events"/>
                        <a href="<c:url value="/pages/scheduleEvent?event=${event.id}"/>" title="Event ${event.currentState.mode.name}; click to change">${event.plannedEvent.activity.name}</a>
                        <c:if test="${not empty event.plannedEvent.details}"><span class="event-details">(${event.plannedEvent.details})</span></c:if> 
                    </li>
                </c:forEach>
            </ul>
        </div>
    </c:forEach>
</div>
</form>
