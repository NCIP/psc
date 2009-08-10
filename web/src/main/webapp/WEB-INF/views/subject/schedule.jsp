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
    <tags:javascriptLink name="jquery/jquery.enumerable"/>

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
            <tags:javascriptLink name="subject/real-schedule-controls"/>
            <tags:javascriptLink name="subject/real-schedule-wiring"/>
            <tags:javascriptLink name="subject/real-schedule-next-segment"/>

            <%-- TODO: there should be a subject in preview mode, too (a fake one) --%>
            <jsp:useBean id="subject" type="edu.northwestern.bioinformatics.studycalendar.domain.Subject" scope="request"/>
            <jsp:useBean id="schedule" type="edu.northwestern.bioinformatics.studycalendar.web.subject.SubjectCentricSchedule" scope="request"/>
            <script type="text/javascript">
                psc.subject.ScheduleData.uriGenerator(function () {
                    return psc.tools.Uris.relative("/api/v1/schedules/${subject.gridId}.json?"+new Date().getTime());
                });

                psc.subject.RealScheduleControls.batchResource('${collectionResource}');
            </script>
        </c:otherwise>
    </c:choose>
     <style type="text/css">
        /* TODO: use a descriptive selectors than "accordionHeader", etc. */
        #schedule-controls {
            background-color: white;
            overflow-y: auto;
            height: 297px;
            position: relative;
            font-size: 0.8em;
        }

        #schedule-legend  {
            width: 70%;
            position: static;
            right: 0;
            top: 0;
        }

        #schedule-controls .accordionDiv .accordionHeader{
            padding: 0px;
            border: 0px;
        }

        .card {
            border: 0pt;
        }

        div.row div.label{
            float:left;
            font-weight:bold;
            margin-left:0.5em;
            text-align:right;
            width:7em;
        }

        div.row div.value {
            font-weight:normal;
            margin-left:8em;
        }

        .card .value ul {
            margin: 0;
            padding: 0;
        }

        .card .value ul li {
            list-style-type: none;
            padding-bottom: 0.25em;
        }

        div.accordionRow a {
            color: #0000CC;
            display: inline !important;
        }

        td.populationTableTD {
            vertical-align:top;
        }

        table.populationTable, table.accordianTbl {
            border-spacing:0px;
            border-collapse:collapse;
            width:100%;
        }

        .accordion-content p {
            margin-top: 1.5em;
        }

        .ui-helper-reset {
            font-size: 1.0em;
        }

        a.ui-accordion-content-active {
            display: inline !important;
        }
    </style>

    <script type="text/javascript">
        jQuery(document).ready(function() {
            jQuery("#schedule-controls").accordion({ autoHeight: false, collapsible: true, navigation: true });
        });
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
        <div class="accordion-content">
            <sched:legend/>
            <div class="content" id="selected-studySegment-content">
                <a id="show_days_button" href="#?" class="control">Show days from study plan</a>
                <a id="hide_days_button" href="#?" class="control" style="display:none;">Hide days from study plan</a>
            </div>
        </div>
     <c:if test="${not schedulePreview}">
          <%--************ Delay Or Advance Portion**********--%>
        <div class="accordionDiv">
            <h3><a class="accordionHeader" href="#">Delay or advance</a></h3>
        </div>

        <div class="accordion-content" style="display: none">
            <div class="accordionRow">
                <select id="delay-or-advance">
                    <option value="1" selected="selected">Delay</option>
                    <option value="-1">Advance</option>
                </select>
                scheduled and conditional activities in
                <select id="delay-assignment">
                    <c:choose>
                        <c:when test="${not empty schedule.visibleAssignments && fn:length(schedule.visibleAssignments) gt 1}">
                            <option value="" selected="selected">all studies</option>
                            <c:forEach items="${schedule.visibleAssignments}" var="row" varStatus="rowStatus">
                                <option value="${row.gridId}">${row.name}</option>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <option value="${schedule.visibleAssignments[0].gridId}">
                                ${schedule.visibleAssignments[0].name}
                            </option>
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
                <input type="submit" value="Apply" id="delay-submit"/>
            </div>
        </div>

        <%--*********** Select and modify Portion****************--%>
        <div class="accordionDiv">
        <h3><a class="accordionHeader" href="#">Select and modify</a></h3>
        </div>
        <div class="accordion-content">
            <p>
                Within
                <select id="mark-select-assignment">
                    <c:choose>
                        <c:when test="${not empty schedule.visibleAssignments && fn:length(schedule.visibleAssignments) gt 1}">
                            <option value="" selected="selected">all studies</option>
                            <c:forEach items="${schedule.visibleAssignments}" var="row" varStatus="rowStatus">
                                <option value="${row.gridId}">${row.name}</option>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <option value="${schedule.visibleAssignments[0].gridId}">
                                ${schedule.visibleAssignments[0].name}
                            </option>
                        </c:otherwise>
                    </c:choose>
                </select>
                you can select
                <a href="#" id="mark-select-all" class="mark-select">all activities</a>,
                <a href="#" id="mark-select-none" class="mark-select">no activities</a>,
                <a href="#" id="mark-select-past-due" class="mark-select">past due activities</a>,
                <a href="#" id="mark-select-conditional" class="mark-select">conditional activities</a>, or you
                can just check things off by hand.
            </p>
            <p>
                When you have a set of activities selected, you can modify them here:
            </p>
            <p>
                <select id="mark-new-mode">
                    <option value="move-date-only">Leave the state the same</option>
                    <option value="scheduled">Mark/keep as scheduled</option>
                    <option value="occurred">Mark as occurred</option>
                    <option value="canceled-or-na">Mark as canceled or NA</option>
                    <option value="missed">Mark missed</option>
                </select>
                <label id="mark-date-group">
                    and
                    <select id="mark-delay-or-advance">
                        <option value="1" selected="selected">delay</option>
                        <option value="-1">advance</option>
                    </select>
                    by
                    <input type="text" id="mark-delay-amount" value="0" size="3"/>
                    days.
                </label>
                <label id="mark-reason-group">
                    Why? <input type="text" id="mark-reason"/>
                </label>
            </p>
            <div>
                <tags:activityIndicator id="markUpdate-indicator"/>
                <input type="submit" value="Apply" id="mark-submit"/>
                <span id="mark-activities-count"></span>
            </div>
        </div>
        <div class="accordionDiv">
          <h3><a class="accordionHeader" href="#">Next segment</a></h3>
        </div>
        <div class="accordion-content">
            <markTag:scheduleStudySegment subject="${subject}"/>
        </div>
        <div class="accordionDiv">
          <h3><a class="accordionHeader" href="#">Population</a></h3>
        </div>
        <div class="accordion-content">
            <div class="card">
                <markTag:population/>
            </div>
        </div>

        <!-- Export accordian -->
        <div class="accordionDiv">
            <h3><a class="accordionHeader" href="#">Export</a></h3>
        </div>
        <div class="accordion-content">
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
        <div id="schedule-preview-controls" class="accordion-content">
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