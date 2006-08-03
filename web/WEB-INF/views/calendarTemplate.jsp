<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

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
            color: white;
            padding: 3px;
        }
        .period0 { background-color: red; }
        .period1 { background-color: blue; }
        .period2 { background-color: green; }
    </style>
</head>
<body>
<h1>Template for ${calendar.name}</h1>
<c:choose>
    <c:when test="${fn:length(study.arms) == 1}">
        <a href="<c:url value="/pages/newPeriod?id=${study.arms[0].id}"/>">Add a period to this template</a>
    </c:when>
    <c:otherwise>
        <ul>
        <c:forEach items="${study.arms}" var="arm">
            <li><a href="<c:url value="/pages/newPeriod?id=${arm.id}"/>">Add a period to arm ${arm.name}</a></li>
        </c:forEach>
        </ul>
    </c:otherwise>
</c:choose>

<c:if test="${not empty calendar.weeks}">
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
        <c:forEach items="${calendar.weeks}" var="week" varStatus="weekStatus">
            <c:forEach items="${week.arms}" var="arm" varStatus="armStatus">
                <tr class="arm ${arm.cssClass}">
                    <c:if test="${armStatus.index == 0}"><th class="common" rowspan="${fn:length(week.arms)}">${weekStatus.count}</th></c:if>
                    <th>${arm.name}</th>
                    <c:forEach items="${arm.days}" var="day">
                        <td>
                            <c:forEach items="${day.periods}" var="period">
                                <div class="period ${period.cssClass}">${period.name}</div>
                            </c:forEach>
                        </td>
                    </c:forEach>
                </tr>
            </c:forEach>
        </c:forEach>
    </table>
</c:if>
</body>
</html>