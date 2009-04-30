<jsp:useBean id="subject" type="edu.northwestern.bioinformatics.studycalendar.domain.Subject" scope="request"/>
<jsp:useBean id="schedule" type="edu.northwestern.bioinformatics.studycalendar.web.subject.SubjectCentricSchedule" scope="request"/>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags/schedule" %>

<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="markTag" tagdir="/WEB-INF/tags/accordion" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<tags:escapedUrl var="collectionResource" value="api~v1~schedules~${subject.gridId}~partial"/>

<html>
<head>
    <title>Comprehensive Subject Schedule</title>
    <tags:includeScriptaculous/>
    <tags:sassLink name="schedule"/>
    <tags:sassLink name="single-schedule"/>
    <tags:javascriptLink name="subject/timeline"/>
    <tags:javascriptLink name="jquery/ui.core"/>
    <tags:javascriptLink name="jquery/ui.accordion"/>
    <link type="text/css" href="http://jqueryui.com/latest/themes/base/ui.all.css" rel="stylesheet" />
    <script type="text/javascript" src="http://jqueryui.com/latest/ui/ui.core.js"></script>

    <tags:stylesheetLink name="main"/>
    <tags:javascriptLink name="scheduled-activity"/>
    <tags:javascriptLink name="scheduled-activity-batch-modes"/>

     <style type="text/css">
        .myaccordion {
            position: absolute;
            right: 1em;
            width: 21%;
            height: 91%;

            font-size: 11pt;
            border: 1px solid #444;
            background-color: white;
        }

        .legendSetup {
            position: static;
            width: 90%;
        }

        .myaccordion #schedule-legend  {
            width: 70%;
            position: inherit;
        }

        .myaccordion .accordionDiv .accordionA{
            padding: 0px;
            border: 0px;
        }

        .myaccordion .value .delayAdvanceSelector {
            color:#000000;
            font-family:'Helvetica Neue',Arial,Helvetica,sans-serif;
            font-size:10pt;
            margin-bottom:1em;
        }

        .submitDelayOrAdvance {
            margin-top: 2em;
        }

        .delayAdvanceHeader {
            line-height: 2em;
        }

        .title-card {
            position:relative;
            width:70%;
            border: 0pt;
            padding: 0pt;
        }

        .card .value ul {
            margin: 0;
            padding: 0;
        }

        .card .value ul li {
            list-style-type: none;
            padding-bottom: 0.25em;
        }

        div.row, .studySegmentSelector{
            line-height:1em;
            font-size:0.9em;
            font-family:Verdana,Arial,sans-serif;
            text-decoration:none;
            margin:0;
            padding:0;
        }

        div.row div.label {
            width: 25%;
            color: #666;
        }

        div.row div.value {
            margin-left: 30%;
            line-height:1em;
        }

        .row even {
            margin-top: 0;
            float: right;
            width: 20%;
            padding: 4px;
            border: 1px solid #666;
        }

        .studySegmentSelector {
              width:inherit;
        }

        .studySegmentSelectLabel {
             width:95%
        }


    </style>

    <script type="text/javascript">
        jQuery(document).ready(function() {
            jQuery("#accordion").accordion();
        });


        function incrementDecrementDate(date, shiftByDate){
            var d = new Date();
            var time = date
            var time2 =  time + (parseInt(toDate, 10)*24*60*60*1000)
            d.setTime(time2)
            return d
        }

        function convertStringToDate(dateString){
            var day = dateString.substring(3, 5)
            var month = dateString.substring(0, 2)
            month = parseInt(month) -1
            var year = dateString.substring(6, dateString.length)

            return new Date(year, month.toString(), day);
        }

        function shiftDateByNumberOfDays(dateToShift, numberOfDaysToShift) {
            var shiftedDate = new Date();
            var timeShifted =  dateToShift + (parseInt(numberOfDaysToShift, 10)*24*60*60*1000)
            shiftedDate.setTime(timeShifted)
            return shiftedDate.toString();
        }

        function executeDelayAdvancePost() {
            var arrayOfActivities='';
            var mapOfParameters='';
            var shiftOptionForwardOrBackward = $('delayAdvanceSelector').value
            var reason = $('reason').value
            var toDate = shiftOptionForwardOrBackward*$('toDate').value
            var asOfDate = $(document.getElementById('currentDate')).value

            var asOfDateInDateFormat = convertStringToDate(asOfDate)

            <c:forEach items="${schedule.days}" var="day">
                var actualDayInDateRepresentation =  new Date(${day.date.year + 1900}, ${day.date.month}, ${day.date.date});
                var shiftedDate = shiftDateByNumberOfDays(${day.date.time}, toDate)

                if (actualDayInDateRepresentation >= asOfDateInDateFormat) {
                    <c:if test="${not day['empty']}">
                        <c:if test="${not empty day.activities}">
                            <c:forEach items="${day.activities}" var="sa">
                                arrayOfActivities = arrayOfActivities+'${sa.gridId}'+';';
                                var mapOfParametersForOne={
                                    '${sa.gridId}': {
                                        "reason": reason,
                                        "date": shiftedDate,
                                        "state" : '${sa.currentState}'
                                    }
                                };
                                mapOfParameters = mapOfParameters + mapOfParametersForOne
                            </c:forEach>
                        </c:if>
                    </c:if>
                }
            </c:forEach>
            post(arrayOfActivities, Object.toJSON(mapOfParameters) );
        }


        function gatherDataFromMarkForm() {
            var newModeSelector = $$("#new-mode-selector")[0].value
            var state=""; //default for current state is empty
            var reason="";
            var toDate=""
            if (newModeSelector == "") {                                     
                reason = $$('#new-reason-input-group input')[0].value
                toDate = $$("#move_date_by_new-date-input-group input")[0].value
            } if (newModeSelector == 1) {
                state = "scheduled"
                toDate = $$("#move_date_by_new-date-input-group input")[0].value
                reason = $$('#new-reason-input-group input')[0].value
            } if (newModeSelector == 2) {
                state = "occurred"
            } if (newModeSelector == 3) {
                state = "cancelled"
                reason = $$('#new-reason-input-group input')[0].value
            } if (newModeSelector == 6) {
                reason = $$('#new-reason-input-group input')[0].value
            }

            var params= {
                "reason": reason,
                "date": toDate,
                "currentDate": "",
                "state" : state
            }
            return Object.toJSON(params)
        }

        function executeMarkPost() {
            var arrayOfActivities='';
            var events = $$('.event')
            for(var i = 0; i< events.length; i++ ){
                if (events[i].checked){
                    arrayOfActivities = arrayOfActivities+events[i].value+';'
                }
            }
            post(arrayOfActivities, gatherDataFromMarkForm())
        }

        function post(arrayOfActivities, parameters) {
            SC.asyncRequest('${collectionResource}'+'/' + arrayOfActivities, Object.extend({
                method: 'POST',
                contentType: 'application/json',
                parameters: parameters
            }))
        }

        //todo - this will have to be properly adjusted for the resource
        function putScheduleNextSegment() {
            var parameters = gatherDataFromScheduleStudySegment()
            var studySegmentSelector = $$('#studySegmentSelector')[0].value //gives us ids for study, epoch, study_segment
            studySegmentSelector = studySegmentSelector.split("_")
            var studyId = studySegmentSelector[0];

            var href = "api~v1~studies~"+studyId+ "~sites~${site.name}"
            SC.asyncRequest(href, Object.extend({
                method: 'PUT',
                parameters: parameters
            }))
        }

        function gatherDataFromScheduleStudySegment() {
            var studySegmentSelector = $$('#studySegmentSelector')[0].value //gives us ids for study, epoch, study_segment
            studySegmentSelector = studySegmentSelector.split("_")
            var epochId = studySegmentSelector[1];
            var studySegmentId = studySegmentSelector[2];
            var immediateOrPerProtocol = $$('#mode-row input');
            for (var i= 0; i < 2; i++){
                if (immediateOrPerProtocol[i].checked) {
                    immediateOrPerProtocol = immediateOrPerProtocol[i].value
                }
            }
            var date = $('start-date-input').value;
            return Object.extend({
                "study-segment": studySegmentId,
                "date": date,
                "epoch": epochId,
                "immediateOrPerProtocol": immediateOrPerProtocol
            })
        }

    </script>

