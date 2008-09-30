<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>

<c:forEach items="${notificationsSubjectMap}" var="notificationsSubjectMap" varStatus="keyStatus">
    <div class="autoclear ${commons:parity(keyStatus.index)}">
    <table>
        <tr>
            <td colspan="3">
                <c:if test="${not empty notificationsSubjectMap.value}">
                    <a href="<c:url value="/pages/cal/schedule?calendar=${notificationsSubjectMap.value[0].assignment.scheduledCalendar.id}&studySegment=${notificationsSubjectMap.value[0].assignment.scheduledCalendar.scheduledStudySegments[0].id}"/>">
                   ${notificationsSubjectMap.key.firstName} ${notificationsSubjectMap.key.lastName}</a> 
                </c:if>
            </td>
        </tr>
        <c:forEach items="${notificationsSubjectMap.value}" var="value" varStatus="valueStatus">
            <tr>
                <c:if test="${value.dismissed == false}">
                    <c:set var="message" value="${value.message}"/>
                    <td> - </td>
                    <td>
                        <c:choose>
                            <c:when test="${fn:contains(message,'pages/cal')}">
                                <a href= "<c:url value="${value.message}"/>">${value.title}</a>
                            </c:when>
                            <c:otherwise>${value.message}</c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <input class="dismissNotification" id="dismissNotification" type="submit" onclick="dismissNotification(${value.id})" value="Dismiss"/>
                    </td>
                </c:if>
            </tr>
        </c:forEach>
    </table>
    </div>
</c:forEach>
