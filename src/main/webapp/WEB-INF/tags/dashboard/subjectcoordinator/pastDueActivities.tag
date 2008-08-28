<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>

<%@attribute name="activities" required="true" type="java.util.Map"%>

<c:if test="${not empty activities}">
    <laf:box title="Past-due activities">
        <ul class="menu">
            <li class="autoclear">
                <c:forEach items="${activities}" var="mapOfPastDueActivities" varStatus="keyStatus">
                    <c:set var="pastDueActivities" value="0"/>
                    <c:forEach items="${mapOfPastDueActivities.key}" var="mapOfPastDueActivitiesKey" varStatus="keyStatus">
                        ${mapOfPastDueActivitiesKey.key.firstName} ${mapOfPastDueActivitiesKey.key.lastName} has
                        <c:set var="pastDueActivities" value="${mapOfPastDueActivitiesKey.value}"/>
                    </c:forEach>
                    <c:forEach items="${mapOfPastDueActivities.value}" var="mapOfPastDueActivitiesValue" varStatus="valueStatus">
                        <a href=
                            "<c:url value="/pages/cal/schedule?calendar=${mapOfPastDueActivitiesValue.key.id}"/>" > ${pastDueActivities} past-due activities</a>. Earliest is
                            from <tags:formatDate value="${mapOfPastDueActivitiesValue.value.actualDate}"/>
                        <br>
                    </c:forEach>
                </c:forEach>
            </li>
           </ul>
    </laf:box>
</c:if>