</head>
<body>
<laf:box autopad="true" title="Timeline">
    <div id="total-timeline">
        <div class="date start-date <tags:dateClass date="${schedule.dateRange.start}"/>">
            <tags:formatDate value="${schedule.dateRange.start}"/>
        </div>
        <div class="date end-date <tags:dateClass date="${schedule.dateRange.stop}"/>">
            <tags:formatDate value="${schedule.dateRange.stop}"/>
        </div>
        <div id="total-timeline-midline"></div>
        <div id="total-timeline-refbox">
            <div id="total-timeline-refbox-midline"></div>
        </div>
    </div>
    <div id="detail-timeline-dates">
        <c:if test="${schedule.includesToday}">
            <div id="detail-timeline-date-today" class="detail-timeline-date" title="Today" style="display: none"></div>
        </c:if>
        <div id="detail-timeline-date-hover" class="detail-timeline-date" style="display: none"></div>
    </div>
    <div id="detail-timeline-block">
        <div id="detail-timeline-studies">
            <table>
                <tr class="activity-boxes">
                    <td>&nbsp;</td>
                </tr>
                <c:set var="lastStudy" value="${null}"/>
                <c:forEach items="${schedule.segmentRows}" var="row" varStatus="rowStatus">
                    <c:set var="study" value="${row.assignment.studySite.study}"/>
                    <tr class="segment-group row-${rowStatus.index} <tags:studyClass study="${study}"/>">
                        <td class="study" title="${study.assignedIdentifier}">
                            <c:choose>
                                <c:when test="${lastStudy != study}">
                                    <a href="<c:url value="/pages/cal/schedule?assignment=${row.assignment.id}"/>">${study.assignedIdentifier}</a>
                                </c:when>
                                <c:otherwise>&nbsp;</c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    <c:set var="lastStudy" value="${study}"/>
                </c:forEach>
            </table>
        </div>
        <div id="detail-timeline">
            <table>
                <tr class="activity-boxes">
                    <c:forEach items="${schedule.days}" var="day">
                        <td class="${day.detailTimelineClasses}">
                            <div class="activity-marker spacer"></div>
                            <c:forEach items="${day.activities}" var="sa">
                                <div class="activity-marker ${sa.outstanding ? 'outstanding' : 'completed'}"
                                    title="${sa.scheduledStudySegment.scheduledCalendar.assignment.studySite.study.assignedIdentifier} / ${sa.scheduledStudySegment.name} / ${sa.activity.name}"
                                    ></div>
                            </c:forEach>
                            <c:if test="${day.hasHiddenActivities}">
                                <div class="activity-marker hidden" title="One or more hidden activities"></div>
                            </c:if>
                        </td>
                    </c:forEach>
                </tr>
                <c:forEach items="${schedule.segmentRows}" var="row" varStatus="rowStatus">
                    <tr class="segment-group row-${row.rowNumber} <tags:studyClass study="${row.assignment.studySite.study}"/>">
                        <c:forEach items="${schedule.days}" var="day">
                            <td class="${day.detailTimelineClasses}">&nbsp;</td>
                        </c:forEach>
                    </tr>
                </c:forEach>
            </table>
            <c:forEach items="${schedule.segmentRows}" var="row" varStatus="rowStatus">
                <c:forEach items="${row.segments}" var="segment" varStatus="segmentStatus">
                    <c:set var="dates"><tags:formatDate value="${segment.dateRange.start}"/> to <tags:formatDate value="${segment.dateRange.stop}"/></c:set>
                    <c:set var="classes">row-${row.rowNumber} <tags:dateClass date="${segment.dateRange.start}" prefix="start_date"/> <tags:dateClass date="${segment.dateRange.stop}" prefix="end_date"/></c:set>
                    <div class="segment-box ${classes}" title="${segment.name} ${dates}" style="display: none">
                        <a href="<c:url value="/pages/cal/schedule?assignment=${row.assignment.id}"/>&studySegment=${segment.id}">${segment.name}</a>
                        <span class="dates">${dates}</span>
                    </div>
                </c:forEach>
            </c:forEach>
        </div>
    </div>
