<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags/schedule" %>

<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="markTag" tagdir="/WEB-INF/tags/accordion" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<tags:escapedUrl var="collectionResource" value="api~v1~schedules~${subject.gridId}~batchUpdate"/>

<jsp:useBean id="schedulePreview" type="java.lang.Boolean" scope="request"/>

<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<tags:javascriptLink name="scheduled-activity-batch-days-subheader"/>

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
            <jsp:useBean id="study" type="edu.northwestern.bioinformatics.studycalendar.domain.Study" scope="request"/>
            <jsp:useBean id="amendmentIdentifier" type="java.lang.String" scope="request"/>

            <tags:sassLink name="schedule-preview"/>
            <tags:javascriptLink name="jquery/jquery.query"/>
            <tags:javascriptLink name="schedule-preview/parameters"/>
            <tags:javascriptLink name="schedule-preview/controls"/>
            <tags:javascriptLink name="schedule-preview/wiring"/>

            <tags:escapedUrl var="previewResource" value="api~v1~studies~${study.assignedIdentifier}~template~${amendmentIdentifier}~schedule-preview.json" />
            <script type="text/javascript">
                psc.subject.ScheduleData.uriGenerator(function () {
                    return "${previewResource}" + psc.schedule.preview.Parameters.toQueryString();
                });
            </script>
        </c:when>
        <c:otherwise>
            <%-- TODO: these scripts should be in the subject folder with everything else
                       unless they are shared with other pages.
                        --%>
            <tags:javascriptLink name="scheduled-activity"/>
            <tags:javascriptLink name="scheduled-activity-batch-modes"/>

            <tags:javascriptLink name="subject/real-schedule-controls"/>
            <tags:javascriptLink name="subject/real-schedule-wiring"/>
            <tags:javascriptLink name="subject/real-schedule-next-segment"/>

            <%-- TODO: there should be a subject in preview mode, too (a fake one) --%>
            <jsp:useBean id="subject" type="edu.northwestern.bioinformatics.studycalendar.domain.Subject" scope="request"/>
            <jsp:useBean id="schedule" type="edu.northwestern.bioinformatics.studycalendar.web.subject.SubjectCentricSchedule" scope="request"/>
            <script type="text/javascript">
                psc.subject.ScheduleData.uriGenerator(function () {
                    return psc.tools.Uris.relative("/api/v1/schedules/${subject.gridId}.json");
                });

                psc.subject.RealScheduleControls.batchResource('${collectionResource}');
            </script>
        </c:otherwise>
    </c:choose>
     <style type="text/css">
         /* TODO: use a descriptive selectors than "myaccordion", "accordionHeader", etc. */
        .myaccordion {
            font-size: 10pt;
            border: 1px solid #444;
            background-color: white;
            overflow-y: auto;
            height: 297px;
            position: relative;
        }

        #accordian-content {
            overflow: auto;
            overflow-x: auto;
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

        div.accordionRow a {
            color: #0000CC;
        }

        td.populationTableTD {
            vertical-align:top;
        }

         table.populationTable, table.accordianTbl {
             border-spacing:0px;
             border-collapse:collapse;
             width:100%;
         }

    </style>

    <script type="text/javascript">
        jQuery(document).ready(function() {
            jQuery(".myaccordion").accordion({ autoHeight: false, collapsible: true, navigation: true });
        });

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
                toDate = $$("#new-date-input-group input")[0].value;
                reason = $$('#new-reason-input-group input')[0].value;
            } if (newModeSelector == "markAsOccurred") {
                state = "occurred";
            } if (newModeSelector == "markAsCanceled") {
                state = "canceled";
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
                    state = checkedEvents[i].className.split(' ')[2]
                }
                var date = checkedEvents[i].up('.day').down('h3').innerHTML;
                date = date.match(/\d{4}-\d{2}-\d{2}/).toString()
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
                [# if (hasId()) { #]
                  <input type="checkbox" value="[#= id #]" name="scheduledActivities" class="event [#= stateClasses() #]  [#= assignmentClass() #]"/>
                [# } #]
                <img src="<c:url value="/images/"/>[#= current_state.name #].png" alt="Status: [#= current_state.name #]"/>
                [# if(hasAssignment()) { #]
                  <span title="Assignment" class="assignment">[#= assignment.name #]</span> /
                [# } else { #]
                  <span title="Study" class="study [#= studyClass() #]">[#= study #]</span> /
                [# } #]
                <span title="Segment" class="segment">[#= study_segment #]</span> /
                [# if (hasId()) { #]
                  <a title="Activity" href="<c:url value="/pages/cal/scheduleActivity"/>?event=[#= id #]">[#= activity.name #]</a>
                [# } else { #]
                  [#= activity.name #]
                [# } #]

                <span class="event-details" >
                    [# if (hasDetails()) { #]
                         [#= getDetails() #]
                         [# if (hasCondition()) { #]
                            [#= ';' #]
                         [# } #]

                    [# } #]
                    [# if (hasCondition()) { #]
                            [#= 'Condition:' #] [#= getCondition() #]
                            [# if (hasLabels()) { #]
                                [#= ';' #]
                            [# } #]
                    [# } #]
                    [# if (hasLabels()) { #]
                        <span class="label">
                            [#= 'Labels:' #] [#= getLabels() #]
                        </span>
                    [# } #]
                </span>

                <li class="days_from_period" id="days_from_period" style="display:none;">
                    <span class="event-details">
                        [#= getPlanDay() #]    
                    </span>
                </li>
            </label>
        </li>
    </tags:resigTemplate>

    <c:if test="${schedulePreview}">
        <tags:resigTemplate id="preview_segment_entry">
            <li id="preview-segment-[#= id #]" class="preview-segment [#= name ? 'known' : 'unknown' #]">
                <div class="segment-name">[#= name ? name : "Unknown" #]</div>
                <div class="segment-date">Starting <span class="date">[#= start_date #]</span></div>
                <div class="remove control"><input type="button" value="Remove"/></div>
            </li>
        </tags:resigTemplate>
    </c:if>
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
    <div id="loading-shield" class="loading"></div>
    <div id="loading-text" class="loading">
        <tags:activityIndicator/> Loading&hellip;
    </div>
    <div id="schedule">
        <form id="batch-form" style="font-weight:normal;">
            <div id="scheduled-activities"></div>
        </form>
    </div>
</laf:box>

<laf:box title="Modify schedule" id="schedule-controls-box">
    <%--TODO - move css to display.jsp, make accordion fit in the box--%>
    <div id="schedule-controls" class="myaccordion">
        <div class="accordionDiv">
            <h3><a class="accordionHeader" href="#">Display </a></h3>
        </div>
        <div class="accordian-content">
            <sched:legend/>
            <div class="content" id="selected-studySegment-content">
                <a id="show_days_button" href="#?" class="control">Show days from study plan</a>
                <a id="hide_days_button" href="#?" class="control" style="display:none;">Hide days from study plan</a>
            </div>
        </div>
     <c:if test="${not schedulePreview}">
          <%--************ Delay Or Advance Portion**********--%>
        <div class="accordionDiv">
            <h3><a class="accordionHeader" href="#">Delay or Advance</a></h3>
        </div>

        <div class="accordian-content" style="display: none">
            <div class="accordionRow">
                <select id="delay-or-advance">
                    <option value="1" selected="selected">Delay</option>
                    <option value="-1">Advance</option>
                </select>
                scheduled and conditional activities in
                <select id="delay-assignment">
                    <c:choose>
                        <c:when test="${not empty schedule.studies && fn:length(schedule.studies) gt 1}">
                            <option value="" selected="selected">all studies</option>
                            <c:forEach items="${schedule.visibleAssignments}" var="row" varStatus="rowStatus">
                                <option value="${row.gridId}">${row.name}</option>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <option>${schedule.studies[0].name}</option>
                        </c:otherwise>
                    </c:choose>
                </select>

                by <input id="delay-amount" size="3" value="7"/> day(s)
                as of <laf:dateInput local="true" path="delay-as-of"/>.
                <br/>
                Why? <input id="delay-reason"/>
            </div>
            <div>
                <tags:activityIndicator id="delayOrAdvance-indicator"/>
                <input type="submit" value="Update" id="delay-submit"/>
            </div>
        </div>

        <%--*********** Select and modify Portion****************--%>
        <div class="accordionDiv">
        <h3><a class="accordionHeader" href="#">Select and modify</a></h3>
        </div>
        <div class="accordian-content">
            <markTag:markActivity/>
            <div class="delayOrAdvanceBlock">
                <tags:activityIndicator id="mark-indicator"/>
                <input class="submitDelayOrAdvance" type="submit" value="Submit" id="new-mode-submit" onclick="executeMarkPost()"/>
            </div>
        </div>
        <div class="accordionDiv">
          <h3><a class="accordionHeader" href="#">Next Segment</a></h3>
        </div>
        <div class="accordian-content">
            <markTag:scheduleStudySegment subject="${subject}"/>
        </div>
        <div class="accordionDiv">
          <h3><a class="accordionHeader" href="#">Population</a></h3>
        </div>
        <div class="accordian-content">
            <div class="card">
                <markTag:population/>
            </div>
        </div>

        <!-- Export accordian -->
        <div class="accordionDiv">
            <h3><a class="accordionHeader" href="#">Export</a></h3>
        </div>
        <div class="accordian-content">
            <table class="accordianTbl">
                <c:forEach items="${subject.assignments}" var="assignment" varStatus="outterCounter">
                <tr class="<c:if test="${outterCounter.index%2 != 0}">odd</c:if> <c:if test="${outterCounter.index%2 == 0}">even</c:if>">
                <td>
                    <div class="accordionRow">
                        <div class="label">${assignment.name}</div>
                        <div class="value"><a class="control"
                                href="<c:url value="/api/v1/studies/${assignment.studySite.study.assignedIdentifier}/schedules/${assignment.gridId}.ics"/>"
                                id="export-ics-calendar" title="Export as ICS for iCal, Outlook and other calendar applications">Export ICS</a>
                        </div>
                    </div>
                </td>
                </tr>
                </c:forEach>
           </table>
        </div>
         
     </c:if>
     <c:if test="${schedulePreview}">
        <div class="accordionDiv">
        <h3><a class="accordionA" href="#">Preview study segments</a></h3>
        </div>
        <div id="schedule-preview-controls" class="accordian-content">
            <ul id="preview-segments">
                <li id="next-segment">
                    <h4>Preview another segment?</h4>
                    <div class="segment-name">
                        <select id="preview-segment-selector">
                            <c:forEach items="${study.plannedCalendar.epochs}" var="epoch">
                                <c:forEach items="${epoch.studySegments}" var="studySegment">
                                    <option value="${studySegment.gridId}">${studySegment.qualifiedName}</option>
                                </c:forEach>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="segment-date">
                        Starting <laf:dateInput path="next-segment-date" local="true"/>
                    </div>
                    <div class="add control"><input type="button" value="Add" id="add-button"/></div>
                </li>
            </ul>

            <div id="refresh-preview-control">
                <span class="notice">When you're done making changes&hellip;</span>
                <input id="refresh-preview" type="button" value="Refresh preview"/>
            </div>
        </div>
     </c:if>
</laf:box>

</div>
</body>
</html>