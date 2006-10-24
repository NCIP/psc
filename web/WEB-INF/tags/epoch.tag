<%@tag%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@attribute name="last" type="java.lang.Boolean"%>
<%@attribute name="epoch" type="edu.northwestern.bioinformatics.studycalendar.domain.Epoch" required="true" %>
<%@attribute name="selectedArm" type="edu.northwestern.bioinformatics.studycalendar.domain.Arm"%>
<div class="epoch${last ? ' last' : ''}" style="width: <tags:epochWidth epoch="${epoch}"/>" id="epoch-${epoch.id}">
    <h4 title="Epoch ${epoch.name} has ${epoch.multipleArms ? fn:length(epoch.arms) : 'no'} arms" id="epoch-${epoch.id}-header"><span id="epoch-${epoch.id}-name">${epoch.name}</span></h4>
    <ul class="arms">
    <c:forEach items="${epoch.arms}" var="arm">
        <li id="arm-${arm.id}-item" class="arm <c:if test="${arm == selectedArm}">selected</c:if>">
            <a href="#" class="arm" id="arm-${arm.id}" title="${arm.qualifiedName}">${arm.name}</a>
        </li>
    </c:forEach>
    </ul>
</div>