</laf:box>
<laf:box autopad="true" title="Scheduled activities" id="scheduled-activities-box">
    <%--TODO - move css to display.jsp, make accordion fit in the box--%>
    <div id="accordion" class="myaccordion">
        <div class="accordionDiv">
            <h3><a class="accordionA" href="#">Legend </a></h3>
        </div>
        <div><sched:legend/> </div>

          <%--************ Delay Or Advance Portion**********--%>
        <div class="accordionDiv">
            <h3><a class="accordionA" href="#">Delay or Advance</a></h3>
        </div>

        <div class="content" style="display: none">
            <laf:division>
                <div class="value">
                    <h4>Study:
                    <c:if test="${not empty schedule.studies && fn:length(schedule.studies) gt 1}">
                        <select id="studySelector" class="delayAdvanceSelector">
                            <option value="all" selected="true">All Studies </option>
                            <c:forEach items="${schedule.studies}" var="row" varStatus="rowStatus">
                                <option value="${row.id}">${row.name}</option>
                            </c:forEach>
                         </select>
                    </c:if>
                    <c:if test="${fn:length(schedule.studies) eq 1}">
                        <c:forEach items="${schedule.studies}" var="row" varStatus="rowStatus">
                            <input id="studyId" type="hidden" value="${row.id}"/>${row.name}
                        </c:forEach>
                    </c:if>

                        <%--if we decide to delayOrAdvance study segment--%>
                        <%--<c:if test="${not empty schedule.segmentRows}">--%>
                            <%--<option value="all" selected="true"> Full Study </option>--%>
                        <%--</c:if>--%>
                        <%--<c:forEach items="${schedule.segmentRows}" var="row" varStatus="rowStatus">--%>
                            <%--<c:forEach items="${row.segments}" var="segment" varStatus="segmentStatus">--%>
                                <%--<option value="${segment.id}" <c:if test="${segment.id == selectedId}">selected</c:if>>${segment.name}</option>--%>
                            <%--</c:forEach>--%>
                        <%--</c:forEach>--%>


                        <%--if we keep the perProtocol functionality, then the code is below--%>
                         <%--<input id="toDate" size="5" path="toDate" value="7"/>--%>
                            <%--day(s) as of date:  <input id="currentDate" path="currentDate" size="15"--%>
                            <%--value="<tags:formatDate value="${schedule.datesImmediatePerProtocol['PER_PROTOCOL']}"/>" class="date"/>--%>
                    <%--</select> --%>
                    </h4>
                </div>
                <h4 class="delayAdvanceHeader">
                    <select id="delayAdvanceSelector" name="delayAdvanceSelector">
                        <option value="1" selected="true" >Delay</option>
                        <option value="-1">Advance</option>
                    </select> scheduled or conditional activities by <input id="toDate" size="5" path="toDate" value="7"/> day(s) as
                    of date:  <input id="currentDate" path="currentDate" size="15" value="" class="date"/>
                    <a href="#" id="currentDate-calbutton">
                        <img src="<laf:imageUrl name='chrome/b-calendar.gif'/>" alt="Calendar" width="17" height="16" border="0" align="absmiddle"/>
                    </a>
                    Reason: <input id="reason" class="reason" path="reason" value=""/>
                    
                </h4>
                <h4><input class="submitDelayOrAdvance" type="submit" value="Submit" onclick="executeDelayAdvancePost();"/><tags:activityIndicator id="indicator"/></h4>
            </laf:division>
        </div>

        <%--*********** Mark Portion****************--%>
        <div class="accordionDiv">
        <h3><a class="accordionA" href="#">Mark</a></h3>
        </div>
        <div class="content">
            <markTag:markActivity/>
            <input type="submit" value="Submit" class="submitDelayOrAdvance"  id="new-mode-submit" onclick="executeMarkPost()"/>
        </div>
        <div class="accordionDiv">
          <h3><a class="accordionA" href="#">Next Segment</a></h3>
        </div>
        <div>
            <markTag:scheduleStudySegment/>
        </div>
        <div class="accordionDiv">
          <h3><a class="accordionA" href="#">Population</a></h3>
        </div>
        <div class="content card title-card">
            <markTag:population/>
        </div>
    </div>

    <%--<sched:legend/>--%>
    <form id="batch-form" style="font-weight:normal;">
        <div id="scheduled-activities">
            <c:forEach items="${schedule.days}" var="day">
                <c:if test="${day.today}">
                    <div id="schedule-today-marker" title="Today"></div>
                </c:if>
                <c:if test="${not day['empty']}">
                    <div class="day <tags:dateClass date="${day.date}"/>">
                        <h3 class="date"><tags:formatDate value="${day.date}"/></h3>
                        <div class="day-activities">
                            <c:if test="${not empty day.activities}">
                                <ul>
                                    <c:forEach items="${day.activities}" var="sa">
                                        <c:set var="study" value="${sa.scheduledStudySegment.scheduledCalendar.assignment.studySite.study}"/>
                                        <li>
                                            <input type="checkbox" value="${sa.gridId}" name="events" class="event <c:if test="${sa.conditionalState}">conditional-event</c:if>
                                            <c:if test="${(sa.conditionalState || sa.scheduledState) && day.date < sa.scheduledStudySegment.todayDate}">past-due-event</c:if>"/>
                                            <img src="<c:url value="/images/${sa.currentState.mode.name}.png"/>" alt="Status: ${sa.currentState.mode.name}"/>
                                            <span title="Study" class="study <tags:studyClass study="${study}"/>">${study.assignedIdentifier}</span>
                                            / <span title="Segment" class="segment">${sa.scheduledStudySegment.name}</span>
                                            / <a title="Scheduled activity" href="<c:url value="/pages/cal/scheduleActivity?event=${sa.id}"/>">${sa.activity.name}</a>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:if>
                            <c:if test="${day.hasHiddenActivities}">
                                <span class="hidden-activities">
                                    Note: There are one or more activities on this day
                                    which belong to studies or sites to which you don't
                                    have access.
                                </span>
                            </c:if>
                        </div>
                    </div>
                </c:if>
            </c:forEach>
        </div>
    </form>
</laf:box>
</body>
</html>