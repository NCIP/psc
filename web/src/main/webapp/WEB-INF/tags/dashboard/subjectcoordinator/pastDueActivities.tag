<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>

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
                            "<c:url value="/pages/subject?subject=${mapOfPastDueActivitiesValue.key.subject.id}"/>" > ${pastDueActivities} past-due activities</a> on study ${mapOfPastDueActivitiesValue.key.studySite.study.name}. Earliest is
                            from <tags:formatDate value="${mapOfPastDueActivitiesValue.value.actualDate}"/>
                        <br>
                    </c:forEach>
                </c:forEach>
            </li>
           </ul>
    </laf:box>
</c:if>
