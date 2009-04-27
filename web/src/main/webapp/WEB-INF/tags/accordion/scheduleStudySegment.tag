<%@tag%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<div class="row">
    <label id="new-mode-selector-group" class="studySegmentSelectLabel">
        <select id="studySegmentSelector" class="studySegmentSelector">
            <c:forEach items="${schedule.studies}" var="study">
                <c:forEach items="${study.plannedCalendar.epochs}" var="epoch">
                    <c:forEach items="${epoch.studySegments}" var="studySegment">
                        <option value="${study.id}_${epoch.id}_${studySegment.id}">${study.name}:${epoch.name}:${studySegment.name}</option>
                    </c:forEach>
                </c:forEach>
            </c:forEach>
        </select>
    </label>
</div>

<div class="row" style="margin-top:1em;">
    <div class="label">Start date</div>
    <div class="value"><input type="text" name="startDate" id="start-date-input"
                              value="<tags:formatDate value="${schedule.datesImmediatePerProtocol['PER_PROTOCOL']}"/>"
                              class="date" size="10"/>
        <a href="#" id="start-date-input-calbutton">
            <img src="<laf:imageUrl name='chrome/b-calendar.gif'/>" alt="Calendar" width="17" height="16" border="0" align="middle"/>
        </a>
    </div>
</div>

<div class="row" style="margin-top:1em;" id="mode-row">
    <div class="label">When?</div>
    <div class="value">
        <input type="radio" class="mode-radio" id="mode-radio-immediate"
                name="mode" value="IMMEDIATE"/> Immediately <br>
        <input type="radio" class="mode-radio" id="mode-radio-per-protocol"
                name="mode" value="PER_PROTOCOL" checked="checked"/> Per Protocol
    </div>
 </div>


<div class="alignStudySegmentButtonInTheMiddle" style="margin-top:1em;">
    <tags:activityIndicator id="next-studySegment-indicator"/>
    <input type="submit" style="margin:auto;" value="Schedule next study segment" id="next-studySegment-button" onclick="putScheduleNextSegment();"/>
</div>

