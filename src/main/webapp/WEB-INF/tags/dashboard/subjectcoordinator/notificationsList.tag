<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:forEach items="${notifications}" var="notification">
    <c:if test="${notification.dismissed == false}">
        <c:set var="message" value="${notification.message}"/>
        <c:choose>
            <c:when test="${fn:contains(message,'pages/cal')}">
                <a href=
                        "<c:url value="${notification.message}"/>">${notification.title}</a>
            </c:when>
            <c:otherwise>${notification.message}<br></c:otherwise>
        </c:choose>
        <input class="dismissNotification" id="dismissNotification" type="submit" onclick="dismissNotification(${notification.id})" value="Dismiss"/> <br>
    </c:if>
</c:forEach>