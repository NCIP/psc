<%@tag%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
<c:choose>
    <c:when test="${not empty mapOfUserAndCalendar}">
        <c:forEach items="${mapOfUserAndCalendar}" var="map" varStatus="keyStatus">
            <c:if test="${not empty map.value}">
                <div class="day autoclear ${commons:parity(keyStatus.index)}">
                    <h3 class="day">${map.key}</h3>
                    <table>
                        <c:forEach items="${map.value}" var="eventsMap" varStatus="mapEvent">
                            <tr>
                                <td>
                                    <a href="<c:url value="/pages/cal/schedule?calendar=${eventsMap.value[0].scheduledStudySegment.scheduledCalendar.id}&studySegment=${eventsMap.value[0].scheduledStudySegment.id}"/>
                                       title="View entire schedule on ${eventsMap.value[0].scheduledStudySegment.scheduledCalendar.assignment.studySite.study.name} >${eventsMap.key} -</a><br>
                                </td>
                                <td>
                                    <table cellspacing="0" cellpadding="0">
                                        <c:forEach items="${eventsMap.value}" var="mapEventList" varStatus="eventFromList">
                                            <tr>
                                                <td>
                                                    <a href="<c:url value="/pages/cal/scheduleActivity?event=${mapEventList.id}"/>"> ${mapEventList.activity.name}</a><br>
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
    </c:when>
    <c:otherwise>
        <em>No scheduled or conditional events for the next ${numberOfDays} days in the selected categories.</em>
    </c:otherwise>
 </c:choose>
