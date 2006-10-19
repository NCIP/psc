<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <title>Template for ${study.name}</title>
        <tags:stylesheetLink name="main"/>
        <style type="text/css">
            .epochs-and-arms {
                margin: 1em;
            }

            table.periods, table.periods tr, table.periods td, table.periods th {
                border-spacing: 0;
                border: 0 solid #666;
                margin: 1em;
            }
            table.periods td, table.periods th {
                width: 2em;
            }
            table.periods th {
                padding: 2px;
                border-right-width: 1px;
            }
            table.periods th.row {
                padding-right: 0.5em;
                text-align: right;
            }
            table.periods th.column {
                border-top-width: 1px;
            }
            table.periods tr.resume th {
                border-right: 1px solid #ddd;
            }
            table.periods td {
                padding: 0;
                border-width: 1px 1px 0 0;
                text-align: center;
            }
            table.periods a {
                text-decoration: none;
                margin: 0;
                padding: 2px;
                display: block;
                color: #444;
            }
            table.periods a:hover {
                color: #000;
            }
            table.periods td.repetition:hover {
                background-color: #ccc;
            }
            table.periods td.repetition {
                background-color: #ddd;
                border-right-width: 0;
            }
            table.periods td.empty {
                background-color: #fff;
                border-right-width: 0;
            }
            table.periods td.last {
                border-right-width: 1px;
            }
            table.periods tr.last td {
                border-bottom-width: 1px;
            }

            .days {
                margin: 0 3em 3em 5em;
            }
        </style>
    </head>
    <body>
        <h1>Template for ${study.name}</h1>

        <div id="epochs" class="section">
            <h2>Epochs and arms</h2>
            <tags:epochsAndArms plannedCalendar="${calendar}" selectedArm="${arm.base}"/>
        </div>

        <div id="selected-arm" class="section">
            <h2>${arm.base.qualifiedName}</h2>

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
                            <th class="row">${period.name}</th>
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

    </body>
</html>