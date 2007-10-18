<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@attribute name="arm" required="true" type="edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm" %>
<%@attribute name="visible" type="java.lang.Boolean" %>
<%@attribute name="modes" type="java.util.Collection" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>


<laf:box title="${arm.name}">
    <laf:division>
        <form id="batch-form" action="<c:url value="/pages/cal/schedule/batch"/>">
            <input type="hidden" name="scheduledCalendar" value="${arm.scheduledCalendar.id}"/>
                <%--<h2 id="selected-arm-header">${arm.name}</h2>--%>
            <div id="batch-reschedule" class="subsection subcollapsible">
                <h3>Batch reschedule</h3>
                <div class="content" style="display: none">
                    <laf:division>
                        <label id="new-mode-selector-group">
                            <select name="newMode" id="new-mode-selector">
                                <option value="">Select an action...</option>
                                <option value="1">Mark/Keep as scheduled</option>
                                <option value="2">Mark occurred</option>
                                <option value="3">Mark canceled</option>
                            </select>
                        </label>
                        <label id="new-date-input-group">and shift date by <input type="text" name="dateOffset" value="7" size="4"/> days.</label>
                        <label id="new-reason-input-group">
                            Why? <input type="text" name="newReason"/>
                        </label>
                        <tags:activityIndicator id="batch-indicator"/>
                        <input type="submit" value="Submit" id="new-mode-submit"/>

                        <div class="links-row">
                            Check Events:
                            <span id="check-all-events"     class="batch-schedule-link" href="#">All</span>,
                            <span id="uncheck-all-events"   class="batch-schedule-link" href="#">None</span>,
                            <span id="check-all-conditional-events"  class="batch-schedule-link" href="#">Conditional</span>
                        </div>
                    </laf:division>
                </div>
            </div>


            <div class="content" id="selected-arm-content"<c:if test="${not visible}"> style="display: none"</c:if>>
                <div class="legend">
                    <h3>Legend</h3>
                    <ul>
                        <li class="even"><img src="<c:url value="/images/scheduled.png"/>" alt="activity indicator"/><a>  Scheduled</a></li>
                        <li class="odd" ><img src="<c:url value="/images/occurred.png"/>" alt="activity indicator"/><a>  Occurred</a></li>
                        <li class="even" ><img src="<c:url value="/images/canceled.png"/>" alt="activity indicator"/><a>  Canceled</a></li>
                        <li class="odd"><img src="<c:url value="/images/conditional.png"/>" alt="activity indicator"/><a>  Conditional</a></li>
                        <li class="even"><img src="<c:url value="/images/notApplicable.png"/>" alt="activity indicator"/><a>  NA</a></li>
                    </ul>
                </div>
                <c:forEach items="${arm.eventsByDate}" var="entry" varStatus="status">
                    <div class="day autoclear ${commons:parity(status.index)}">
                        <h3 class="day"><tags:formatDate value="${entry.key}"/></h3>

                        <ul>
                            <c:forEach items="${entry.value}" var="event">
                                <li>
                                    <input type="checkbox" value="${event.id}" name="events" class="event <c:if test="${event.conditionalState}">conditional-event</c:if>"/>
                                    <c:choose>
                                        <c:when test="${event.currentState.mode.name == 'scheduled'}">
                                            <img src="<c:url value="/images/scheduled.png"/>" alt="activity indicator"/>
                                        </c:when>
                                        <c:when test="${event.currentState.mode.name == 'conditional'}">
                                            <img src="<c:url value="/images/conditional.png"/>" alt="activity indicator"/>
                                        </c:when>
                                        <c:when test="${event.currentState.mode.name == 'occurred'}">
                                            <img src="<c:url value="/images/occurred.png"/>" alt="activity indicator"/>
                                        </c:when>
                                        <c:when test="${event.currentState.mode.name == 'canceled'}">
                                            <img src="<c:url value="/images/canceled.png"/>" alt="activity indicator"/>
                                        </c:when>
                                        <c:otherwise>
                                            <img src="<c:url value="/images/notApplicable.png"/>" alt="activity indicator"/>
                                        </c:otherwise>
                                    </c:choose>

                                        <a href="<c:url value="/pages/cal/scheduleEvent?event=${event.id}"/>" title="Event ${event.currentState.mode.name}; click to change">${event.activity.name}</a>
                                        <c:if test="${not empty event.details}"><span class="event-details">(${event.details})</span></c:if>
                                </li>
                                <li class="event-details">
                                    <c:if test="${not empty event.plannedEvent.conditionalDetails}">Conditional </c:if>
                                    <c:if test="${not empty event.plannedEvent.conditionalDetails}"> (${event.plannedEvent.conditionalDetails})</c:if>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                </c:forEach>
            </div>
        </form>
    </laf:division>
</laf:box>
