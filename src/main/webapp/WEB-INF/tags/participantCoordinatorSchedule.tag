<%@tag%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
<table>
     <ul class="menu">
        <li class="controls ">
            <c:forEach items="${mapOfUserAndCalendar}" var="map" varStatus="keyStatus">
                <tr>
                    <td valign="top">
                        <li class="controls ">
                        ${map.key}
                        </li>
                    </td>
                    <td valign="top">
                        <c:forEach items="${map.value}" var="eventsMap" varStatus="mapEvent">
                            <li class="controls ">
                                ${eventsMap.key}  <br>
                            </li>
                        </c:forEach>
                    </td>
                    <td valign="top">
                        <c:forEach items="${map.value}" var="eventsMap" varStatus="mapEvent">
                            <li class="controls ">
                                   <ul class="controls">
                                    <tags:restrictedListItem cssClass="control" url="/pages/cal/scheduleEvent?event=${eventsMap.value.id}">Update</tags:restrictedListItem>
                                    <tags:restrictedListItem cssClass="control" url="/pages/cal/schedule?calendar=${eventsMap.value.scheduledArm.scheduledCalendar.id}&arm=${eventsMap.value.scheduledArm.id}">Entire Schedule</tags:restrictedListItem>
                                 </ul>
                            </li>
                        </c:forEach>
                    </td>
                </tr>
            </c:forEach>
        </li
    </ul>
</table>
