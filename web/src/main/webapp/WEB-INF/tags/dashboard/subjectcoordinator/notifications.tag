<%@taglib prefix="laf" uri="/WEB-INF/tags/laf.tld"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tag" tagdir="/WEB-INF/tags/dashboard/subjectcoordinator" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@attribute name="notifications" type="java.util.Collection" required="true" %>

<c:if test="${not empty notifications}">
    <!--need to check if notifications are dismissed-->
    <c:set var="dismissed" value="true"/>
        <c:forEach items="${notifications}" var="notification">
            <c:if test="${notification.dismissed == false}">
                <c:set var="dismissed" value="false"/>
            </c:if>
        </c:forEach>
    <c:if test="${dismissed == false}" >
    <laf:box title="Notifications">
        <ul class="menu">
            <li class="autoclear" id="notification-table">
                <tag:notificationsList/>
            </li>
        </ul>

    </laf:box>
    </c:if>

</c:if>
