<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@attribute name="studySegment" required="true" type="edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment" %>
<%@attribute name="visible" type="java.lang.Boolean" %>
<%@attribute name="modes" type="java.util.Collection" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<laf:box title="${studySegment.name}">
    <laf:division>
        <form id="batch-form" action="<c:url value="/pages/cal/schedule/batch"/>">
            <input type="hidden" name="scheduledCalendar" value="${studySegment.scheduledCalendar.id}"/>
                <%--<h2 id="selected-studySegment-header">${studySegment.name}</h2>--%>
             <div id="batch-reschedule" class="subsection" >
                <h3>Modify selected activities</h3>
                <div class="content">
                    <laf:division>
                        <div class="links-row">
                            Select Activities:
                            <!-- TODO: why are these spans? (vs. anchors) -->
                            <span id="check-all-events"     class="batch-schedule-link" href="#">All</span>,
                            <span id="uncheck-all-events"   class="batch-schedule-link" href="#">None</span>,
                            <span id="check-all-conditional-events"  class="batch-schedule-link" href="#">Conditional</span>,
                            <span id="check-all-past-due-events"  class="batch-schedule-link" href="#">Past due</span>
                        </div>
                        <br>
                        <label id="new-mode-selector-group">
                            <select name="newMode" id="new-mode-selector">
                                <option value="-1">Select an action...</option>
                                <option value="">Move the date</option>
                                <option value="1">Mark/Keep as scheduled</option>
                                <option value="2">Mark occurred</option>
                                <option value="3">Mark canceled</option>
                                <option value="6">Mark as missed</option>
                            </select>
                        </label>
                        <label id="new-date-input-group">and shift date by <input type="text" name="dateOffset" value="0" size="4"/> days.</label>
                        <label id="move_date_by_new-date-input-group"> by <input type="text" name="moveDateOffset" value="0" size="4"/> days.</label>
                        <label id="new-reason-input-group">
                            Why? <input type="text" name="newReason"/>
                        </label>
                        <tags:activityIndicator id="batch-indicator"/>
                        <input type="submit" value="Submit" id="new-mode-submit"/>
                    </laf:division>
                </div>
            </div>


            <div class="content" id="selected-studySegment-content"<c:if test="${not visible}"> style="display: none"</c:if>>
                <div class="legend">
                    <h3>Legend</h3>
                    <ul>
                        <li class="even"><img src="<c:url value="/images/scheduled.png"/>" alt="scheduled icon"/> Scheduled</li>
                        <li class="odd"> <img src="<c:url value="/images/occurred.png"/>" alt="occurred icon"/> Occurred</li>
                        <li class="even"><img src="<c:url value="/images/canceled.png"/>" alt="canceled icon"/> Canceled</li>
                        <li class="odd"> <img src="<c:url value="/images/missed.png"/>" alt="error icon"/> Missed</li>
                        <li class="even"><img src="<c:url value="/images/conditional.png"/>" alt="conditional icon"/> Conditional</li>
                        <li class="odd"><img src="<c:url value="/images/NA.png"/>" alt="not applicable icon"/> NA</li>
                    </ul>
                </div>
                <c:forEach items="${studySegment.activitiesByDate}" var="entry" varStatus="status">
                    <div class="day autoclear ${commons:parity(status.index)}">
                        <h3><tags:formatDate value="${entry.key}"/></h3>

                        <ul>
                            <c:forEach items="${entry.value}" var="event">
                                <li>
                                    <input type="checkbox" value="${event.id}" name="events" class="event
                                        <c:if test="${event.conditionalState}">conditional-event</c:if>
                                        <c:if test="${entry.key < studySegment.todayDate}">past-due-event</c:if>"
                                    />
                                    <img src="<c:url value="/images/${event.currentState.mode.name}.png"/>" alt="Status: ${event.currentState.mode.name}"/>
                                    
                                    <a href="<c:url value="/pages/cal/scheduleActivity?event=${event.id}"/>" title="Activity ${event.currentState.mode.name}; click to change">${event.activity.name}</a>
                                    <span class="event-details">
                                        <c:if test="${not empty event.details}">
                                            ${event.details}${not empty event.plannedActivity.condition ? ';' : ''}
                                        </c:if>
                                        <c:if test="${not empty event.plannedActivity.condition}">
                                            Condition: ${event.plannedActivity.condition}
                                        </c:if>
                                    </span>
                                </li>
                                <li>
                                    <span class="event-details">
                                        <c:set var="repetitionEnding" value="th"/>
                                        <c:set var="repetition" value="${event.repetitionNumber +1}"/>
                                        <c:if test="${fn:endsWith(repetition,'1')}">
                                            <c:set var="repetitionEnding" value="st"/>
                                        </c:if>
                                        <c:if test="${fn:endsWith(repetition,'2')}">
                                            <c:set var="repetitionEnding" value="nd"/>
                                        </c:if>
                                        <c:if test="${fn:endsWith(repetition,'3')}">
                                            <c:set var="repetitionEnding" value="rd"/>    
                                        </c:if>

                                        Day ${event.plannedActivity.day} of ${event.repetitionNumber +1}${repetitionEnding} 
                                        <c:if test="${empty event.plannedActivity.period.name}">"Unnamed period"</c:if>
                                        <c:if test="${not empty event.plannedActivity.period.name}">${event.plannedActivity.period.name}</c:if>

                                    </span>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                </c:forEach>
            </div>
        </form>
    </laf:division>
</laf:box>
