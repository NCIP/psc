<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="row odd">
    <div class="label">Study</div>
    <div class="value">${schedule.segmentRows[0].assignment.studySite.study.name}</div>
</div>
<div class="row even">
    <div class="label">Site</div>
    <div class="value">${schedule.segmentRows[0].assignment.studySite.site.name}</div>
</div>
<div class="row odd">
    <div class="label">Current amendment</div>
    <div class="value">
        ${schedule.segmentRows[0].assignment.currentAmendment.displayName}
    </div>
</div>
<c:if test="${not empty schedule.segmentRows[0].assignment.studySite.study.populations}">
    <div class="row even">
        <div class="label">Populations</div>
        <div class="value">
            <ul>
                <c:if test="${empty schedule.segmentRows[0].assignment.populations}"><em>None</em></c:if>
                <c:forEach items="${schedule.segmentRows[0].assignment.populations}" var="pop">
                    <li>${pop.name}</li>
                </c:forEach>
                <li><a class="control"
                       href="<c:url value="/pages/cal/schedule/populations?assignment=${schedule.segmentRows[0].assignment.id}"/>">Change</a>
                </li>
            </ul>
        </div>
    </div>
</c:if>