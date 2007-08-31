<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="arm" type="edu.northwestern.bioinformatics.studycalendar.web.template.ArmTemplate"%>
<%@attribute name="visible" type="java.lang.Boolean" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>

<%--<laf:box title="${arm.base.qualifiedName}">--%>
<c:set var="editable" value="${not arm.base.epoch.plannedCalendar.complete}"/>

<c:set var="amended" value="${arm.base.epoch.plannedCalendar.study.amended}"/>
<%--<h2 id="selected-arm-header">${arm.base.qualifiedName}</h2>--%>

<div id="selected-arm-content"<c:if test="${not visible}"> style="display: none"</c:if>>
<laf:box title="${arm.base.qualifiedName}">
<laf:division>
    <p class="controls">
        <c:if test="${amended or editable}">
            <a href="<c:url value="/pages/cal/newPeriod?arm=${arm.base.id}"/>" class="control">Add period</a>
        </c:if>
        <c:if test="${not empty arm.months}">
            <a id="show_button" href="#" class = "control">Show All</a>
            <a id="hide_button" href="#" class = "control" style="visibility: hidden;">Hide All</a>
        </c:if>
    </p>
    <c:if test="${amended or editable and empty arm.months}">
        <p class="tip">
            To begin placing activities in this part of the protocol template, click
            <a href="<c:url value="/pages/cal/newPeriod?arm=${arm.base.id}"/>" class="control">add period</a>.
            This will allow you to add a <em>period</em>, which is a (possibly repeating) series of
            days.  You will then have the opportunity to associate activities with days in your new
            period.
        </p>
    </c:if>

    <c:forEach items="${arm.months}" var="month" varStatus="monthStatus">

        <table class="periods" cellspacing="0">
            <tr>
                <c:if test="${editable}"><td class="controls"></td></c:if>
                <th class="row">Day</th>
                <c:forEach items="${month.periods[0].days}" var="day">
                    <th class="column">${day.day.number}</th>
                </c:forEach>
            </tr>
            <c:forEach items="${month.periods}" var="period" varStatus="pStatus">
                <tr class="<c:if test="${pStatus.last}">last</c:if> <c:if test="${period.resume}">resume</c:if>">
                    <c:if test="${editable}"><td class="controls"><a class="control" href="<c:url value="/pages/editPeriod?period=${period.id}"/>">Edit</a></td></c:if>
                    <th class="row">
                            ${period.name}
                    </th>
                    <c:forEach items="${period.days}" var="day" varStatus="dStatus">
                        <c:choose>
                            <c:when test="${day.inPeriod}">
                                <td class="repetition<c:if test="${day.lastDayOfSpan}"> last</c:if>">
                                    <a href="<c:url value="/pages/cal/managePeriod?id=${day.id}"/>">${day['empty'] ? '&nbsp;' : '&times;'}</a>
                                </td>
                            </c:when>
                            <c:otherwise><td class="empty<c:if test="${dStatus.last || day.lastDayOfSpan}"> last</c:if>">&nbsp;</td></c:otherwise>
                        </c:choose>

                    </c:forEach>
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
                            <a href="#" class="showArrow control" id="showArrow">&#65291</a></td>
                    </c:if>
                </c:forEach>
            </tr>
        </table>

        <a href="#" class="control showMonth">&#65291</a>
        <a href="#" class="control hideMonth" style="visibility: hidden;"><b>&#8212;</b></a>

        <div class="days">
            <c:forEach items="${month.days}" var="entry">
                <c:if test="${not empty entry.value.events}">
                    <div class="day autoclear" style="display: none;">
                        <h3>Day ${entry.key}</h3>
                        <ul>
                            <c:forEach items="${entry.value.events}" var="event">
                                <li>
                                    <a href="<c:url value="/pages/cal/managePeriod?id=${event.period.id}"/>">${event.activity.name}</a>
                                    <span class="event-details"><c:if test="${not empty event.details}">(${event.details})</c:if></span>
                                </li>
                                <li class="event-details">
                                    <c:if test="${not empty event.conditionalDetails}">Conditional </c:if>
                                    <c:if test="${not empty event.conditionalDetails}"> (${event.conditionalDetails})</c:if>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                </c:if>
            </c:forEach>
        </div>

        <c:if test="${editable and not empty arm.months and not arm.hasEvents and monthStatus.index == 0}">
            <p class="tip">
                Now that you have a period, you can add activities to it.  Click in any shaded
                part of the grid above to begin.
            </p>
        </c:if>
    </c:forEach>
</laf:division>
</laf:box>
</div>
