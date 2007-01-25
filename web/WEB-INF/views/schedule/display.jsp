
<%@page pageEncoding="utf8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags/schedule"%>
<html>
<head>
    <title>Participant Schedule for ${participant.fullName} on ${plannedCalendar.name}</title>
    <tags:includeScriptaculous/>
    <tags:stylesheetLink name="main"/>
    <tags:javascriptLink name="main"/>
    <tags:javascriptLink name="scheduled-event"/>
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

        #selected-arm h3 {
            margin: 0 1em;
        }

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

        function registerBatchRescheduleHandlers() {
            Event.observe('batch-form', "submit", function(e) {
                $('batch-indicator').reveal()
                Event.stop(e)
                SC.asyncSubmit('batch-form', {
                    onComplete: function() {
                        $('batch-indicator').conceal()
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
                header.innerHTML += " <span class='collapse-icon'>&#9660;</span>"
                header.title = "Click to reveal"
                Event.observe(header, 'click', function() {
                    var content = section.getElementsByClassName("content")[0]
                    var icon = section.getElementsByClassName("collapse-icon")[0]
                    if (content.visible()) {
                        SC.slideAndHide(content, {
                            afterFinish: function() {
                                header.title = "Click to reveal form"
                                Element.update(icon, '&#9660;')
                            }
                        });
                    } else {
                        SC.slideAndShow(content, {
                            afterFinish: function() {
                                header.title = "Click to conceal form"
                                Element.update(icon, '&#9650;')
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
        Event.observe(window, "load", registerBatchRescheduleHandlers);
        Event.observe(window, "load", registerDefaultDateSetterHandlers);
        Event.observe(window, "load", registerHeaderCollapse);
        Event.observe(window, "load", registerDismissControl);
    </script>
</head>
<body>
<h1>Participant Schedule for ${participant.fullName} on ${plannedCalendar.name}</h1>

<c:forEach items="${assignment.currentAeNotifications}" var="aeNote">
    <div id="sae-${aeNote.id}" class="section ae collapsible autoclear">
        <h2 id="sae-${aeNote.id}-header">AE on <tags:formatDate value="${aeNote.adverseEvent.detectionDate}"/></h2>
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
                    <a class="sso" href="${configuration.map.caAERSBaseUrl}/pages/ae/list?assignment=${assignment.bigId}">View in caAERS</a>
                </c:if>
            </p>
        </div>
    </div>
</c:forEach>

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

<div id="batch-reschedule" class="section collapsible">
    <h2>Batch reschedule</h2>
    <div class="content" style="display: none">
        <p class="tip">
            To change all currently scheduled events, first select to what you'd like to change them.
            Then fill in the rest of the form.
        </p>
        <form id="batch-form" action="<c:url value="/pages/schedule/batch"/>">
            <input type="hidden" name="scheduledCalendar" value="${calendar.id}"/>
            <label id="new-mode-selector-group">
                <select name="newMode" id="new-mode-selector">
                    <option></option>
                    <option value="1">Keep as scheduled</option>
                    <option value="3">Mark canceled</option>
                </select>
            </label>
            <label id="new-date-input-group">and shift date by <input type="text" name="dateOffset" value="7" size="4"/> days.</label>
            <label id="new-reason-input-group">
                Why? <input type="text" name="newReason"/>
                <tags:activityIndicator id="batch-indicator"/>
                <input type="submit" value="Submit"/>
            </label>
        </form>
        <p class="tip">
            This change will affect all events for this participant that have not been canceled or
            marked "occurred."  To change a single event, click it in the full schedule display
            below.
        </p>
    </div>
</div>

<div id="scheduled-arms" class="section">
    <h2>Arms scheduled</h2>
    <p class="tip">Select an arm to show its detailed schedule below.</p>
    <ul id="scheduled-arms-list">
        <li id="scheduled-arms-indicator-item"><tags:activityIndicator id="scheduled-arms-indicator"/></li>
    <c:forEach items="${calendar.scheduledArms}" var="scheduledArm">
        <sched:scheduledArmsListItem currentArm="${arm}" scheduledArm="${scheduledArm}"/>
    </c:forEach>
    </ul>
</div>

<div id="selected-arm" class="section">
    <sched:scheduledArm arm="${arm}" visible="true"/>
</div>
</body>
</html>