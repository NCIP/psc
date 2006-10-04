<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@attribute name="plannedCalendar" type="edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar"%>
<div class="epochs-and-arms autoclear" style="height: ${2 + plannedCalendar.maxArmCount * 2}em">
<c:forEach items="${plannedCalendar.epochs}" var="epoch" varStatus="status">
    <div class="epoch${status.last ? ' last' : ''}" style="width: ${80 / fn:length(plannedCalendar.epochs)}%">
        <h4 title="Epoch ${epoch.name} has ${epoch.multipleArms ? fn:length(epoch.arms) : 'no'} arms">${epoch.name}</h4>
        <div class="arms">
            <c:forEach items="${epoch.arms}" var="arm">
                <a href="#" class="arm" id="arm-${arm.id}">${arm.name}</a>
            </c:forEach>
        </div>
    </div>
</c:forEach>
</div>