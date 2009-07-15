<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${fn:length(subject.assignments) gt 0}">
    <table class="populationTable">
        <c:forEach items="${subject.assignments}" var="assignment" varStatus="outterCounter">
            <tr class="<c:if test="${outterCounter.index%2 != 0}">odd</c:if> <c:if test="${outterCounter.index%2 == 0}">even</c:if>">
                <td class="populationTableTD">
                    <div class="row">
                        <div class="label">Study</div>
                    </div>
                </td>
                <td class="populationTableTD">
                    <div class="row">
                        <div class="value">${assignment.name}</div>
                    </div>
                </td>
                <td class="populationTableTD">
                    <div class="row">
                        <div class="label">Populations</div>
                    </div>
                </td>
                <c:choose>
                <c:when  test="${not empty assignment.populations}">
                    <td class="populationTableTD">
                        <div class="row">
                            <div class="value">
                                <ul>
                                    <c:forEach items="${assignment.populations}" var="pop">
                                        <li>${pop.name}</li>
                                    </c:forEach>
                                    <li><a class="control"
                                           href="<c:url value="/pages/cal/schedule/populations?assignment=${assignment.id}"/>">Change</a>
                                    </li>
                                </ul>
                            </div>
                            </div>
                        </td>
                </c:when>
                <c:otherwise>
                    <td class="populationTableTD">
                        <div class="row">
                            <div class="value">
                                <em>None</em>
                            </div>
                         </div>
                    </td>
                </c:otherwise>
                </c:choose>
            </tr>

        </c:forEach>
    </table>
</c:if>