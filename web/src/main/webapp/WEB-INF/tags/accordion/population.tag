<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>

<c:forEach items="${schedule.visibleAssignments}" var="assignment" varStatus="outerCounter">
    <div class="row ${commons:parity(outerCounter.index)}">
        <div class="label">${assignment.name}</div>
        <div class="value">
            <ul>
                <c:choose>
                    <c:when test="${empty assignment.populations}">
                        <li class="none">None</li>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${assignment.populations}" var="pop">
                            <li>${pop.name}</li>
                        </c:forEach>
                        <c:if test="${canUpdateSchedule}">
                            <a class="control"
                                href="<c:url value="/pages/cal/schedule/populations?assignment=${assignment.id}"/>">Change</a>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
    </div>
</c:forEach>
