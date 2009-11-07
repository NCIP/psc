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
            <jsp:useBean id="schedule" type="edu.northwestern.bioinformatics.studycalendar.web.subject.SubjectCentricSchedule" scope="request"/>
            <script type="text/javascript">
                psc.subject.ScheduleData.uriGenerator(function () {
                    return psc.tools.Uris.relative("/api/v1/subjects/${subject.gridId}/schedules.json?"+new Date().getTime());
                });

                psc.subject.RealScheduleControls.batchResource('${collectionResource}');
            </script>
            <c:set var="isNotificationAvailable" value="false"/>
            <c:forEach items="${subject.assignments}" var="assignment" varStatus="outerCounter">
                <c:if test="${not empty assignment.currentAeNotifications}">
                    <c:set var="isNotificationAvailable" value="true"/>
                </c:if>
           </c:forEach>
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
    </style>

    <script type="text/javascript">
        jQuery(document).ready(function() {
            jQuery("#schedule-controls").accordion({ autoHeight: false, collapsible: true, navigation: true, active: false });
            <c:choose>
                <c:when test="${isNotificationAvailable == true}">
                   jQuery("#schedule-controls").accordion('activate', 1)
                </c:when>
                <c:otherwise>
                   jQuery("#schedule-controls").accordion('activate', 0)
               </c:otherwise>
            </c:choose>
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
                [# if (hasHiddenActivities) { #]
                    <span class="hidden-activities">
                        Note: There are one or more activities on this day which
                        belong to studies or sites to which you don't have access.
                    </span>
                [# } #]
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
            <div id="display-controls">
                <a href="#" id="toggle-plan-days" class="control">Show days from study plan</a>
            </div>
        </div>
     <c:if test="${not schedulePreview}">
        <div class="accordionDiv">
            <h3><a class="accordionHeader" href="#">Notifications </a></h3>
        </div>
        <div class="accordion-content">
           <span id="notification-message"></span>
           <c:forEach items="${subject.assignments}" var="assignment" varStatus="outerCounter">
                <c:if test="${not empty assignment.currentAeNotifications}">
                   <div class="row ${commons:parity(outerCounter.index)}" id="div-${assignment.name}" >
                        <div class="label">${assignment.name}</div>
                        <div class="value">
                            <ul>
                            <c:forEach items="${assignment.currentAeNotifications}" var="notification" varStatus="innerCounter">
                                <li id="notifiction-${notification.gridId}" class="notification-list ${assignment.name} remove ${commons:parity(innerCounter.index)}" study="${assignment.name}">
                                    <c:set var="message" value="${notification.message}"/>
                                    <c:choose>
                                        <c:when test="${fn:contains(message,'pages/cal')}">
                                            <a href="<c:url value="${notification.message}"/>">${notification.title}</a>
                                        </c:when>
                                        <c:when test="${fn:contains(message, 'optional amendment')}">
                                            <label>${notification.message} using amendment section</label>
                                        </c:when>
                                        <c:otherwise>
                                            <label>${notification.title}</label>
                                        </c:otherwise>
                                    </c:choose>
                                    <a href="#" class="notification-control control" title="This will permanently clear this notification from the screen"
                                       notification="${notification.gridId}" assignment="${assignment.gridId}" subject="${subject.gridId}">Dismiss</a>
                                </li>
                            </c:forEach>
                            </ul>
                        </div>
                    </div>
                </c:if>
           </c:forEach>
           <c:if test="${isNotificationAvailable == false}" >
               <div class="label">No current notifications available.</div>
           </c:if>
        </div>

        <div class="accordionDiv">
            <h3><a class="accordionHeader" href="#">Ammendments </a></h3>
        </div>
        <div class="accordion-content">
            <c:forEach items="${subject.assignments}" var="assignment" varStatus="outerCounter">
                <div class="row ${commons:parity(outerCounter.index)}" id="div-${assignment.name}" >
                    <div class="label">${assignment.name}</div>
                    <div class="value">
                        ${assignment.currentAmendment.displayName}
                        <c:if test="${not (assignment.currentAmendment eq assignment.studySite.currentApprovedAmendment)}">
                            <a class="control"
                               href="<c:url value="/pages/cal/schedule/amend?assignment=${assignment.id}"/>">Apply</a>
                        </c:if>
                    </div>
                </div>
            </c:forEach>
        </div>
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
            <markTag:population/>
        </div>

        <!-- Export accordion -->
        <div class="accordionDiv">
            <h3><a class="accordionHeader" href="#">Export</a></h3>
        </div>
        <div class="accordion-content" id="export-controls">
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
            <c:forEach items="${subject.assignments}" var="assignment" varStatus="outerCounter">
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
                </div>
            </c:forEach>
        </div>

        <div class="accordionDiv">
            <h3><a class="accordionHeader" href="#">Interapplication links</a> </h3>
        </div>
        <div class="accordion-content">
            <c:forEach items="${subject.assignments}" var="assignment" varStatus="outerCounter">

                <c:if test="${configuration.externalAppsConfigured}">
                    <div class="row ${commons:parity(outerCounter.index)}">
                        <c:set var="caaersAvail" value="${not empty configuration.map.caAERSBaseUrl}"/>
                        <c:set var="labViewerAvail" value="${not empty configuration.map.labViewerBaseUrl}"/>
                        <c:set var="ctmsAvail" value="${not empty configuration.map.patientPageUrl}"/>
                        <c:set var="subjectAssignment" value="${assignment}"/>
                        <div class="label">
                            <c:if test="${caaersAvail || labViewerAvail || ctmsAvail}">
                                View this subject's
                            </c:if>
                        </div>
                        <div class="value">
                            <c:if test="${ctmsAvail}">
                                <tags:externalLink appShortName="ctms" subjectAssigmnent="${subjectAssignment}" urlTemplateProperty="patientPageUrl"
                                                   cssClass="control">${configuration.map.ctmsName} record</tags:externalLink>
                            </c:if>
                            <c:if test="${caaersAvail}">
                                <tags:externalLink appShortName="caaers"  subjectAssigmnent="${subjectAssignment}" urlTemplateProperty="caAERSBaseUrl"
                                                   cssClass="control">adverse events</tags:externalLink>
                            </c:if>
                            <c:if test="${labViewerAvail}">
                                <tags:externalLink appShortName="labviewer" subjectAssigmnent="${subjectAssignment}" urlTemplateProperty="labViewerBaseUrl"
                                                   cssClass="control">lab results</tags:externalLink>
                            </c:if>
                        </div>
                    </div>
                </c:if>
            </c:forEach>
            <c:if test="${! configuration.externalAppsConfigured}">
                <li class="none">None configured</li>
             </c:if>  

        </div>

        <div class="accordionDiv">
            <h3><a class="accordionHeader" href="#">Template links</a> </h3>
        </div>
        <div class="accordion-content">
            <c:if test="${not empty schedule.visibleAssignments && fn:length(schedule.visibleAssignments) gt 0}">
                Return to template :
                <c:forEach items="${schedule.visibleAssignments}" var="row" varStatus="rowStatus">
                    <a class="control" href="<c:url value="/pages/cal/template?study=${row.studySite.study.id}"/>">
                        ${row.name}
                    </a>
                </c:forEach>
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