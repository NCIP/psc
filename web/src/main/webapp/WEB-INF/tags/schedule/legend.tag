<%@ tag import="edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div id="schedule-legend" class="legend">
    <h4>Activity States</h4>
    <ul>
        <c:forEach items="<%= ScheduledActivityMode.values() %>" var="mode" varStatus="status">
            <li class="${commons:parity(status.index)} autoclear legend-row" id="${fn:toLowerCase(mode.name)}-legend">
                <div class="legend-entry">
                    <img src="<c:url value="/images/${mode.name}.png"/>" alt="${mode.displayName} icon"/>
                    ${mode.displayName}
                </div>
                <div class="visibility-controls visible-${!schedulePreview}">
                    (
                    <a class="control show-control disabled" href="javascript:void(0)">Show</a> |
                    <a class="control hide-control enabled" href="javascript:void(0)">Hide</a>
                    )
                </div>
            </li>
        </c:forEach>
    </ul>
</div>
