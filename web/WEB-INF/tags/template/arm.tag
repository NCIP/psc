<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="arm" type="edu.northwestern.bioinformatics.studycalendar.web.template.ArmTemplate"%>
<%@attribute name="visible" type="java.lang.Boolean" %>
<h2 id="selected-arm-header">${arm.base.qualifiedName}</h2>

<div id="selected-arm-content"<c:if test="${not visible}"> style="display: none"</c:if>>
    <c:if test="${not arm.base.epoch.plannedCalendar.complete}">
    <p class="controls"><a href="<c:url value="/pages/newPeriod?arm=${arm.base.id}"/>">Add period</a></p>
    </c:if>
    <c:forEach items="${arm.months}" var="month">
        <table class="periods" cellspacing="0">
            <tr>
                <th class="row">Day</th>
                <c:forEach items="${month.periods[0].days}" var="day">
                    <th class="column">${day.day.number}</th>
                </c:forEach>
            </tr>
            <c:forEach items="${month.periods}" var="period" varStatus="pStatus">
                <tr class="<c:if test="${pStatus.last}">last</c:if> <c:if test="${period.resume}">resume</c:if>">
                    <th class="row">${period.name}<c:if test="${empty period.name}">&nbsp;</c:if></th>
                    <c:forEach items="${period.days}" var="day" varStatus="dStatus">
                    <c:choose>
                        <c:when test="${day.inPeriod}">
                            <td class="repetition<c:if test="${day.lastDayOfRepetition}"> last</c:if>">
                                <a href="<c:url value="/pages/managePeriod?id=${day.id}"/>">${day['empty'] ? '&nbsp;' : '&times;'}</a>
                            </td>
                        </c:when>
                        <c:otherwise><td class="empty<c:if test="${dStatus.last}"> last</c:if>">&nbsp;</td></c:otherwise>
                    </c:choose>
                    </c:forEach>
                </tr>
            </c:forEach>
        </table>

        <div class="days">
            <c:forEach items="${month.days}" var="entry">
                <c:if test="${not empty entry.value.events}">
                    <div class="day autoclear">
                        <h3>Day ${entry.key}</h3>
                        <ul>
                        <c:forEach items="${entry.value.events}" var="event">
                            <li><a href="<c:url value="/pages/managePeriod?id=${event.period.id}"/>">${event.activity.name}</a></li>
                        </c:forEach>
                        </ul>
                    </div>
                </c:if>
            </c:forEach>
        </div>
    </c:forEach>
</div>
