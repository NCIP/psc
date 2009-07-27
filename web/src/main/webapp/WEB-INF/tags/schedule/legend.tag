<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div id="schedule-legend" class="legend">
    <ul>
        <li class="even"><img src="<c:url value="/images/scheduled.png"/>" alt="scheduled icon"/> Scheduled</li>
        <li class="odd"> <img src="<c:url value="/images/occurred.png"/>" alt="occurred icon"/> Occurred</li>
        <li class="even"><img src="<c:url value="/images/canceled.png"/>" alt="canceled icon"/> Canceled</li>
        <li class="odd"> <img src="<c:url value="/images/missed.png"/>" alt="error icon"/> Missed</li>
        <li class="even"><img src="<c:url value="/images/conditional.png"/>" alt="conditional icon"/> Conditional</li>
        <li class="odd"><img src="<c:url value="/images/NA.png"/>" alt="not applicable icon"/> NA</li>
    </ul>
</div>