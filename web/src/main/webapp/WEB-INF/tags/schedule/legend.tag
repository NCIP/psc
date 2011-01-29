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
                <div class="visibility-controls">
                    (
                    <a class="control show-control disabled" href="javascript:void(0)">Show</a> |
                    <a class="control hide-control enabled" href="javascript:void(0)">Hide</a>
                    )
                </div>
            </li>
        </c:forEach>
        <%--
        <li class="even autoclear" id="scheduled-legend">
            <div class="legend-entry">
                <img src="<c:url value="/images/scheduled.png"/>" alt="scheduled icon"/> Scheduled
            </div>
            <div class="visibility-controls">
                (
                <span class="show-control disabled">Show</span> |
                <span class="hide-control enabled">Hide</span>
                )
            </div>
        </li>
        <li class="odd autoclear" id="occurred-legend"> <img src="<c:url value="/images/occurred.png"/>" alt="occurred icon"/> Occurred
            <a href="#" class="showControl disableControl">Show</a>/
            <a href="#" class="hideControl enableControl">Hide</a>
        </li>
        <li class="even autoclear" id="canceled-legend"><img src="<c:url value="/images/canceled.png"/>" alt="canceled icon"/> Canceled
            <a href="#"  class="showControl disableControl">Show</a>/
            <a href="#"  class="hideControl enableControl">Hide</a>
        </li>
        <li class="odd autoclear" id="missed-legend"> <img src="<c:url value="/images/missed.png"/>" alt="error icon"/> Missed
            <a href="#" class="showControl disableControl">Show</a>/
            <a href="#" class="hideControl enableControl">Hide</a>
        </li>
        <li class="even autoclear" id="conditional-legend"><img src="<c:url value="/images/conditional.png"/>" alt="conditional icon"/> Conditional
            <a href="#" class="showControl disableControl">Show</a>/
            <a href="#" class="hideControl enableControl">Hide</a>
        </li>
        <li class="odd autoclear" id="na-legend">
            <img src="<c:url value="/images/NA.png"/>" alt="not applicable icon"/> NA
            <a href="#" class="showControl disableControl">Show</a>/
            <a href="#" class="hideControl enableControl">Hide</a>
        </li>
        --%>
    </ul>
</div>
