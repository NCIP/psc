<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags/schedule" %>

<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="markTag" tagdir="/WEB-INF/tags/accordion" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<tags:escapedUrl var="collectionResource" value="api~v1~schedules~${subject.gridId}~batchUpdate"/>

<html>
<head>
    <title>Comprehensive Subject Schedule</title>
    <tags:includeScriptaculous/>
    <tags:sassLink name="schedule"/>
    <tags:sassLink name="single-schedule"/>
    <tags:stylesheetLink name="main"/>


    <tags:javascriptLink name="resig-templates" />
    <script type="text/javascript">
        Timeline_ajax_url = "<c:url value="/js/simile-timeline/2.3.0/timeline_ajax/simile-ajax-api.js?bundle=true"/>";
        Timeline_urlPrefix = "<c:url value="/js/simile-timeline/2.3.0/timeline_js/"/>";
        Timeline_parameters = "bundle=true";
    </script>
    <tags:javascriptLink name="simile-timeline/2.3.0/timeline_js/timeline-api"/>
    <tags:javascriptLink name="psc-tools/async-updater"/>
    <tags:javascriptLink name="psc-tools/misc"/>
    <tags:javascriptLink name="psc-tools/range"/>
    <tags:javascriptLink name="subject/schedule-structure"/>
    <tags:javascriptLink name="subject/schedule-data"/>
    <tags:javascriptLink name="subject/schedule-timeline"/>
    <tags:javascriptLink name="subject/timeline-ext"/>
    <tags:javascriptLink name="subject/schedule-list"/>
    <tags:javascriptLink name="subject/schedule-init"/>

    <tags:javascriptLink name="jquery/jquery-ui-1.7.2.custom.min"/>
    <tags:stylesheetLink name="jquery/jquery-ui-1.7.2.custom"/>

    <c:choose>
        <c:when test="${schedulePreview}">
            <tags:javascriptLink name="schedule-preview/schedule-preview"/>
            <tags:escapedUrl var="previewResource" value="api~v1~studies~${study.assignedIdentifier}~template~${amendmentIdentifier}~schedulePreview.json" />
        </c:when>
        <c:otherwise>
            <%-- TODO: reenable when the content is updated to work with the new page
                 TODO: these scripts should be in the subject folder with everything else
                       unless they are shared with other pages.
                        --%>
            <tags:javascriptLink name="scheduled-activity"/>
            <tags:javascriptLink name="scheduled-activity-batch-modes"/>

            <%-- TODO: there should be a subject in preview mode, too (a fake one) --%>
            <jsp:useBean id="subject" type="edu.northwestern.bioinformatics.studycalendar.domain.Subject" scope="request"/>
            <jsp:useBean id="schedule" type="edu.northwestern.bioinformatics.studycalendar.web.subject.SubjectCentricSchedule" scope="request"/>
            <script type="text/javascript">
                psc.subject.ScheduleData.uriGenerator(function () {
                    return psc.tools.Uris.relative("/api/v1/schedules/${subject.gridId}.json");
                    <%--return SC.relativeUri("/api/v1/subjects/${subject.gridId}/schedule.json");--%>
                })
            </script>
        </c:otherwise>
    </c:choose>
     <style type="text/css">
         /* TODO: use a descriptive selectors than "myaccordion", "accordionHeader", etc. */
        .myaccordion {
            font-size: 10pt;
            border: 1px solid #444;
            background-color: white;
            overflow-x: auto;
            height: 297px;
        }

        .legendSetup {
            position: static;
            width: 90%;
        }

        .myaccordion #schedule-legend  {
            width: 70%;
            position: static;
            right: 0;
            top: 0;
            font-size:9pt;
        }

        .myaccordion .accordionDiv .accordionHeader{
            padding: 0px;
            border: 0px;
        }

        .myaccordion .value .delayAdvanceSelector {
            color:#000000;
            font-family:'Helvetica Neue',Arial,Helvetica,sans-serif;
            font-size:9pt;
            margin-bottom:1em;
        }

        .submitDelayOrAdvance {
            margin-top: 2em;
        }

        .delayAdvanceHeader {
            line-height: 2em;
        }

        .card {
            border: 0pt;
        }

        div.accordionRow div.value{
            font-weight:normal;
            margin-left:7em;
            font-size:9pt;
        }

        div.accordionRow div.label{
            font-weight:bold;
            text-align:right;
            float:left;
            font-size:9pt;
            line-height:1em;
        }

        div.delayOrAdvanceBlock{
            font-size:9pt;
            line-height:1.5em;
        }

        div.accordionRow{
            font-size:9pt;
            margin:1px 0;
            padding:2px;
        }

        div.populationRow div.label{
            font-size:9pt;
            float:left;
            font-weight:bold;
            font-size:9pt;
            margin-left:0.5em;
            text-align:right;
            width:7em;
            line-height:1.5em;

        }

        div.populationRow div.value{
            font-weight:normal;
            margin-left:8em;
            line-height:1.5em;
            font-size:9pt;
        }

        div.row div.label{
            float:left;
            font-weight:bold;
            font-size:9pt;
            margin-left:0.5em;
            text-align:right;
            width:7em;
        }

        div.row div.value {
            font-weight:normal;
            margin-left:8em;
            font-size:9pt;
        }

        .card .value ul {
            margin: 0;
            padding: 0;
        }

        .card .value ul li {
            list-style-type: none;
            padding-bottom: 0.25em;
        }

        div.studySegmentSelector {
            line-height:1em;
            font-size:9pt;
            font-family:Verdana,Arial,sans-serif;
            text-decoration:none;
            margin:0;
            padding:0;
        }

        /* TODO: never use presentational class names */
       .alignStudySegmentButtonInTheMiddle {
            font-size:9pt;
            margin-top:1em;"
        }

        div.previewRow{
            float:left;
            vertical-align:middle;
            width:100%;
            padding: 2px;
            margin: 1px 0;
        }

    </style>

    <script type="text/javascript">
        jQuery(document).ready(function() {
            jQuery(".myaccordion").accordion({ autoHeight: true, collapsible: true, navigation: true });
        });

        function shiftDateByNumberOfDays(dateToShiftInMilliseconds, numberOfDaysToShift) {
            var shiftedDate1 = new Date(psc.tools.Dates.incrementDecrementDate(dateToShiftInMilliseconds, numberOfDaysToShift))
            return psc.tools.Dates.utcToApiDate(shiftedDate1)
        }

        function executeDelayAdvancePost() {
            $('delayOrAdvance-indicator').reveal()
            var mapOfParameters={};
            var shiftOptionForwardOrBackward = $('delayAdvanceSelector').value
            var reason = $('reason').value
            var toDate = shiftOptionForwardOrBackward*$('toDate').value
            var asOfDate = $(document.getElementById('currentDate')).value

            var asOfDateInDateFormat = psc.tools.Dates.displayDateToUtc(asOfDate)

            <c:forEach items="${schedule.days}" var="day">
                var actualDayInDateRepresentation =  new Date(${day.date.year + 1900}, ${day.date.month}, ${day.date.date});
                var shiftedDate = shiftDateByNumberOfDays(${day.date.time}, toDate)

                if (actualDayInDateRepresentation >= asOfDateInDateFormat) {
                    <c:if test="${not day['empty']}">
                        <c:if test="${not empty day.activities}">
                            <c:forEach items="${day.activities}" var="sa">
                                mapOfParameters['${sa.gridId}']={
                                    "reason": reason,
                                    "date": shiftedDate,
                                    "state" : '${sa.currentState.mode}'
                                };
                            </c:forEach>
                        </c:if>
                    </c:if>
                }
            </c:forEach>
            post(Object.toJSON(mapOfParameters) );
        }


        function executeMarkPost() {
            $('mark-indicator').reveal()
            var newModeSelector = $$("#new-mode-selector")[0].value
            var state=""; //default for current state is empty
            var reason="";
            var toDate=0;
            if (newModeSelector == "moveDate") {
                reason = $$('#new-reason-input-group input')[0].value;
                toDate = $$("#move_date_by_new-date-input-group input")[0].value;
            } if (newModeSelector == "markAsScheduled") {
                state = "scheduled";
                toDate = $$("#move_date_by_new-date-input-group input")[0].value;
                reason = $$('#new-reason-input-group input')[0].value;
            } if (newModeSelector == "markAsOccurred") {
                state = "occurred";
            } if (newModeSelector == "markAsCancelled") {
                state = "cancelled";
                reason = $$('#new-reason-input-group input')[0].value;
            } if (newModeSelector == "markAsMissed") {
                reason = $$('#new-reason-input-group input')[0].value;
            }

            var mapOfParameters = {};
            var events = $$('.event')
            var checkedEvents = events.select(function(e) { return e.checked })

            var isStateEmpty= (state == "");
            for (var i = 0; i< checkedEvents.length; i++ ){
                if (isStateEmpty) {
                    state = $$('.event')[2].up('li').className.toLowerCase()
                }
                var date = checkedEvents[i].up('.day').down('h3').innerHTML
                var activityKey = checkedEvents[i].value
                var dateInDateFormat = psc.tools.Dates.apiDateToUtc(date)
                var shiftedDate = shiftDateByNumberOfDays(dateInDateFormat.getTime(), toDate)
                mapOfParameters[activityKey] = {
                        "reason": reason,
                        "date": shiftedDate,
                        "state" : state
                };
            }
            post(Object.toJSON(mapOfParameters) );
        }

        function post(parameters) {
            SC.asyncRequest('${collectionResource}'+'/', Object.extend({
                method: 'POST',
                contentType: 'application/json',
                postBody: parameters,
                onComplete: function() {
                    $('delayOrAdvance-indicator').conceal();
                    $('mark-indicator').conceal();
                    psc.subject.ScheduleData.refresh()
                }
            }))
        }
  /*
        //todo - need to figure out the submect.assignments.gridId for study.
        function putScheduleNextSegment() {
            $('next-studySegment-indicator').reveal()
            var parameters = gatherDataFromScheduleStudySegment()
            var studySegmentSelector = $$('#studySegmentSelector')[0].options[$$('#studySegmentSelector')[0].selectedIndex].value //gives us ids for study, epoch, study_segment
            studySegmentSelector = studySegmentSelector.split("_")
            var studyId = studySegmentSelector[0];
            var studyIdentifierName = $$('#studySegmentSelector')[0].options[$$('#studySegmentSelector')[0].selectedIndex].text.split(":")[0] //gives us names for study, epoch, study_segment

//            TODO - need to figure out assignment part
            var href = '/psc/api/v1/studies/'+studyIdentifierName+'/schedules/${subject.assignments[0].gridId}'
            SC.asyncRequest(href, Object.extend({
                method: 'POST',
                contentType: 'text/xml',
                postBody: parameters,
                onComplete: function(){
                    $('next-studySegment-indicator').conceal()
                }
            }))
        }

        function gatherDataFromScheduleStudySegment() {
            var selectedElt = $$('#studySegmentSelector')[0].options[$$('#studySegmentSelector')[0].selectedIndex]
            var studySegmentSelectorIds = selectedElt.value //gives us ids for study, epoch, study_segment
            var studySegmentSelectorNames = selectedElt.text //gives us names for study, epoch, study_segment
            studySegmentSelectorIds = studySegmentSelectorIds.split("_")
            var epochId = studySegmentSelectorIds[1];
            var studySegmentId = studySegmentSelectorIds[2];

            studySegmentSelectorNames = studySegmentSelectorNames.split(":")
            var epochName = studySegmentSelectorNames [1]
            var studySegmentName = studySegmentSelectorNames [2]

            var immediateOrPerProtocol = $$('#mode-row input');
            for (var i= 0; i < 2; i++){
                if (immediateOrPerProtocol[i].checked) {
                    immediateOrPerProtocol = immediateOrPerProtocol[i].value
                }
            }
            immediateOrPerProtocol = immediateOrPerProtocol.replace("_", "-");
            var date = $('start-date-input').value;
            date = date.split("/")

            date = date[2] + '-'+date[0]+'-'+date[1]

            var paramsInXML = '<next-scheduled-study-segment study-segment-id="'+studySegmentId+ '" start-date="'+date+ '" mode="'+ immediateOrPerProtocol.toLowerCase() +'" start-day="5"/>'
            return paramsInXML;
        }
        */
        <c:if test="${schedulePreview}">
            Event.observe(window, 'load', function() {
                    SC.SP.generateIntialSchedulePreview('${previewResource}')
                    Event.observe('generateSchedulePreview', 'click', function(){
                        SC.SP.generateSchedulePreview('${previewResource}')
                    })
                    Event.observe('addToSelectedStudySegments', 'click', SC.SP.selectStudySegmentsForPreview)
                    Event.observe('removeSelectedStudySegments', 'click', SC.SP.removeFromSelectedStudySegments)
                })
        </c:if>

        Event.observe(window, 'load', function() {
            psc.subject.ScheduleTimeline.create();
            psc.subject.ScheduleTimeline.FocusHandler.init();
            psc.subject.ScheduleList.init();
            psc.subject.ScheduleList.FocusHandler.init();

        })

    </script>

    <tags:resigTemplate id="list_day_entry">
        <div class="day [#= dateClass #]">
            <h3 class="date">
                [#= displayDate #]
                [# if (isToday) { #]
                    <span>Today</span>
                [# } #]
            </h3>
            <div class="day-activities">
                [# if (scheduledActivities.length !== 0) { #]
                <ul>
                    [#= scheduledActivityListItems #]
                </ul>
                [# } #]
                [# /*if (hasHiddenActivities) { #]
                    <span class="hidden-activities">
                        Note: There are one or more activities on this day which
                        belong to studies or sites to which you don't have access.
                    </span>
                [# }*/ #]
            </div>
        </div>
    </tags:resigTemplate>

    <tags:resigTemplate id="list_day_sa_entry">
        <li class="[#= stateClasses() #]">
            <label>
                <input type="checkbox" value="[#= id #]" name="scheduledActivities" class="[#= stateClasses() #]"/>
                <img src="<c:url value="/images/"/>[#= current_state.name #].png" alt="Status: [#= current_state.name #]"/>
                <span title="Study" class="study [#= studyClass() #]">[#= study #]</span> /
                <span title="Segment" class="segment">[#= study_segment #]</span> /
                <a title="Activity" href="<c:url value="/pages/cal/scheduleActivity"/>?event=[#= id #]">[#= activity.name #]</a>
            </label>
        </li>
    </tags:resigTemplate>
</head>
<body>
<div id="schedule-timeline"></div>
<div id="schedule-error"></div>
<!--
<div id="schedule-help">
    <p>
        This page shows all the schedule information PSC has for whoever it is.
        TODO: more help
    </p>
</div>
-->

<div id="lower-pane">

<laf:box title="Schedule details" id="schedule-box">
    <div id="schedule">
        <div id="loading-shield" class="loading"></div>
        <div id="loading-text" class="loading">
            <tags:activityIndicator/> Loading&hellip;
        </div>
        <div id="scheduled-activities"></div>
    </div>
</laf:box>

<laf:box title="Modify schedule" id="schedule-controls-box">
    <%--TODO - move css to display.jsp, make accordion fit in the box--%>
    <div id="schedule-controls" class="myaccordion">
        <div class="accordionDiv">
            <h3><a class="accordionHeader" href="#">Legend </a></h3>
        </div>
        <div><sched:legend/> </div>
     <c:if test="${not schedulePreview}">
          <%--************ Delay Or Advance Portion**********--%>
        <div class="accordionDiv">
            <h3><a class="accordionHeader" href="#">Delay or Advance</a></h3>
        </div>

        <div style="display: none">
            <div class="accordionRow">
                <div class="label">Study: </div>
                <div class="value">
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
                </div>
                    
            </div>
            <div class="delayOrAdvanceBlock">
                <select id="delayAdvanceSelector" name="delayAdvanceSelector">
                    <option value="1" selected="true" >Delay</option>
                    <option value="-1">Advance</option>
                </select> scheduled or conditional activities by <input id="toDate" size="5" path="toDate" value="7"/> day(s) as
                of date:  <input id="currentDate" path="currentDate" size="10" value="" class="date"/>
                <a href="#" id="currentDate-calbutton">
                    <img src="<laf:imageUrl name='chrome/b-calendar.gif'/>" alt="Calendar" width="17" height="16" border="0" align="absmiddle"/>
                </a>
                Reason: <input id="reason" class="reason" path="reason" value=""/>

            </div>
            <div class="delayOrAdvanceBlock">
                <tags:activityIndicator id="delayOrAdvance-indicator"/>
                <input class="submitDelayOrAdvance" type="submit" value="Submit" onclick="executeDelayAdvancePost();"/>
            </div>
        <%--</laf:division>--%>
    </div>

        <%--*********** Mark Portion****************--%>
        <div class="accordionDiv">
        <h3><a class="accordionHeader" href="#">Mark</a></h3>
        </div>
        <div class="content">
            <markTag:markActivity/>
            <div class="delayOrAdvanceBlock">
                <tags:activityIndicator id="mark-indicator"/>
                <input class="submitDelayOrAdvance" type="submit" value="Submit" id="new-mode-submit" onclick="executeMarkPost()"/>
            </div>
        </div>
        <div class="accordionDiv">
          <h3><a class="accordionHeader" href="#">Next Segment</a></h3>
        </div>
        <div>
            <markTag:scheduleStudySegment/>
        </div>
        <div class="accordionDiv">
          <h3><a class="accordionHeader" href="#">Population</a></h3>
        </div>
        <div class="card">
            <markTag:population/>
        </div>
     </c:if>
     <c:if test="${schedulePreview}">
        <div class="accordionDiv" id="accordianHeader-5">
        <h3><a class="accordionA" href="#">Reschedule Preview</a></h3>
        </div>
        <div class="schedulePreview">
            <div class="previewRow">
                <label class="label">Start date</label>
                <input id="previewStartDate" size="10" class="date"/>
                <a href="#" id="previewStartDate-calbutton">
                    <img src="<laf:imageUrl name='chrome/b-calendar.gif'/>" alt="Calendar" width="17" height="16" border="0"/>
                </a>
            </div>
            <div class="previewRow">
                <select id="studySegmentPreviewSelector" class="studySegmentPreviewSelector">
                    <c:forEach items="${study.plannedCalendar.epochs}" var="epoch">
                        <c:forEach items="${epoch.studySegments}" var="studySegment">
                            <option value="${studySegment.gridId}_${epoch.name}:${studySegment.name}_${studySegment.lengthInDays}">${epoch.name}:${studySegment.name}</option>
                        </c:forEach>
                    </c:forEach>
                </select>
            </div>
            <div class="previewRow">
                <input id="addToSelectedStudySegments" class = "control" type="button"
                               name="addToSelectedStudySegments" value="Add" />
                 <input id="removeSelectedStudySegments" class = "control" type="button" value="Remove"/>
            </div>
            <div class="previewRow">
                <select class="selectedStudySegments" id="selectedStudySegments" size="5" style="width:100%"></select>
            </div>
            <div class="previewRow">
                <input id="generateSchedulePreview" class = "control" type="button"
                               name="generateSchedulePreview" value="Schedule Preview"/>
            </div>
        </div>
     </c:if>
</laf:box>

</div>
</body>
</html>