<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="studySegment" type="edu.northwestern.bioinformatics.studycalendar.web.template.StudySegmentTemplate"%>
<%@attribute name="developmentRevision" type="edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment"%>
<%@attribute name="visible" type="java.lang.Boolean" %>
<%@attribute name="canEdit" type="java.lang.Boolean" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<c:set var="editable" value="${not empty developmentRevision}"/>

<div id="selected-studySegment-content"<c:if test="${not visible}"> style="display: none"</c:if>>
<laf:box title="${studySegment.base.qualifiedName}">
<laf:division>
    <c:url var="cycleUrl" value="/pages/cal/setCycleLength?studySegment=${studySegment.base.id}"/>
    <c:if test="${canEdit || (!canEdit && not empty studySegment.base.cycleLength)}">
        <form:form method="post" id="cycle-form" action="${cycleUrl}">
            <c:if test="${canEdit}">
               <form:errors path="*"/>
               <h5 id="cycleError"></h5>
                    Cycle length <input id="cycleLength" name="cycleLength" value="${studySegment.base.cycleLength}" size="5"/> days
                <input type="submit" value="Update" id="cycleButton"/>
            </c:if>
            <c:if test="${!canEdit && not empty studySegment.base.cycleLength}">
                The cycle length of this segment is ${studySegment.base.cycleLength} days.
            </c:if>
        </form:form>
    </c:if>
    <p class="controls addPeriod" studySegmentId=${studySegment.base.id}>
        <c:if test="${not empty studySegment.months}">
            <a id="show_button" href="#" class = "control">Show All</a>
            <a id="hide_button" href="#" class = "control" style="visibility: hidden;">Hide All</a>
        </c:if>
    </p>
    <c:if test="${editable and canEdit and empty studySegment.months}">
        <p class="tip">
            To begin placing activities in this part of the protocol template, click
            <a href="<c:url value="/pages/cal/newPeriod?studySegment=${studySegment.base.id}"/>" class="control">add period</a>.
            This will allow you to add a <em>period</em>, which is a (possibly repeating) series of
            days.  You will then have the opportunity to associate activities with days in your new
            period.
        </p>
    </c:if>

    <c:forEach items="${studySegment.months}" var="month" varStatus="monthStatus">

        <table class="periods" cellspacing="0">
            <tr>
                <c:if test="${editable}"><td class="controls"></td></c:if>
                <th class="row">Day</th>
                <c:forEach items="${month.periods[0].days}" var="day">
                    <c:set var="n" value="${day.day.number}"/>
                    <c:set var="thisCycle" value="${n.hasCycle ? n.cycleNumber : null}"/>
                    <c:choose>
                        <c:when test="${not n.hasCycle}">
                            <th class="column no-cycle">${n}</th>
                        </c:when>
                        <c:when test="${empty thisCycle or empty lastCycle or thisCycle != lastCycle}">
                            <th class="column with-cycle" title="${n}">
                                <strong class="cycle-number">C${thisCycle}</strong><span class="day-number">D${n.dayNumber}</span>
                            </th>
                            <c:set var="lastCycle" value="${thisCycle}"/>
                        </c:when>
                        <c:otherwise>
                            <th class="column cycle-day" title="${n}">
                                <span class="day-number">D${n.dayNumber}</span>
                            </th>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
                <c:set var="n" value="${null}"/>
                <c:set var="lastCycle" value="${null}"/>
            </tr>
            <c:forEach items="${month.periods}" var="period" varStatus="pStatus">
                <tr class="<c:if test="${pStatus.last}">last</c:if> <c:if test="${period.resume}">resume</c:if>">
                    <c:if test="${editable}">
                        <td class="controls editPeriod" periodId="${period.id}" />
                    </c:if>
                    <th class="row" style="white-space:nowrap; text-decoration:none;">
                            ${fn:replace(period.name, " ", "&nbsp;")}
                    </th>
                    <c:forEach items="${period.days}" var="day" varStatus="dStatus">
                        <c:choose>
                            <c:when test="${day.inPeriod}">
                                <td class="repetition<c:if test="${day.lastDayOfSpan}"> last</c:if><c:if test="${editable}"> editable</c:if>">
                                    <%-- TODO: could have non-edit mode clicks expand the events for the day --%>
                                    <c:set var="dayCheck">${day['empty'] ? '&nbsp;' : '&times;'}</c:set>
                                    <c:choose>
                                        <c:when test="${not editable}"><span>${dayCheck}</span></c:when>
                                        <c:otherwise><a href="<c:url value="/pages/cal/managePeriodActivities?period=${day.id}"/>">${dayCheck}</a></c:otherwise>
                                    </c:choose>
                                </td>
                            </c:when>
                            <c:otherwise><td class="empty<c:if test="${dStatus.last || day.lastDayOfSpan}"> last</c:if>">&nbsp;</td></c:otherwise>
                        </c:choose>

                    </c:forEach>
                    <c:if test="${editable}">
                        <td class="controls deletePeriod" periodId="${period.id}" periodName="${period.name}" studySegmentId="${studySegment.base.id}" />
                    </c:if>
                </tr>
            </c:forEach>

            <tr class="arrows">
                <c:if test="${editable}">
                    <td></td>
                </c:if>
                <td></td>
                <c:forEach items="${month.days}" var="entry">
                    <c:if test="${empty entry.value.events}">
                        <td class="hideDay"></td>
                    </c:if>
                    <c:if test="${not empty entry.value.events}">
                        <td class="showDay control">
                            <a href="#" class="showArrow control" id="showArrow">&#43;</a></td>
                    </c:if>
                </c:forEach>
            </tr>
        </table>

        <a href="#" class="control showMonth">&#43;</a>
        <a href="#" class="control hideMonth" style="visibility: hidden;"><b>&#45;</b></a>

        <div class="days">
            <c:forEach items="${month.days}" var="entry">
                <c:if test="${not empty entry.value.events}">
                    <div class="day autoclear" style="display: none;">
                        <h3><c:if test="${not entry.value.number.hasCycle}">Day </c:if>${entry.value.number}</h3>
                        <ul>
                            <c:forEach items="${entry.value.events}" var="event">
                                <li>
                                    <c:if test="${not empty event.population}">
                                        <c:set var="populationId" value="${event.population.id}"/>
                                        <c:forEach items="${study.populations}" var="population">
                                            <c:if test="${population.id eq populationId}">
                                                ${population.abbreviation}:
                                            </c:if>
                                        </c:forEach>    
                                        <%--${event.population.abbreviation}:--%>
                                    </c:if>
                                    <c:choose>
                                        <c:when test="${not editable}"><span>${event.activity.name}</span></c:when>
                                        <c:otherwise><a href="<c:url value="/pages/cal/managePeriodActivities?period=${event.period.id}"/>">${event.activity.name}</a></c:otherwise>
                                    </c:choose>
                                    <span class="event-details"><c:if test="${not empty event.details}">(${event.details})</c:if></span>
                                </li>
                                <li class="event-details">
                                    <c:if test="${not empty event.condition}">Conditional </c:if>
                                    <c:if test="${not empty event.condition}"> (${event.condition})</c:if>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                </c:if>
            </c:forEach>
        </div>

        <c:if test="${editable and not empty studySegment.months and not studySegment.hasEvents and monthStatus.index == 0}">
            <p class="tip">
                Now that you have a period, you can add activities to it.  Click in any shaded
                part of the grid above to begin.
            </p>
        </c:if>
    </c:forEach>
</laf:division>
</laf:box>
</div>
