
<%@page pageEncoding="utf8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags/schedule"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<html>
<head>
    <%--<title>Participant Schedule for ${participant.fullName} on ${plannedCalendar.name}</title>--%>
    <tags:includeScriptaculous/>
    <tags:stylesheetLink name="main"/>
    <tags:javascriptLink name="main"/>
    <tags:javascriptLink name="scheduled-event"/>
    <tags:javascriptLink name="scheduled-event-batch"/>
    <style type="text/css">
        .epochs-and-arms, #next-arm-form {
            margin: 1em;
        }

        .epochs-and-arms {
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

        #next-arm-form {
            margin-top: 0;
            float: right;
            width: 20%;
            padding: 4px;
            border: 1px solid #666;
        }

        #next-arm-form .row .label {
            width: 35%;
            color: #666;
        }

        #next-arm-form .row .value {
            margin-left: 40%;
        }

        #mode-row label {
            display: block;
        }

        #batch-reschedule form {
            font-size: 0.8em;
            margin: 1em;
        }

        #scheduled-arms ul {
            white-space: nowrap;
            padding: 0;
            margin: 0.5em 0;
        }

        #scheduled-arms ul li {
            display: inline;
            padding: 0.2em 0.5em;
            margin: 0 0.3em;
            border: 1px solid #aaa;
            white-space: nowrap;
        }

        #scheduled-arms #scheduled-arms-indicator-item {
            border-color: white;
        }

        #scheduled-arms a {
            text-decoration: none;
            color: black;
        }

        #scheduled-arms ul li.selected {
            background-color: #999 !important;
        }

        #scheduled-arms ul li.selected a {
            color: white;
        }

        #scheduled-arms ul li:hover {
            background-color: #ccc;
        }

        #selected-arm .content {
            position: relative;
        }

        /* For IE */
        * html #selected-arm .content {
            height: 0;
        }

        /*#selected-arm h3 {*/
            /*margin: 0 1em;*/
        /*}*/

        #selected-arm .legend {
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

        #selected-arm .legend h3 {
            background-color: #444;
            color: #fff;
            padding: 4px;
            margin: 0;
            font-size: 11pt;
            font-family: inherit;
        }

        #selected-arm .legend ul {
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

        #external-apps {
            color: #666;
            padding: 8px;
        }

        #schedule-switch {
            float: right;
            margin: 1em;
        }
    </style>
    <script type="text/javascript">
        var DEFAULT_DATES = {
            IMMEDIATE: '<tags:formatDate value="${dates['IMMEDIATE']}"/>',
            PER_PROTOCOL: '<tags:formatDate value="${dates['PER_PROTOCOL']}"/>'
        }

        function registerSelectArmHandlers() {
            $$('#scheduled-arms a').each(registerSelectArmHandler)
        }

        function registerSelectArmHandler(a) {
            var aElement = $(a)
            Event.observe(aElement, "click", function(e) {
                Event.stop(e)
                selectArm(aElement.href)
            })
        }

        function selectArm(href) {
            $("scheduled-arms-indicator").reveal();
            SC.slideAndHide('selected-arm-content', { afterFinish: function() {
                // deselect current
                var sel = $$("#scheduled-arms li.selected")
                if (sel && sel.length > 0) Element.removeClassName(sel[0], "selected")

                new Ajax.Request(href, { asynchronous: true,
                    onComplete: function() {
                        $("scheduled-arms-indicator").conceal()
                    },
                    onFailure: function() {
                        Element.update('selected-arm-content', "Loading failed")
                        Element.update('selected-arm-header', "Error")
                        SC.slideAndShow('selected-arm-content')
                    }
                });
            } });
        }

        function registerSelectNextArmHandlers() {
            $$(".epochs-and-arms a.arm").each(function(a) {
                Event.observe(a, "click", function(e) {
                    Event.stop(e)
                    $('next-arm-id').value = a.id.substring(4)
                    Element.update('next-arm-name', a.title)
                    SC.highlight('next-arm-name', { restorecolor: "#ffffff" })
                    $('next-arm-button').disabled = false
                })
            })

            Event.observe('next-arm-form', "submit", function(e) {
                $('next-arm-indicator').reveal()
                Event.stop(e)
                SC.asyncSubmit('next-arm-form', {
                    onComplete: function() {
                        $('next-arm-indicator').conceal()
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

        function registerHeaderCollapse() {
            $$(".collapsible").each(function(section) {
                var header = section.getElementsByTagName("H2")[0]
                header.innerHTML += " <span class='collapse-icon'>&#65291;</span>"
                header.title = "Click to reveal"
                Event.observe(header, 'click', function() {
                    var content = section.getElementsByClassName("content")[0]
                    var icon = section.getElementsByClassName("collapse-icon")[0]
                    if (content.visible()) {
                        SC.slideAndHide(content, {
                            afterFinish: function() {
                                header.title = "Click to reveal form"
                                Element.update(icon, '&#65291;')
                            }
                        });
                    } else {
                        SC.slideAndShow(content, {
                            afterFinish: function() {
                                header.title = "Click to conceal form"
                                Element.update(icon, '&#8212;')
                            }
                        });
                    }
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

        Event.observe(window, "load", registerSelectArmHandlers);
        Event.observe(window, "load", registerSelectNextArmHandlers);
        Event.observe(window, "load", registerDefaultDateSetterHandlers);
        Event.observe(window, "load", registerHeaderCollapse);
        Event.observe(window, "load", registerDismissControl);
    </script>
</head>
<body>

<laf:box>
<c:if test="${not empty assignments}">
    <div id="schedule-switch">View schedule for
        <select id="assigned-participant-selector">
            <c:forEach items="${assignments}" var="a">
                <option value="${a.scheduledCalendar.id}" <c:if test="${a == assignment}">selected="selected"</c:if>>${a.participant.lastFirst}</option>
            </c:forEach>
        </select>
        <a class="control" href="<c:url value="/pages/schedule"/>" id="go-to-schedule-control">Go</a>
    </div>
</c:if>



<%--<h1>Participant Schedule for ${participant.fullName} on ${plannedCalendar.name}</h1>--%>

<c:if test="${configuration.externalAppsConfigured}">
    <div class="section" id="external-apps">
        <c:set var="caaersAvail" value="${not empty configuration.map.caAERSBaseUrl}"/>
        <c:set var="labViewerAvail" value="${not empty configuration.map.labViewerBaseUrl}"/>
        View this participant's
        <c:if test="${caaersAvail}">
            <a class="sso" href="${configuration.map.caAERSBaseUrl}/pages/ae/list?assignment=${assignment.gridId}">adverse events</a>
        </c:if>
        <c:if test="${caaersAvail and labViewerAvail}">or</c:if>
        <c:if test="${labViewerAvail}">
            <a class="sso" href="${configuration.map.labViewerBaseUrl}/LabSearch?StudyId=${study.protocolAuthorityId}&PatientId=${participant.personId}">lab results</a>
        </c:if>
    </div>
</c:if>

<c:forEach items="${assignment.currentAeNotifications}" var="aeNote">
    <div id="sae-${aeNote.id}" class="section ae collapsible autoclear">
        <h2 id="sae-${aeNote.id}-header">Adverse event on <tags:formatDate value="${aeNote.adverseEvent.detectionDate}"/></h2>
        <div class="content" style="display: none">
            <p>
                An adverse event was reported for this participant.  Please consider how
                this should impact future scheduling.
            </p>
            <h3>Details</h3>
            <p>${aeNote.adverseEvent.description}</p>
            <p>
                <a class="dismiss-control" href="<c:url value="/pages/schedule/dismissAe?notification=${aeNote.id}"/>">Dismiss</a>
                <c:if test="${not empty configuration.map.caAERSBaseUrl}">
                    View <a class="sso" href="${configuration.map.caAERSBaseUrl}/pages/ae/list?assignment=${assignment.gridId}">all adverse events</a>
                </c:if>
            </p>
        </div>
    </div>
</c:forEach>
    </laf:box>


<laf:box>
<div id="schedule-next-arm" class="section autoclear collapsible">

    <h2 id="schedule-next-arm-header">Schedule next arm</h2>
    <div class="content" style="display: none">
        <p class="tip">Select an arm from the calendar to run next.  Then select a start date.</p>
        <form id="next-arm-form" class="autoclear" action="<c:url value="/pages/schedule/nextArm"/>">
            <div class="row">
                <div class="label">Next arm</div>
                <div class="value"><span id="next-arm-name"><em>Select at left</em></span></div>
            </div>
            <input type="hidden" name="arm" value="-1" id="next-arm-id"/>
            <input type="hidden" name="calendar" value="${calendar.id}"/>
            <div class="row" id="mode-row">
                <div class="label">When?</div>
                <div class="value">
                    <label><input type="radio" class="mode-radio" id="mode-radio-immediate"
                                  name="mode" value="IMMEDIATE"/> Immediately</label>
                    <label><input type="radio" class="mode-radio" id="mode-radio-per-protocol"
                                  name="mode" value="PER_PROTOCOL" checked="checked"/> Per Protocol</label>
                </div>
            </div>
            <div class="row">
                <div class="label"><label for="start-date-input">Start date</label></div>
                <div class="value"><input type="text" name="startDate" id="start-date-input" value="<tags:formatDate value="${dates['PER_PROTOCOL']}"/>" size="10"/></div>
            </div>
            <div class="row">
                <div class="value"><tags:activityIndicator id="next-arm-indicator"/><input type="submit" value="Schedule next arm" disabled="disabled" id="next-arm-button"/></div>
            </div>
        </form>
        <tags:epochsAndArms plannedCalendar="${plannedCalendar}"/>
    </div>

</div>
</laf:box>


<div id="scheduled-arms" class="section">
    <laf:box title="Arms scheduled">
        <!--<h2>Arms scheduled</h2>-->
        <p class="tip">Select an arm to show its detailed schedule below.</p>
        <ul id="scheduled-arms-list">
            <li id="scheduled-arms-indicator-item"><tags:activityIndicator id="scheduled-arms-indicator"/></li>
            <c:forEach items="${calendar.scheduledArms}" var="scheduledArm">
                <sched:scheduledArmsListItem currentArm="${arm}" scheduledArm="${scheduledArm}"/>
            </c:forEach>
        </ul>
    </laf:box>
</div>


<div id="selected-arm" class="section">
    <sched:scheduledArm arm="${arm}" visible="true"/>
</div>
</body>
</html>