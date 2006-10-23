<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@attribute name="plannedCalendar" type="edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar"%>
<%@attribute name="selectedArm" type="edu.northwestern.bioinformatics.studycalendar.domain.Arm"%>
<div class="epochs-and-arms autoclear" style="height: ${2 + plannedCalendar.maxArmCount * 2}em">
    <tags:activityIndicator id="epochs-indicator"/>
    <c:forEach items="${plannedCalendar.epochs}" var="epoch" varStatus="status">
        <div class="epoch${status.last ? ' last' : ''}" style="width: ${100 / fn:length(plannedCalendar.epochs) - 1}%">
            <h4 title="Epoch ${epoch.name} has ${epoch.multipleArms ? fn:length(epoch.arms) : 'no'} arms" id="epoch-${epoch.id}-header"><span id="epoch-${epoch.id}-name">${epoch.name}</span></h4>
            <ul class="arms">
            <c:forEach items="${epoch.arms}" var="arm">
                <li id="arm-${arm.id}-item" class="arm<c:if test="${arm == selectedArm}"> selected</c:if>">
                    <a href="#" class="arm" id="arm-${arm.id}" title="${arm.qualifiedName}">${arm.name}</a>
                </li>
            </c:forEach>
            </ul>
        </div>
    </c:forEach>
</div>