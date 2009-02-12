<%@taglib prefix="laf" uri="/WEB-INF/tags/laf.tld"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>

<c:forEach items="${notificationsSubjectMap}" var="notificationsSubjectMap" varStatus="keyStatus">
     <ul class="menu" style="width:100%;">
         <li class="noMarginAtAllNotifications">
            <c:if test="${not empty notificationsSubjectMap.value}">
                <a href="<c:url value="/pages/cal/schedule?calendar=${notificationsSubjectMap.value[0].assignment.scheduledCalendar.id}&studySegment=${notificationsSubjectMap.value[0].assignment.scheduledCalendar.scheduledStudySegments[0].id}"/>">
               ${notificationsSubjectMap.key.firstName} ${notificationsSubjectMap.key.lastName}</a>
            </c:if>

            <c:forEach items="${notificationsSubjectMap.value}" var="value" varStatus="valueStatus">
                <c:if test="${value.dismissed == false}">
                <c:set var="message" value="${value.message}"/>
                    <li class="noMarginForNotifications ${commons:parity(valueStatus.index)}" >
                        <c:choose>
                            <c:when test="${fn:contains(message,'pages/cal')}">
                                <a href= "<c:url value="${value.message}"/>" style="float:left">${value.title}</a>
                            </c:when>
                            <c:otherwise>${value.message}</c:otherwise>
                        </c:choose>
                        <ul style="margin-left:35%">
                            <input class="dismissNotification" style="float:left; margin-left:20px" id="dismissNotification" type="submit" onclick="dismissNotification(${value.id})" value="Dismiss"/>
                        </ul>
                        <br style="clear:both;"/>
                    </li>
                 </c:if>
        </c:forEach>
        </li>
    </ul>
</c:forEach>
