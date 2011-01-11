<%@tag%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<div class="accordionRow">
    <label id="new-mode-selector-group" class="studySegmentSelectLabel">
        <select id="studySegmentSelector" class="studySegmentSelector">
            <c:forEach items="${schedule.visibleAssignments}" var="assignment">
                <c:forEach items="${assignment.studySite.study.plannedCalendar.epochs}" var="epoch">
                    <c:forEach items="${epoch.studySegments}" var="studySegment">
                        <option assignment="${assignment.gridId}" study="${assignment.studySite.study.assignedIdentifier}" studySegment="${studySegment.gridId}" startday="${studySegment.dayRange.startDay}" segmentName="${studySegment.qualifiedName}">${assignment.name}:${studySegment.qualifiedName}</option>
                    </c:forEach>
                </c:forEach>
            </c:forEach>
        </select>
    </label>
</div>

<div class="accordionRow" style="margin-top:1em;" id="mode-row">
    <div class="label">When?</div>
    <div class="value">
        <input type="radio" class="mode-radio" id="mode-radio-immediate"
                name="mode" value="IMMEDIATE"/> Immediately <br>
        <input type="radio" class="mode-radio" id="mode-radio-per-protocol"
                name="mode" value="PER_PROTOCOL" /> Per Protocol
    </div>
 </div>

<div class="accordionRow" style="margin-top:1em;">
    <div class="label">Start date</div>
    <div class="value"><input type="text" name="startDate" id="start-date-input"
                                                           class="date" size="10"/>
        <a href="#" id="start-date-input-calbutton">
            <img src="<laf:imageUrl name='chrome/b-calendar.gif'/>" alt="Calendar" width="17" height="16" border="0" align="middle"/>
        </a>
    </div>
</div>

<div class="alignStudySegmentButtonInTheMiddle">
    <tags:activityIndicator id="next-studySegment-indicator"/>
    <input type="submit" style="margin:auto;" value="Schedule next study segment" id="next-study-segment-button"/>
</div>

