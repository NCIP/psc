
<%@page pageEncoding="utf8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags/schedule"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<html>
<head>
    <title>Subject Schedule for ${subject.fullName} on ${plannedCalendar.name}</title>
    <tags:includeScriptaculous/>
    <tags:stylesheetLink name="main"/>
    <tags:javascriptLink name="main"/>
    <tags:javascriptLink name="scheduled-activity"/>
    <tags:javascriptLink name="scheduled-activity-batch"/>
    <style type="text/css">
        .epochs-and-studySegments, #next-studySegment-form {
            margin: 1em;
        }

        .epochs-and-studySegments {
            margin-top: 0;
            width: 75%;
        }

        #epochs-indicator {
            display: none;
        }

        .collapse-icon {
            font-size: 0.6em;
        }

        .collapsible h2 {
            cursor: pointer;
        }

        #next-studySegment-form {
            margin-top: 0;
            float: right;
            width: 20%;
            padding: 4px;
            border: 1px solid #666;
        }

        #next-studySegment-form .row .label {
            width: 35%;
            color: #666;
        }

        #next-studySegment-form .row .value {
            margin-left: 40%;
        }

        #mode-row label {
            display: block;
        }

        #batch-reschedule form {
            font-size: 0.8em;
            margin: 1em;
        }

        #scheduled-studySegments ul {
            white-space: nowrap;
            padding: 0;
            margin: 0.5em 0;
        }

        #scheduled-studySegments ul li {
            display: inline;
            padding: 0.2em 0.5em;
            margin: 0 0.3em;
            border: 1px solid #aaa;
            white-space: nowrap;
        }

        #scheduled-studySegments #scheduled-studySegments-indicator-item {
            border-color: white;
        }

        #scheduled-studySegments a {
            text-decoration: none;
            color: black;
        }

        #scheduled-studySegments ul li.selected {
            background-color: #999 !important;
        }

        #scheduled-studySegments ul li.selected a {
            color: white;
        }

        #scheduled-studySegments ul li:hover {
            background-color: #ccc;
        }

        #selected-studySegment .content {
            position: relative;
        }

        /* For IE */
        * html #selected-studySegment .content {
            height: 0;
        }

        #selected-studySegment .legend {
            position: absolute;
            right: 1em;
            top: 1em;
            width: 20%;
            font-size: 11pt;
            border: 1px solid #444;
            background-color: white;
            z-index: 50;
            padding: 0;
            margin: 0;
        }

        #selected-studySegment .legend h3 {
            background: #444;
            color: #fff;
            padding: 4px;
            margin: 0;
            border-width: 0;
        }

        #selected-studySegment .legend ul {
            margin: 0; padding: 0;
        }

        .legend li {
            display: block;
            padding: 3px;
            border-top: 1px solid #444;
        }

        li.scheduled a {
            font-weight: bold;
            color: #00C;
        }
        li.canceled a {
            font-style: italic;
            color: #444;
        }
        li.occurred a {
            font-weight: normal;
            color: #006;
        }
        li.conditional a {
            font-weight: bold;
            color: darkslategray;
        }
        li.NA a {
            font-weight: normal;
            color: steelblue;
        }

        li img {
            vertical-align:middle;            
        }

        .ae a.dismiss-control {
            display: block;
            float: right;
            padding: 6px;
            color: #ccc;
            background-color: #444;
        }
        .ae.section {
            border-color: #600;
        }
        .ae h2 {
            background-color: #911;
        }
        .ae h3 {
            background-color: #c99;
            padding: 5px 1em;
        }

        .topBordered {
            border-top:1px solid #000000;
            padding-top:30px;
        }

        #external-apps {
            color: #666;
            padding: 8px;
        }

        #schedule-switch, #outside-links {
            margin: 1em;
            text-align: right
        }

        #schedule-controls{
            margin-right: 1em;
            text-align: right;
        }

        #schedule-controls li {
            display: inline
        }

        .batch-schedule-link {
            color:#0000cc;
            cursor:pointer;
            white-space:nowrap;
        }

        .box {
            clear: both;
        }

        .card .value ul {
            margin: 0;
            padding: 0;
        }

        .card .value ul li {
            list-style-type: none;
            padding-bottom: 0.25em;
        }
        .alignStudySegmentButtonInTheMiddle {
            text-align:center;
        }

    </style>
    <script type="text/javascript">
        var DEFAULT_DATES = {
            IMMEDIATE: '<tags:formatDate value="${dates['IMMEDIATE']}"/>',
            PER_PROTOCOL: '<tags:formatDate value="${dates['PER_PROTOCOL']}"/>'
        }

        function registerSelectStudySegmentHandlers() {
            $$('#scheduled-studySegments a').each(registerSelectStudySegmentHandler)
        }

        function registerSelectStudySegmentHandler(a) {
            var aElement = $(a)
            Event.observe(aElement, "click", function(e) {
                Event.stop(e)
                selectStudySegment(aElement.href)
            })
        }

        function selectStudySegment(href) {
            $("scheduled-studySegments-indicator").reveal();
            SC.slideAndHide('selected-studySegment-content', { afterFinish: function() {
                // deselect current
                var sel = $$("#scheduled-studySegments li.selected")
                if (sel && sel.length > 0) Element.removeClassName(sel[0], "selected")

                new Ajax.Request(href, { asynchronous: true,
                    onComplete: function() {
                        $("scheduled-studySegments-indicator").conceal()
                    },
                    onFailure: function() {
                        Element.update('selected-studySegment-content', "Loading failed")
                        Element.update('selected-studySegment-header', "Error")
                        SC.slideAndShow('selected-studySegment-content')
                    }
                });
            } });
        }

        function registerSelectNextStudySegmentHandlers() {
            $$(".epochs-and-studySegments a.studySegment").each(function(a) {
                Event.observe(a, "click", function(e) {
                    Event.stop(e)
                    $('next-studySegment-id').value = a.id.substring('studySegment'.length +1)
                    Element.update('next-studySegment-name', a.title)
                    SC.highlight('next-studySegment-name', { restorecolor: "#ffffff" })
                    $('next-studySegment-button').disabled = false
                })
            })

            Event.observe('next-studySegment-form', "submit", function(e) {
                $('next-studySegment-indicator').reveal()
                Event.stop(e)
                SC.asyncSubmit('next-studySegment-form', {
                    onComplete: function() {
                        $('next-studySegment-indicator').conceal()
                    }
                })
            })
        }

        function registerDefaultDateSetterHandlers() {
            $$('.mode-radio').each(function(radio) {
                Event.observe(radio, "click", function() {
                    $('start-date-input').value = DEFAULT_DATES[radio.value];
                })
            })
        }

        function registerDismissControl() {
            $$(".ae .dismiss-control").each(function(control) {
                Event.observe(control, "click", function(event) {
                    Event.stop(event)
                    var confirmMessage = "This will permanently clear this notification from this screen.  It will not affect any other record of the adverse event."
                    if (window.confirm(confirmMessage)) {
                        SC.doAsyncLink(control)
                    }
                })
            })
        }

        function ajaxform() {
            var href = '<c:url value="/pages/cal/schedule/rescheduleArms"/>'
            // Set up data variable
            var formdata = "";
            formdata = formdata + 'toDate' + "=" + $('toDate').value+"&";
            formdata = formdata + 'reason' + "=" + $(document.getElementById('reason')).value + "&";
            formdata = formdata + 'scheduledCalendar' + "=" + ${studySegment.scheduledCalendar.id} + "&";
            formdata = formdata + 'currentDate' + "=" + $(document.getElementById('currentDate')).value + "&";
            var lastRequest = new Ajax.Request(href,
            {
                postBody: formdata
            });
            return true;
        }

        function registerFunctions() {
            registerSelectStudySegmentHandlers();
            registerSelectNextStudySegmentHandlers();
            registerDefaultDateSetterHandlers();
            registerHeaderCollapse();
            registerDismissControl();
        }

