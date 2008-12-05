<jsp:useBean id="subject" type="edu.northwestern.bioinformatics.studycalendar.domain.Subject" scope="request"/>
<jsp:useBean id="schedule" type="edu.northwestern.bioinformatics.studycalendar.web.subject.SubjectCentricSchedule" scope="request"/>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sched" tagdir="/WEB-INF/tags/schedule" %>
<html>
<head>
    <title>Comprehensive schedule</title>
    <tags:includeScriptaculous/>
    <tags:sassLink name="schedule"/>
    <tags:sassLink name="single-schedule"/>
    <tags:javascriptLink name="subject/timeline"/>
</head>
<body>
<laf:box autopad="true" title="Timeline">
    <div id="total-timeline">
        <div class="date start-date <tags:dateClass date="${schedule.dateRange.start}"/>">
            <tags:formatDate value="${schedule.dateRange.start}"/>
        </div>
        <div class="date end-date <tags:dateClass date="${schedule.dateRange.stop}"/>">
            <tags:formatDate value="${schedule.dateRange.stop}"/>
        </div>
        <div id="total-timeline-midline"></div>
        <div id="total-timeline-refbox">
            <div id="total-timeline-refbox-midline"></div>
        </div>
    </div>
    <div id="detail-timeline-dates">
        <c:if test="${schedule.includesToday}">
            <div id="detail-timeline-date-today" class="detail-timeline-date" title="Today" style="display: none"></div>
        </c:if>
        <div id="detail-timeline-date-hover" class="detail-timeline-date" style="display: none"></div>
    </div>
    <div id="detail-timeline-block">
        <div id="detail-timeline-studies">
            <table>
                <tr class="activity-boxes">
                    <td>&nbsp;</td>
                </tr>
                <c:set var="lastStudy" value="${null}"/>
                <c:forEach items="${schedule.segmentRows}" var="row" varStatus="rowStatus">
                    <c:set var="study" value="${row.assignment.studySite.study}"/>
                    <tr class="segment-group row-${rowStatus.index} <tags:studyClass study="${study}"/>">
                        <td class="study" title="${study.assignedIdentifier}">
                            <c:choose>
                                <c:when test="${lastStudy != study}">
                                    <a href="<c:url value="/pages/cal/schedule?assignment=${row.assignment.id}"/>">${study.assignedIdentifier}</a>
                                </c:when>
                                <c:otherwise>&nbsp;</c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    <c:set var="lastStudy" value="${study}"/>
                </c:forEach>
            </table>
        </div>
        <div id="detail-timeline">
            <table>
                <tr class="activity-boxes">
                    <c:forEach items="${schedule.days}" var="day">
                        <td class="${day.detailTimelineClasses}">
                            <div class="activity-marker spacer"></div>
                            <c:forEach items="${day.activities}" var="sa">
                                <div class="activity-marker ${sa.outstanding ? 'outstanding' : 'completed'}"
                                    title="${sa.scheduledStudySegment.scheduledCalendar.assignment.studySite.study.assignedIdentifier} / ${sa.scheduledStudySegment.name} / ${sa.activity.name}"
                                    ></div>
                            </c:forEach>
                            <c:if test="${day.hasHiddenActivities}">
                                <div class="activity-marker hidden" title="One or more hidden activities"></div>
                            </c:if>
                        </td>
                    </c:forEach>
                </tr>
                <c:forEach items="${schedule.segmentRows}" var="row" varStatus="rowStatus">
                    <tr class="segment-group row-${row.rowNumber} <tags:studyClass study="${row.assignment.studySite.study}"/>">
                        <c:forEach items="${schedule.days}" var="day">
                            <td class="${day.detailTimelineClasses}">&nbsp;</td>
                        </c:forEach>
                    </tr>
                </c:forEach>
            </table>
            <c:forEach items="${schedule.segmentRows}" var="row" varStatus="rowStatus">
                <c:forEach items="${row.segments}" var="segment" varStatus="segmentStatus">
                    <c:set var="dates"><tags:formatDate value="${segment.dateRange.start}"/> to <tags:formatDate value="${segment.dateRange.stop}"/></c:set>
                    <c:set var="classes">row-${row.rowNumber} <tags:dateClass date="${segment.dateRange.start}" prefix="start_date"/> <tags:dateClass date="${segment.dateRange.stop}" prefix="end_date"/></c:set>
                    <div class="segment-box ${classes}" title="${segment.name} ${dates}" style="display: none">
                        <a href="<c:url value="/pages/cal/schedule?assignment=${row.assignment.id}"/>&studySegment=${segment.id}">${segment.name}</a>
                        <span class="dates">${dates}</span>
                    </div>
                </c:forEach>
            </c:forEach>
        </div>
    </div>
</laf:box>
<laf:box autopad="true" title="Scheduled activities" id="scheduled-activities-box">
    <sched:legend/>
    <div id="scheduled-activities">
        <c:forEach items="${schedule.days}" var="day">
            <c:if test="${day.today}">
                <div id="schedule-today-marker" title="Today"></div>
            </c:if>
            <c:if test="${not day['empty']}">
                <div class="day <tags:dateClass date="${day.date}"/>">
                    <h3 class="date"><tags:formatDate value="${day.date}"/></h3>
                    <div class="day-activities">
                        <c:if test="${not empty day.activities}">
                            <ul>
                                <c:forEach items="${day.activities}" var="sa">
                                    <c:set var="study" value="${sa.scheduledStudySegment.scheduledCalendar.assignment.studySite.study}"/>
                                    <li>
                                        <img src="<c:url value="/images/${sa.currentState.mode.name}.png"/>" alt="Status: ${sa.currentState.mode.name}"/>
                                        <span title="Study" class="study <tags:studyClass study="${study}"/>">${study.assignedIdentifier}</span>
                                        / <span title="Segment" class="segment">${sa.scheduledStudySegment.name}</span>
                                        / <a title="Scheduled activity" href="<c:url value="/pages/cal/scheduleActivity?event=${sa.id}"/>">${sa.activity.name}</a>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:if>
                        <c:if test="${day.hasHiddenActivities}">
                            <span class="hidden-activities">
                                Note: There are one or more activities on this day
                                which belong to studies or sites to which you don't
                                have access.
                            </span>
                        </c:if>
                    </div>
                </div>
            </c:if>
        </c:forEach>
    </div>
</laf:box>
</body>
</html>