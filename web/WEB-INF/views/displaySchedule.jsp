<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<html>
<head>
    <title>Participant Schedule for ${participant.fullName} on ${plannedCalendar.name}</title>
    <style type="text/css">
        .section {
            border: 2px solid #666;
            margin: 1em 0;
        }
        .section p {
            margin: 1em;
        }

        .epochs-and-arms {
            margin: 1em;
        }

        h3 {
            margin: 0;
            padding: 4px;
            font-family: sans-serif;
            color: white;
            background-color: #999;
        }

        .day {
            margin: 1em;
            position: relative;
        }
        .day h4 {
            float: left;
            margin: 0;
        }
        .day ul {
            margin-left: 4em;
        }
        .day ul li {
            list-style-type: none;
        }

        .tip {
            font-size: 80%;
        }

        #scheduled-arms ul li {
            display: inline;
            padding: 0.5em;
            border: 1px solid #aaa;
        }
    </style>
</head>
<body>
<h1>Participant Schedule for ${participant.fullName} on ${plannedCalendar.name}</h1>

<div class="section">
    <h3 title="TODO">Schedule next arm</h3>
    <div class="content">
        <tags:epochsAndArms plannedCalendar="${plannedCalendar}"/>
    </div>
</div>

<div id="scheduled-arms" class="section">
    <h3>Arms scheduled</h3>
    <p class="tip">Select an arm to show its detailed schedule below.</p>
    <ul>
    <c:forEach items="${calendar.scheduledArms}" var="arm">
        <li><a href="<c:url value="/pages/schedule/arm/something"/>">${arm.name}</a></li>
    </c:forEach>
    </ul>
</div>

<div id="selected-arm" class="section">
    <h3>${arm.name}</h3>
    <c:forEach items="${arm.eventsByDate}" var="entry">
        <div class="day autoclear">
            <h4><tags:formatDate value="${entry.key}"/></h4>
            <ul>
                <c:forEach items="${entry.value}" var="event">
                    <li>${event.plannedEvent.activity.name}</li>
                </c:forEach>
            </ul>
        </div>
    </c:forEach>
</div>
</body>
</html>