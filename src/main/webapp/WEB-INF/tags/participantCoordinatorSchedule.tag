<%@tag%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>

<c:forEach items="${mapOfUserAndCalendar}" var="map" varStatus="keyStatus">
    <c:if test="${not empty map.value}">
        <div class="day autoclear ${commons:parity(keyStatus.index)}">
            <h3 class="day" style="width:10%;">${map.key}</h3>

            <ul>
                <table cellpadding="0">
                    <tr valign="top">
                        <td width="500" valign="top">
                            <c:forEach items="${map.value}" var="eventsMap" varStatus="mapEvent">
                             <li >
                                ${fn:split(eventsMap.key, "-")[0]} -
                                <a href="/studycalendar/pages/cal/scheduleEvent?event=${eventsMap.value.id}">${fn:split(eventsMap.key, "-")[1]}</a>
                            </li>
                            </c:forEach>
                        </td>
                        <td valign="top">
                            <c:forEach items="${map.value}" var="eventsMap" varStatus="mapEvent">
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/schedule?calendar=${eventsMap.value.scheduledArm.scheduledCalendar.id}&arm=${eventsMap.value.scheduledArm.id}">Entire Schedule</tags:restrictedListItem>
                            </c:forEach>
                        </td>
                    </tr>
                </table>
            </ul>
        </div>
    </c:if>
</c:forEach>