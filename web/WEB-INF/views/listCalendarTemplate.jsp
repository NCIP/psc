<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="../security.tld" prefix="security" %> 

<html>
<head>
    <title>Template for ${calendar.name}</title>

    <tags:javascriptLink name="calendar-template"/>
</head>
<body>
<h1>Template for ${calendar.name}</h1>
<a href="<c:url value="/pages/studyList"/>">Select a different study</a>.<br>

<c:if test="${not study.plannedSchedule.complete}">
    <c:forEach items="${study.plannedSchedule.epochs}" var="epoch">
        <c:choose>
            <c:when test="${fn:length(epoch.arms) < 1}">
                <p class="error">This study is broken -- it has no arms at all</p>
            </c:when>
            <c:when test="${fn:length(epoch.arms) == 1}">
                <p><a href="<c:url value="/pages/newPeriod?id=${epoch.arms[0].id}"/>">Add a period to epoch ${epoch.name}</a></p>
            </c:when>
            <c:otherwise>
                <p>Epoch ${epoch.name}:</p>
                <c:forEach items="${epoch.arms}" var="arm">
                    <li><a href="<c:url value="/pages/newPeriod?id=${arm.id}"/>">Add a period to arm ${arm.name}</a></li>
                </c:forEach>
                </ul>
            </c:otherwise>
        </c:choose>
    </c:forEach>
</c:if>

<tags:selectCalendarView/>

    <c:forEach items="${listTemplate.epochs}" var="epoch">
        <h2>${epoch.name}</h2>
        <c:forEach items="${epoch.days}" var="day">
            <table border="1">
            <tr><th colspan="2">Day number: ${day.dayNumber}</th></tr>
                <c:forEach items="${day.arms}" var="arm">
                <tr>
                    <c:if test="${fn:length(arm.periods) == 0}">
                        <td>
                    </c:if>
                    <c:if test="${fn:length(arm.periods) > 0}">
                        <td rowspan="${fn:length(arm.periods)}">
                       </c:if>
                    Arm: ${arm.name}</td>
                <c:if test="${fn:length(arm.periods) == 0}">
                    <td>Nothing scheduled.</td>
                </c:if>
                <c:forEach items="${arm.periods}" var="period" varStatus="periodStatus">
                    <c:choose>
                        <c:when test="${periodStatus.first}">
                            <td>
                            <h4><a href="<c:url value="/pages/managePeriod?id=${period.id}"/>">${period.name}</a></h4>
                            <c:forEach items="${period.plannedEvents}" var="event">
                                ${event.activity.name}
                            </c:forEach>
                            </td></tr>
                        </c:when>
                        <c:otherwise>
                            <tr><td>
                            <h4><a href="<c:url value="/pages/managePeriod?id=${period.id}"/>">${period.name}</a></h4>
                            <c:forEach items="${period.plannedEvents}" var="event">
                                ${event.activity.name}
                            </c:forEach>
                            </td></tr>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
                </tr>
            </c:forEach>
            </tr>
            </table>
        </c:forEach>
    </c:forEach>
<security:secureOperation element="ApproveStudyCalendarTemplate" operation="ACCESS">    
<c:if test="${not study.plannedSchedule.complete}">
    <p><a href="<c:url value="/pages/markComplete?id=${study.id}"/>">Mark this template complete</a>.</p>
</c:if>
</security:secureOperation>
</body>
</html>