<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %> 

<html>
<head>
    <title>Template for ${calendar.name}</title>
    <style type="text/css">
        table {
            border-collapse: collapse;
        }

        td {
            vertical-align: top;
        }

        .arm {
            border: 2px solid black;
            /*border-width: 2px 0px;*/
        }
        .arm th {
            background-color: black;
            color: white;
        }
        .arm th.common {
            background-color: white;
            color: black;
        }

        .arm0    { border-color:     #999; }
        .arm0 th { background-color: #999; }
        .arm1    { border-color:     #ccc; }
        .arm1 th { background-color: #ccc; }

        .period {
            background-color: #666;
            color: black;
            padding: 3px;
        }
        .period0 { background-color: #f99; }
        .period1 { background-color: #ff9; }
        .period2 { background-color: #9f9; }
    </style>
    <tags:javascriptLink name="calendar-template"/>
</head>
<body>
<h1>Template for ${calendar.name}</h1>
<a href="<c:url value="/pages/studyList"/>">Select a different study</a>.<br>

<c:if test="${not study.plannedCalendar.complete}">
    <c:forEach items="${study.plannedCalendar.epochs}" var="epoch">
        <c:choose>
            <c:when test="${fn:length(epoch.arms) < 1}">
                <p class="error">This study is broken -- it has no arms at all</p>
            </c:when>
            <c:when test="${fn:length(epoch.arms) == 1}">
                <p><a href="<c:url value="/pages/newPeriod?id=${epoch.arms[0].id}"/>">Add a period to epoch ${epoch.name}</a></p>
            </c:when>
            <c:otherwise>
                <p>Epoch ${epoch.name}:</p>
                <ul>
                <c:forEach items="${epoch.arms}" var="arm">
                    <li><a href="<c:url value="/pages/newPeriod?id=${arm.id}"/>">Add a period to arm ${arm.name}</a></li>
                </c:forEach>
                </ul>
            </c:otherwise>
        </c:choose>
    </c:forEach>
</c:if>

<tags:selectCalendarView/>

<c:if test="${not empty calendar.epochs}">
    <c:forEach items="${calendar.epochs}" var="epoch">
        <h2>${epoch.name}</h2>
        <table class="calendar">
            <tr>
                <th></th>
                <th></th>
                <th>1</th>
                <th>2</th>
                <th>3</th>
                <th>4</th>
                <th>5</th>
                <th>6</th>
                <th>7</th>
            </tr>
            <c:forEach items="${epoch.weeks}" var="week" varStatus="weekStatus">
                <c:forEach items="${week.arms}" var="arm" varStatus="armStatus">
                    <tr class="arm ${arm.cssClass}">
                        <c:if test="${armStatus.index == 0}"><th class="common" rowspan="${fn:length(week.arms)}">${weekStatus.count}</th></c:if>
                        <th>${arm.name}</th>
                        <c:forEach items="${arm.days}" var="day">
                            <td>
                                <c:forEach items="${day.periods}" var="period">
                                    <div class="period ${period.cssClass}"><a href="<c:url value="/pages/managePeriod?id=${period.id}"/>">${period.name}</a></div>
                                    <c:forEach items="${period.plannedEvents}" var="event">
                                        <div class="period ${period.cssClass}">${event.activity.name}</div>
                                    </c:forEach>
                                </c:forEach>
                            </td>
                        </c:forEach>
                    </tr>
                </c:forEach>
                <tr><td>&nbsp;</td></tr>
            </c:forEach>
        </table>
    </c:forEach>
</c:if>
<security:secureOperation element="ApproveStudyCalendarTemplate" operation="ACCESS">
<c:if test="${not study.plannedCalendar.complete}">
    <p><a href="<c:url value="/pages/markComplete?id=${study.id}"/>">Mark this template complete</a>.</p>
</c:if>
</security:secureOperation>
<security:secureOperation element="AssignParticipantLink" operation="ACCESS">    
<c:if test="${study.plannedCalendar.complete}">
    <p><a href="<c:url value="/pages/assignParticipant?id=${study.id}"/>">Assign Participants</a>.</p>
</c:if>
</security:secureOperation>
</body>
</html>