//        Event.observe(window, "load", registerSelectStudySegmentHandlers);
//        Event.observe(window, "load", registerSelectNextStudySegmentHandlers);
//        Event.observe(window, "load", registerDefaultDateSetterHandlers);
//        Event.observe(window, "load", registerHeaderCollapse);
//        Event.observe(window, "load", registerDismissControl);
        Event.observe(window, "load", registerFunctions);
    
    </script>
</head>
<body>
<div class="card title-card">
    <div class="header">Schedule</div>
    <h1>${subject.fullName}</h1>
    <div class="row odd">
        <div class="label">Study</div>
        <div class="value">${plannedCalendar.name}</div>
    </div>
    <div class="row even">
        <div class="label">Site</div>
        <div class="value">${assignment.studySite.site.name}</div>
    </div>
    <div class="row odd">
        <div class="label">Current amendment</div>
        <div class="value">
            ${assignment.currentAmendment.displayName}
            <c:if test="${not onLatestAmendment}">
                <a class="control" href="<c:url value="/pages/cal/schedule/amend?assignment=${assignment.id}"/>">Change</a>
            </c:if>
        </div>
    </div>
    <c:if test="${not empty study.populations}">
        <div class="row even">
            <div class="label">Populations</div>
            <div class="value">
                <ul>
                    <c:if test="${empty assignment.populations}"><em>None</em></c:if>
                    <c:forEach items="${assignment.populations}" var="pop">
                        <li>${pop.name}</li>
                    </c:forEach>
                    <li><a class="control" href="<c:url value="/pages/cal/schedule/populations?assignment=${assignment.id}"/>">Change</a></li>
                </ul>
            </div>
        </div>
    </c:if>
