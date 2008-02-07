<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
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
            <li>
                Filter by activity type:
                <c:forEach items="${activityTypes}" var="activityType">
                    <input type="checkbox" class="activity-type" name="activityTypes[${activityType.id}]" value="true" checked="true" />&nbsp;${activityType.name}
                </c:forEach>
            </li>
            <li class="autoclear" id="subject-schedule">
                 <dash:subjectCoordinatorSchedule/>
            </li>
        </ul>
    </form>
</laf:box>
