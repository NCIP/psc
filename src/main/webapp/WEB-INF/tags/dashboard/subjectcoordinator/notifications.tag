<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="dash" tagdir="/WEB-INF/tags/dashboard/subjectcoordinator" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@attribute name="notifications" type="java.util.Collection" required="true" %>

<c:if test="${not empty notifications}">
    <laf:box title="Notifications">
        <ul class="menu">
            <li class="autoclear">

                <c:forEach items="${notifications}" var="notification">
                    <c:set var="message" value="${notification.message}"/>
                    <c:choose>
                        <c:when test="${fn:contains(message,'pages/cal')}">
                            <a href=
                                    "<c:url value="${notification.message}"/>">${notification.title}</a> <br>
                        </c:when>
                        <c:otherwise>${notification.message}<br></c:otherwise>
                    </c:choose>


                </c:forEach>
            </li>
        </ul>

    </laf:box>


</c:if>
