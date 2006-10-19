<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@attribute name="plannedCalendar" type="edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar"%>
<%@attribute name="selectedArm" type="edu.northwestern.bioinformatics.studycalendar.domain.Arm"%>
<div class="epochs-and-arms autoclear" style="height: ${2 + plannedCalendar.maxArmCount * 2}em">
<c:forEach items="${plannedCalendar.epochs}" var="epoch" varStatus="status">
    <div class="epoch${status.last ? ' last' : ''}" style="width: ${100 / fn:length(plannedCalendar.epochs) - 1}%">
        <h4 title="Epoch ${epoch.name} has ${epoch.multipleArms ? fn:length(epoch.arms) : 'no'} arms">${epoch.name}</h4>
        <div class="arms">
            <c:forEach items="${epoch.arms}" var="arm">
                <a href="#" class="arm<c:if test="${arm == selectedArm}"> selected</c:if>" id="arm-${arm.id}" title="<c:if test="${epoch.multipleArms}">${epoch.name}: </c:if>${arm.name}">${arm.name}</a>
            </c:forEach>
        </div>
    </div>
</c:forEach>
</div>