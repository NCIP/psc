<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="dash" tagdir="/WEB-INF/tags/dashboard/subjectcoordinator" %>

<%@attribute name="numberOfDays" type="java.lang.Integer" required="true" %>
<%@attribute name="activityTypes" type="java.util.Collection" required="true"%>

<laf:box title="Current activities">
    <form action="#" id="current-activities-form">
        <ul class="menu">
            <li class="autoclear">
                 Activities for the next <input value="${numberOfDays}" id="toDate" name="toDate" size="5"/> days
            </li>
            <li class="currentActivity">
                Filter by activity type:
                <c:choose>
                    <c:when test="${activityTypesCount < 8}">
                        <c:forEach items="${activityTypes}" var="activityType">
                            <input type="checkbox" class="activity-type" name="activityTypes" value="${activityType.id}" checked="true" />&nbsp;${activityType.name}
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <select id="activityTypesList" name="activityTypes" size = 5 multiple>
                            <c:forEach items="${activityTypes}" var="activityType">
                                <option class="activity-type" value="${activityType.id}" selected="true">${activityType.name}</option>
                            </c:forEach>
                        </select>
                        <input type="button" class="activity-type" name="Button" value="Select all" onclick="selectAll('activityTypesList',true)" />
                        <input type="button" class="activity-type" name="Button" value="Select none" onclick="selectAll('activityTypesList',false)" />
                    </c:otherwise>
                </c:choose>
            </li>
            <li class="autoclear" id="subject-schedule">
                 <dash:subjectCoordinatorSchedule/>
            </li>
        </ul>
    </form>
</laf:box>