</div>

<div class="controls-card card">
    <div class="header">Manipulate and view schedule</div>
    <ul id="schedule-controls">
        <c:if test="${assignment.endDateEpoch == null}">
            <li><a class="control" href="<c:url value="/pages/cal/takeSubjectOffStudy?assignment=${assignment.id}"/>">Take subject off study</a></li>
        </c:if>
        <li><a class="control" href="<c:url value="/pages/cal/schedule/display/${assignment.gridId}.ics"/>" id="export-ics-calendar" title="Export as ICS for iCal, Outlook and other calendar applications">Export ICS</a></li>
    </ul>
    <div id="schedule-switch">
        <span class="schedule-switch-control">
            <c:if test="${not empty onStudyAssignments}">
                View schedule for current subject
                <select id="assigned-subject-selector">
                    <c:forEach items="${onStudyAssignments}" var="a">
                        <option value="${a.scheduledCalendar.id}" <c:if test="${a == assignment}">selected="selected"</c:if>>${a.subject.lastFirst}</option>
                    </c:forEach>
                </select>
                <a class="control" href="<c:url value="/pages/cal/schedule"/>" id="go-to-schedule-control">Go</a>
            </c:if>
        </span>
        <span class="schedule-switch-control">
            <c:if test="${not empty offStudyAssignments}">
                View schedule for historical subject
                <select id="offstudy-assigned-subject-selector">
                    <c:forEach items="${offStudyAssignments}" var="a">
                        <option value="${a.scheduledCalendar.id}" <c:if test="${a == assignment}">selected="selected"</c:if>>${a.subject.lastFirst}</option>
                    </c:forEach>
                </select>
                <a class="control" href="<c:url value="/pages/cal/schedule"/>" id="offstudy-go-to-schedule-control">Go</a>
            </c:if>
        </span>
    </div>

    <div id="outside-links">
        <c:if test="${configuration.externalAppsConfigured}">
            <c:set var="caaersAvail" value="${not empty configuration.map.caAERSBaseUrl}"/>
            <c:set var="labViewerAvail" value="${not empty configuration.map.labViewerBaseUrl}"/>
            <c:set var="ctmsAvail" value="${not empty configuration.map.patientPageUrl}"/>
            <c:if test="${caaersAvail || labViewerAvail || ctmsAvail}">
                View this subject's
            </c:if>
            <c:if test="${ctmsAvail}">
                <a href="<tags:urlFromTemplate property="patientPageUrl" />" class="control">${configuration.map.ctmsName} record</a>
            </c:if>
            <c:if test="${caaersAvail}">
                <a href="${configuration.map.caAERSBaseUrl}/pages/ae/list?assignment=${assignment.gridId}" class="control">adverse events</a>
            </c:if>
            <c:if test="${labViewerAvail}">
                <a href="${configuration.map.labViewerBaseUrl}/LabSearch?StudyId=${study.id}&PatientId=${subject.personId}" class="control">lab results</a>
            </c:if>

            <c:forEach items="${assignment.currentAeNotifications}" var="aeNote">
                <div id="sae-${aeNote.id}" class="section ae collapsible autoclear">
                    <h2 id="sae-${aeNote.id}-header">Adverse event on <tags:formatDate value="${aeNote.adverseEvent.detectionDate}"/></h2>
                    <div class="content" style="display: none">
                        <p>
                            An adverse event was reported for this subject.  Please consider how
                            this should impact future scheduling.
                        </p>
                        <h3>Details</h3>
                        <p>${aeNote.adverseEvent.description}</p>
                        <p>
                            <a class="dismiss-control" href="<c:url value="/pages/cal/schedule/dismissAe?notification=${aeNote.id}"/>">Dismiss</a>
                            <c:if test="${not empty configuration.map.caAERSBaseUrl}">
                                View <a class="sso" href="${configuration.map.caAERSBaseUrl}/pages/ae/list?assignment=${assignment.gridId}">all adverse events</a>
                            </c:if>
                        </p>
                    </div>
                </div>
            </c:forEach>
        </c:if>
    </div>

    <%--<c:if test="${configuration.}"--%>
