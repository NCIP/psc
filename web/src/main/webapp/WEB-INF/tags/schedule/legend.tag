<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div id="schedule-legend" class="legend">
    <h3>Legend</h3>
    <ul>
        <li class="even" id="scheduled-legend"><img align="absmiddle" src="<c:url value="/images/scheduled.png"/>" alt="scheduled icon"/> Scheduled
            <a href="#" class="showControl disableControl">Show</a>/
            <a href="#" class="hideControl enableControl">Hide</a>
        </li>
        <li class="odd" id="occurred-legend"> <img align="absmiddle" src="<c:url value="/images/occurred.png"/>" alt="occurred icon"/> Occurred
            <a href="#" class="showControl disableControl">Show</a>/
            <a href="#" class="hideControl enableControl">Hide</a>
        </li>
        <li class="even" id="canceled-legend"><img align="absmiddle" src="<c:url value="/images/canceled.png"/>" alt="canceled icon"/> Canceled
            <a href="#"  class="showControl disableControl">Show</a>/
            <a href="#"  class="hideControl enableControl">Hide</a>
        </li>
        <li class="odd" id="missed-legend"> <img align="absmiddle" src="<c:url value="/images/missed.png"/>" alt="error icon"/> Missed
            <a href="#" class="showControl disableControl">Show</a>/
            <a href="#" class="hideControl enableControl">Hide</a>
        </li>
        <li class="even" id="conditional-legend"><img align="absmiddle" src="<c:url value="/images/conditional.png"/>" alt="conditional icon"/> Conditional
            <a href="#" class="showControl disableControl">Show</a>/
            <a href="#" class="hideControl enableControl">Hide</a>
        </li>
        <li class="odd" id="na-legend"> <img align="absmiddle" src="<c:url value="/images/NA.png"/>" alt="not applicable icon"/> NA
            <a href="#" class="showControl disableControl">Show</a>/
            <a href="#" class="hideControl enableControl">Hide</a>
        </li>
    </ul>
</div>
