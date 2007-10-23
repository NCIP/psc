<%@tag%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
<c:forEach items="${mapOfUserAndCalendar}" var="map" varStatus="keyStatus">
    <c:if test="${not empty map.value}">
        <div class="day autoclear ${commons:parity(keyStatus.index)}">
            <h3 class="day" style="width:10%;">${map.key}</h3>
            <table>
                <c:forEach items="${map.value}" var="eventsMap" varStatus="mapEvent">
                    <tr valign="top">
                        <td valign="top">
                             <li class="control">
                                 ${eventsMap.key} -
                             </li>
                        </td>
                        <td valign="top">
                            <table cellspacing="0" cellpadding="0">
                                <c:forEach items="${eventsMap.value}" var="mapEventList" varStatus="eventFromList">
                                    <tr valign="top">
                                        <td width="500" valign="top">
                                            <li class="control">
                                                <a href="/studycalendar/pages/cal/scheduleEvent?event=${mapEventList.activity.id}">${mapEventList.activity.name}</a><br>
                                            </li>
                                        </td>
                                        <td valign="top">
                                            <tags:restrictedListItem cssClass="control"  url="/pages/cal/schedule?calendar=${mapEventList.scheduledArm.scheduledCalendar.id}&arm=${mapEventList.scheduledArm.id}">Entire Schedule</tags:restrictedListItem>
                                        </td>
                                    </tr>
                                 </c:forEach>
                             </table>
                        </td>
                     </tr>
                </c:forEach>
            </table>
        </div>
    </c:if>
</c:forEach>