</div>

<c:if test="${assignment.endDateEpoch == null}">
    <laf:box title="Modify entire schedule">
        <laf:division>
        <div id="schedule-next-studySegment" class="section autoclear collapsible">

        <h2 id="schedule-next-studySegment-header">Schedule next study segment</h2>
        <div class="content" style="display: none">
            <p class="tip">Select an study segment from the calendar to run next.  Then select a start date.</p>
            <form id="next-studySegment-form" class="autoclear" action="<c:url value="/pages/cal/schedule/nextStudySegment"/>">
                <div class="row">
                    <div class="label">Next study segment</div>
                    <div class="value"><span id="next-studySegment-name"><em>Select at left</em></span></div>
                </div>
                <input type="hidden" name="studySegment" value="-1" id="next-studySegment-id"/>
                <input type="hidden" name="calendar" value="${calendar.id}"/>
                <div class="row" id="mode-row">
                    <div class="label">When?</div>
                    <div class="value">
                        <label><input type="radio" class="mode-radio" id="mode-radio-immediate"
                                      name="mode" value="IMMEDIATE"/> Immediately</label>
                        <label><input type="radio" class="mode-radio" id="mode-radio-per-protocol"
                                      name="mode" value="PER_PROTOCOL" checked="checked"/> Per Protocol</label>
                    </div>

                    <div class="row">
                        <div class="label"><label for="start-date-input">Start date</label></div>
                        <div class="value"><input type="text"  name="startDate" id="start-date-input" value="<tags:formatDate value="${dates['PER_PROTOCOL']}" />" class="date" size="10"/>
                            <a href="#" id="start-date-input-calbutton">
                                <img src="<laf:imageUrl name='chrome/b-calendar.gif'/>" alt="Calendar" width="17"
                                     height="16" border="0" align="absmiddle"/>
                            </a>

                        </div>
                    </div>
                    <div class="alignStudySegmentButtonInTheMiddle">
                        <tags:activityIndicator id="next-studySegment-indicator"/><input type="submit" style="margin:auto;"
                                                                                         value="Schedule next study segment" disabled="disabled" id="next-studySegment-button"/>
                    </div>
                </div>
                </form>
                <tags:epochsAndStudySegments plannedCalendar="${plannedCalendar}"/>
        </div>
        <br style="clear:both">
        <br style="clear:both">
            <div class="section autoclear collapsible">
                <h2 class="topBordered">Delay or advance</h2>
                <div class="content" style="display: none">
                    <laf:division>
                            Shift the scheduled or conditional activities by
                                <input id="toDate" size="5" path="toDate" value="7"/>
                            day(s) as of date:
                        <input id="currentDate" path="currentDate" size="15" value="<tags:formatDate value="${dates['PER_PROTOCOL']}"/>" class="date"/>

                        <a href="#" id="currentDate-calbutton">
                            <img src="<laf:imageUrl name='chrome/b-calendar.gif'/>" alt="Calendar" width="17"
                                 height="16" border="0" align="absmiddle"/>
                        </a>


                        . Reason: <input type="text" id="reason" name="reason" value="" size="50"/>
                        <input type="submit" value="Submit" onclick="ajaxform();" />
                    </laf:division>
                </div>
            </div>                                                       

        </div>
        </laf:division>
    </laf:box>
</c:if>

<div id="scheduled-studySegments" class="section">
    <laf:box title="Study segments scheduled">
        <laf:division>
        <!--<h2>Study segments scheduled</h2>-->
        <p class="tip">Select a study segment to show its detailed schedule below.</p>
        <ul id="scheduled-studySegments-list">
            <li id="scheduled-studySegments-indicator-item"><tags:activityIndicator id="scheduled-studySegments-indicator"/></li>
            <c:forEach items="${calendar.scheduledStudySegments}" var="scheduledStudySegment">
                <sched:scheduledStudySegmentsListItem currentStudySegment="${studySegment}" scheduledStudySegment="${scheduledStudySegment}"/>
            </c:forEach>
        </ul>
        </laf:division>
    </laf:box>
</div>


<div id="selected-studySegment" class="section">
    <sched:scheduledStudySegment studySegment="${studySegment}" visible="true"/>
</div>
</body>
</html>