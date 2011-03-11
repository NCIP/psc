<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags/schedule" %>

<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="markTag" tagdir="/WEB-INF/tags/accordion" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<tags:escapedUrl var="collectionResource" value="api~v1~subjects~${subject.gridId}~schedules~activities"/>

<jsp:useBean id="schedulePreview" type="java.lang.Boolean" scope="request"/>
<jsp:useBean id="configuration"
             type="edu.northwestern.bioinformatics.studycalendar.configuration.Configuration"
             scope="request"/>

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

    <tags:javascriptLink name="jquery/jquery-ui-1.7.3.custom.min"/>
    <tags:javascriptLink name="jquery/jquery.enumerable"/>
    <tags:javascriptLink name="jquery/jquery.cookie"/>

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
            <tags:icsInstructions/>

            <%-- TODO: there should be a subject in preview mode, too (a fake one) --%>
            <jsp:useBean id="subject" type="edu.northwestern.bioinformatics.studycalendar.domain.Subject" scope="request"/>
            <jsp:useBean id="schedule" type="edu.northwestern.bioinformatics.studycalendar.web.subject.MultipleAssignmentScheduleView" scope="request"/>
            <jsp:useBean id="currentUser" type="edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser" scope="request"/>
            <c:set var="scheduleResource" value="/api/v1/subjects/${subject.gridId}/schedules" />

            <script type="text/javascript">
                psc.subject.ScheduleData.uriGenerator(function () {
                    return psc.tools.Uris.relative('${scheduleResource}'+".json?"+new Date().getTime());
                });
                psc.subject.ScheduleData.contextAPI(function () {
                    return psc.tools.Uris.deployed('${scheduleResource}');
                });
                psc.subject.ScheduleData.undoableActionsURI(function () {
                    return psc.tools.Uris.relative('${scheduleResource}'+"/undoable-actions.json?"+new Date().getTime());
                });
                psc.subject.RealScheduleControls.batchResource('${collectionResource}');
                psc.subject.ScheduleData.setSubjectCoordinator('${currentUser.username}');
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

        #schedule-controls .accordionDiv .accordionHeader {
            padding: 0;
            border: 0;
        }

        .card {
            border: 0;
        }

        #schedule-controls div.row div.label {
            width: 10em;
        }

        #export-controls div.row {
            margin:0;
            padding:0;
        }

        #schedule-controls div.row div.value {
            margin-left: 11em;
        }

        #schedule-controls .none {
            font-style: italic;
        }

        .value ul {
            margin: 0;
            padding: 0;
        }

        .value ul li {
            list-style-type: none;
            padding-bottom: 0.25em;
        }

        div.accordionRow a {
            color: #0000CC;
            display: inline !important;
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

        #mark-reason-group {
            white-space: nowrap
        }
        
        #display-controls {
            margin-top: 1em
        }
        a.notification-control {
            padding:0 2px;
        }

        div.populationChange {
            float:right;
        }

        .legend-row {
            list-style-type:none;
        }
    </style>

    <script type="text/javascript">
        jQuery(document).ready(function() {
            jQuery("#schedule-controls").accordion({ autoHeight: false, collapsible: true, navigation: true, active: false });
        });
    </script>

    <tags:resigTemplate id="list_day_entry">
        <div class="day [#= dateClass #]">
            <h3 class="date">
                [#= displayDate #]
                [# if (isToday) { #]
                    <div class="today-label">Today</div>
                [# } #]
            </h3>
            <div class="day-activities">
                [# if (scheduledActivities.length !== 0) { #]
                <ul>
                    [#= scheduledActivityListItems #]
                </ul>
                [# } #]
                [# if (hasHiddenActivities == "true") { #]
                    <span class="hidden-activities">
                        Note: There are one or more activities on this day which
                        belong to studies or sites to which you don't have access.
                    </span>
                [# } #]
            </div>
        </div>
    </tags:resigTemplate>

    <tags:resigTemplate id="list_day_sa_entry">
        <li class="[#= stateClasses() #] [#= mineClass() #] ">
            <label>
                [# if (hasId() && canUpdateSchedule()) { #]
                  <input type="checkbox" value="[#= id #]" name="scheduledActivities" class="event [#= stateClasses() #]  [#= assignmentClass() #]"/>
                [# } #]
                <img src="<c:url value="/images/"/>[#= current_state.name #].png" alt="Status: [#= current_state.name #]"/>
                [# if(hasAssignment()) { #]
                  <span title="Assignment" class="assignment">[#= assignment.name #] </span> /
                [# } else { #]
                  <span title="Study" class="study [#= studyClass() #]">[#= study #]</span> /
                [# } #]
                [# if(hasStudySubjectId()) { #]
                  <span title="Study Subject Id" class="studySubjectId"> [#= study_subject_id #] / </span>
                [# } #]
                <span title="Segment" class="segment">[#= study_segment #]</span> /
            </label>

            [# if (hasId()) { #]
              <a title="Activity" href="<c:url value="/pages/cal/scheduleActivity"/>?event=[#= id #]">[#= activity.name #]</a>
            [# } else { #]
              [#= activity.name #]
            [# } #]

            <span class="event-details plan-notes" >
                [#= planNotes() #]
            </span>

            <span class="event-details plan-day">
                [#= formatted_plan_day #]
            </span>
        </li>
    </tags:resigTemplate>

    <tags:resigTemplate id="notifications_entry">
        [# if (hasNotifications) { #]
        <div class="assignment notification row" >
            <div class="label">[#= name #]</div>
            <div class="value">
                <ul>
                    [#= notificationListItems #]
                </ul>
            </div>
        </div>
       [# } #]
    </tags:resigTemplate>

    <tags:resigTemplate id="list_notification_entry">
        <li class="notification [#= notificationClass() #]">
            [# if (hasMessageWithLink()) { #]
                <a href="<c:url value="[#= displayMessage() #]"/>">[#= title #]</a>
            [# } else { #]
                <label>[#= displayMessage() #]</label>
            [# } #]

            [# if (canUpdateSchedule()) { #]
                <a href="#" class="notification-control control" link="[#= href #]"
                       title="This will permanently clear this notification from the screen">Dismiss</a>
            [# } #]
        </li>
    </tags:resigTemplate>

    <tags:resigTemplate id="populations_entry">
        <div class="assignment population row [#= counterClass #]" >
            <div class="label">[#= name #]</div>
            <div class="value">
                [# if (hasPopulations) { #]
                    <ul>
                        [#= populationsListItems #]
                    </ul>
                [# }  else { #]
                    <div class="none">None</div>
                [# } #]
                [# if (canUpdateSchedule) { #]
                    <div class="populationChange">
                        <a class="control"
                           href="<c:url value="/pages/cal/schedule/populations"/>?assignment=[#= id #]">Change</a>
                    </div>
                    <br style="clear:both"/>
                [# } #]
            </div>
        </div>
    </tags:resigTemplate>

    <tags:resigTemplate id="population_entry">
        <li class="population">
            <label>[#= name #]</label>
        </li>
    </tags:resigTemplate>

    <tags:resigTemplate id="amendments_entry">
        <div class="assignment amendment row [#= counterClass #]" >
            <div class="label">[#= name #]</div>
            <div class="value">
                [#= currentAmendment #]
                [# if (canUpdateSchedule && hasUnappliedAmendment) { #]
                    <a class="control"
                           href="<c:url value="/pages/cal/schedule/amend"/>?assignment=[#= id #]">Apply</a>
                [# } #]
            </div>
            <br style="clear:both"/>
        </div>
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
        <div class="accordionDiv" id="display-header">
            <h3><a class="accordionHeader" href="#">Display </a></h3>
        </div>
        <div class="accordion-content" id="display-content">
            <sched:legend/>
            <div id="display-controls">
                <a href="#" id="toggle-plan-days" class="control">Show days from study plan</a>
            </div>
        </div>
     <c:if test="${not schedulePreview}">
        <div class="accordionDiv" id="notification-header">
            <h3><a class="accordionHeader" href="#">Notifications </a></h3>
        </div>
        <div class="accordion-content" id="notification-content">
            <div id="pending-notifications"></div>
        </div>

        <div class="accordionDiv">
            <h3><a class="accordionHeader" href="#">Amendments </a></h3>
        </div>
        <div class="accordion-content">
            <div id="assignment-amendments"></div>
        </div>
        <c:if test="${canUpdateSchedule}">
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
                    as of <laf:dateInput local="true" path="delay-as-of" todayDateValue="${todayDate}"/>.
                    <br/>
                    Why? <input id="delay-reason"/>
                </div>
                <div>
                    <tags:activityIndicator id="delayOrAdvance-indicator"/>
                    <input type="submit" value="Apply" id="delay-submit"/>
                </div>
            </div>

            <%--*********** Select and modify Portion****************--%>
            <div class="accordionDiv" id="mark-select-header">
            <h3><a class="accordionHeader" href="#">Select and modify</a></h3>
            </div>
            <div id="mark-select-content" class="accordion-content">
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
                    select
                    <a href="#" id="mark-select-all" class="mark-select">all activities</a>,
                    <a href="#" id="mark-select-none" class="mark-select">no activities</a>,
                    <a href="#" id="mark-select-past-due" class="mark-select">past due activities</a>,
                    <a href="#" id="mark-select-conditional" class="mark-select">conditional activities</a>,
                    or just check things off by hand.
                </p>
                <p>
                    When there is a set of activities selected, modify them here:
                </p>
                <p>
                    <select id="mark-new-mode">
                        <option value="move-date-only">Leave the state the same</option>
                        <option value="scheduled">Mark/keep as scheduled</option>
                        <option value="occurred">Mark as occurred</option>
                        <option value="canceled-or-na">Mark as canceled or NA</option>
                        <option value="missed">Mark missed</option>
                    </select>
                    <span id="mark-date-group">
                        <label>
                            and
                            <select id="mark-delay-or-advance">
                                <option value="1" selected="selected">delay</option>
                                <option value="-1">advance</option>
                            </select>
                        </label>
                        <label>
                            by
                            <input type="text" id="mark-delay-amount" value="0" size="3"/>
                            days.
                        </label>
                    </span>
                    <label id="mark-reason-group">
                        Why? <input type="text" id="mark-reason"/>
                    </label>
                </p>
                <div id="apply-modified-activities-div">
                    <tags:activityIndicator id="markUpdate-indicator"/>
                    <input type="submit" value="Apply" id="mark-submit"/>
                    <span id="mark-activities-count"></span>
                </div>
            </div>
            <div class="accordionDiv">
              <h3><a class="accordionHeader" href="#">Next segment</a></h3>
            </div>
            <div class="accordion-content">
                <markTag:scheduleStudySegment/>
            </div>
        </c:if>
        <div class="accordionDiv">
          <h3><a class="accordionHeader" href="#">Population</a></h3>
        </div>
        <div class="accordion-content">
            <div id="assignment-populations"></div>
        </div>

        <!-- Export accordion -->
        <div class="accordionDiv">
            <h3><a class="accordionHeader" href="#">Export</a></h3>
        </div>
        <div class="accordion-content" id="export-controls">
            <div class="label" style="font-weight: bold;">ICS:</div>
            <c:if test="${not empty schedule.visibleAssignments && fn:length(schedule.visibleAssignments) gt 1}">
                <div class="row even">
                    <div class="label">All</div>
                    <div class="value">
                        <a class="control ics-subscribe"
                            href="<c:url value="/api/v1/subjects/${subject.gridId}/schedules.ics"/>"
                            title="Subscribe as ICS for iCal, Outlook and other calendar applications">Subscribe</a>
                        <a class="control"
                            href="<c:url value="/api/v1/subjects/${subject.gridId}/schedules.ics"/>"
                            title="Download as ICS for one-time import into iCal, Outlook and other calendar applications">Export ICS</a>
                    </div>
                </div>
                <br style="clear:both"/>
            </c:if>
            <c:forEach items="${schedule.visibleAssignments}" var="assignment" varStatus="outerCounter">
                <div class="row ${commons:parity(outerCounter.count)}">
                    <div class="label">${assignment.name}</div>
                    <div class="value">
                        <a class="control ics-subscribe"
                            href="<c:url value="/api/v1/studies/${assignment.studySite.study.assignedIdentifier}/schedules/${assignment.gridId}.ics"/>"
                            title="Subscribe as ICS for iCal, Outlook and other calendar applications">Subscribe</a>
                        <a class="control"
                           href="<c:url value="/api/v1/studies/${assignment.studySite.study.assignedIdentifier}/schedules/${assignment.gridId}.ics"/>"
                           title="Download as ICS for one-time import into iCal, Outlook and other calendar applications">Export ICS</a>
                    </div>
                    <br style="clear:both"/>
                </div>
            </c:forEach>
            <div class="label" style="font-weight: bold;">Report:</div>
            <div class="row">
                <div class="label"> Export activities scheduled from  </div>
                <div class="value">
                    <input id="actual-date-start" class="date" type="text" size="10"/>
                    <a href="#" id="actual-date-start-calbutton">
                        <img src="<laf:imageUrl name='chrome/b-calendar.gif'/>" alt="Calendar" width="17" height="16" border="0" align="absmiddle"/>
                    </a>
                    to:
                    <input id="actual-date-stop" class="date" type="text" size="10"/>
                    <a href="#" id="actual-date-stop-calbutton">
                        <img src="<laf:imageUrl name='chrome/b-calendar.gif'/>" alt="Calendar" width="17" height="16" border="0" align="absmiddle"/>
                    </a>
               </div>
            </div>
            <c:choose>
                <c:when test="${not empty subject.personId}">
                    <c:set var="subjectId" value="${subject.personId}"/>
                </c:when>
                <c:otherwise>
                    <c:set var="subjectId" value="${subject.gridId}"/>
                </c:otherwise>
            </c:choose>
            <div class="row">
                <div class="value" >
                    <a class="control" id="xls-report" href="#" title="Download scheduled activities report into Excel format" extension=".xls"
                       subject="${subjectId}">Excel</a>
                    <a class="control" id="csv-report" href="#" title="Download scheduled activities report into CSV format" extension=".csv"
                       subject="${subjectId}">CSV</a>
                </div>
            </div>
            <div class="row">
                <div class="value" >
                    <a style="float:left;" id="report-options" href="#" subject="${subjectId}">Show more options</a>
                </div>
            </div>
        </div>

        <div class="accordionDiv">
            <h3><a class="accordionHeader" href="#">Links</a> </h3>
        </div>
        <div class="accordion-content">
            <div class="label" style="font-weight: bold;">Interapplication:</div>
            <c:if test="${configuration.externalAppsConfigured}">
                <c:set var="caaersAvail" value="${not empty configuration.map.caAERSBaseUrl}"/>
                <c:set var="labViewerAvail" value="${not empty configuration.map.labViewerBaseUrl}"/>
                <c:set var="ctmsAvail" value="${not empty configuration.map.patientPageUrl}"/>
                <c:forEach items="${schedule.visibleAssignments}" var="subjectAssignment" varStatus="outerCounter">
                    <div class="row ${commons:parity(outerCounter.index)}">
                        <div class="label">
                            ${subjectAssignment.name}
                        </div>
                        <div class="value">
                            <sched:withStudySubjectAssignment value="${subjectAssignment}">
                                <c:if test="${ctmsAvail}">
                                    <tags:externalLink appShortName="ctms" urlTemplateProperty="patientPageUrl"
                                                       cssClass="control">${configuration.map.ctmsName} record</tags:externalLink>
                                </c:if>
                                <c:if test="${caaersAvail}">
                                    <tags:externalLink appShortName="caaers" urlTemplateProperty="caAERSBaseUrl"
                                                       cssClass="control">adverse events</tags:externalLink>
                                </c:if>
                                <c:if test="${labViewerAvail}">
                                    <tags:externalLink appShortName="labviewer" urlTemplateProperty="labViewerBaseUrl"
                                                       cssClass="control">lab results</tags:externalLink>
                                </c:if>
                            </sched:withStudySubjectAssignment>
                        </div>
                    </div>
                </c:forEach>
            </c:if>
            <c:if test="${!configuration.externalAppsConfigured}">
                <div class="row">
                    <div class="value none">None configured</div>
                </div>
            </c:if>
            <div class="label" style="font-weight: bold;">Template:</div>
            <c:if test="${not empty schedule.visibleAssignments && fn:length(schedule.visibleAssignments) gt 0}">
                <div class="row">
                    <div class="label">Return to :</div>
                    <div class="value">
                        <c:forEach items="${schedule.visibleAssignments}" var="row" varStatus="rowStatus">
                            <a class="control" href="<c:url value="/pages/cal/template?study=${row.studySite.study.id}"/>">
                            ${row.name} </a>
                        </c:forEach>
                   </div>
                 </div>
            </c:if>
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