<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="arm" required="true" type="edu.northwestern.bioinformatics.studycalendar.domain.Arm"%>
<%@attribute name="selectedArm" required="false" type="edu.northwestern.bioinformatics.studycalendar.domain.Arm"%>
<li id="arm-${arm.id}-item" class="arm <c:if test="${arm == selectedArm}">selected</c:if>">
    <a href="#" class="arm" id="arm-${arm.id}" title="${arm.qualifiedName}">${arm.name}</a>
</li>
