<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="templ" tagdir="/WEB-INF/tags/template" %>
<%@ taglib prefix="security"
           uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<html>
    <head>
        <title>Template for ${study.name}</title>
        <tags:stylesheetLink name="main"/>
        <tags:includeScriptaculous/>
        <tags:javascriptLink name="main"/>
        <style type="text/css">
            .epochs-and-arms {
                margin: 1em;
            }

            #epochs-indicator {
                margin: 0.5em 0.5em 0 0;
                float: left;
            }

            table.periods, table.periods tr, table.periods td, table.periods th {
                border-spacing: 0;
                border: 0 solid #666;
                margin: 1em;
            }
            table.periods td, table.periods th {
                width: 2em;
            }
            table.periods th {
                padding: 2px;
                border-right-width: 1px;
            }
            table.periods th.row {
                padding-right: 0.5em;
                text-align: right;
            }
            table.periods th.column {
                border-top-width: 1px;
            }
            table.periods tr.resume th {
                border-right: 1px solid #ddd;
            }
            table.periods td {
                padding: 0;
                border-width: 1px 1px 0 0;
                text-align: center;
            }
            table.periods a {
                text-decoration: none;
                margin: 0;
                padding: 2px;
                display: block;
                color: #444;
                /* For IE */
                height: 1%;
            }
            table.periods a:hover {
                color: #000;
            }
            table.periods td.repetition:hover {
                background-color: #ccc;
            }
            table.periods td.repetition {
                background-color: #ddd;
                border-right-width: 0;
            }
            table.periods td.empty {
                background-color: #fff;
                border-right-width: 0;
            }
            table.periods td.last {
                border-right-width: 1px;
            }
            table.periods tr.last td {
                border-bottom-width: 1px;
            }

            .days {
                margin: 0 3em 3em 5em;
            }

            li.arm, #epochs h4 {
                position: relative;
            }
            /* For IE */
            * html #epochs h4 { height: 1px; }
            .controls {
                font-family: Arial, sans-serif;
                font-size: 7.5pt;
            }
            div.arm-controls, div.epoch-controls {
                position: absolute;
                bottom: 4px;
                right: 2px;
                text-align: right;
            }
            span.study-controls {
                margin-left: 4px;
            }

            .inplaceeditor-form a {
                font-size: 11pt;
                border: 1px solid #444;
                padding: 3px;
            }

            ul#admin-options {
                padding: 0;
                margin: 0;
            }
            ul#admin-options li {
                display: inline;
                padding: 2px 4px;
                margin: 0;
                list-style-type: none;
            }

            span.event-details {
                color: #666;
                font-style: italic;
                font-size: 0.9em;
            }
        </style>
        <c:if test="${not plannedCalendar.complete}">
        <script type="text/javascript" src="<c:url value="/pages/template/edit.js?study=${study.id}"/>"></script>
        </c:if>
        <script type="text/javascript">
            var lastRequest;
            var selectedArmId = ${arm.base.id};

            function registerSelectArmHandlers() {
                $$('#epochs a').each(registerSelectArmHandler)
            }

            function registerSelectArmHandler(a) {
                var aElement = $(a)
                Event.observe(aElement, "click", function(e) {
                    Event.stop(e)
                    $("epochs-indicator").reveal();
                    SC.slideAndHide('selected-arm-content', { afterFinish: function() {
                        // deselect current
                        var sel = $$("#epochs .selected")
                        if (sel && sel.length > 0) Element.removeClassName(sel[0], "selected")

                        var armId = aElement.id.substring(4)
                        selectedArmId = armId
                        aElement.href = '<c:url value="/pages/template/select"/>?arm=' + armId

                        lastRequest = new Ajax.Request(aElement.href,
                            {
                                onComplete: function(req) {
                                    $("epochs-indicator").conceal()
                                },
                                onFailure: function() {
                                    Element.update('selected-arm-content', "<p class='error'>Loading failed</p>")
                                    Element.update('selected-arm-header', "Error")
                                    SC.slideAndShow('selected-arm-content')
                                }
                            }
                        );
                    } });
                })
            }

            function epochsAreaSetup() {
                registerSelectArmHandlers()
                <c:if test="${not plannedCalendar.complete}">
                createAllArmControls()
                createAllEpochControls()
                </c:if>
            }

            function registerAdminOptionsControls() {
                if ($('go-to-schedule-control')) {
                    Event.observe('go-to-schedule-control', "click", function(e) {
                        Event.stop(e)
                        var a = $('go-to-schedule-control')
                        var scheduleId = $F('assigned-participant-selector')
                        window.location.href = a.href + "?calendar=" + scheduleId;
                    })
                }
            }

            <c:if test="${not plannedCalendar.complete}">
            Event.observe(window, "load", createStudyControls)
            </c:if>
            Event.observe(window, "load", epochsAreaSetup)
            Event.observe(window, "load", registerAdminOptionsControls)
        </script>
    </head>
    <body>
        <h1>Template for <span id="study-name">${study.name}</span></h1>

        <ul id="admin-options">
            <tags:restrictedListItem url="/pages/markComplete" queryString="study=${study.id}" cssClass="control"
                logicNotAllowed="${plannedCalendar.complete}">Mark this template complete</tags:restrictedListItem>
            <tags:restrictedListItem url="/pages/assignParticipantCoordinator" queryString="id=${study.id}" cssClass="control"
                logicNotAllowed="${not plannedCalendar.complete}">Assign Participant Coordinators</tags:restrictedListItem>
            <tags:restrictedListItem url="/pages/assignParticipant" queryString="id=${study.id}" cssClass="control"
                logicNotAllowed="${not plannedCalendar.complete}">Assign Participant</tags:restrictedListItem>
            <c:if test="${not empty assignments}">
                <security:secureOperation element="/pages/schedule" operation="ACCESS">
                <li>View schedule for
                    <select id="assigned-participant-selector">
                        <c:forEach items="${assignments}" var="assignment">
                            <option value="${assignment.scheduledCalendar.id}">${assignment.participant.lastFirst}</option>
                        </c:forEach>
                    </select>
                    <a class="control" href="<c:url value="/pages/schedule"/>" id="go-to-schedule-control">Go</a>
                </li>
                </security:secureOperation>
            </c:if>
        </ul>

        <div id="epochs" class="section">
            <h2>Epochs and arms</h2>
            <tags:epochsAndArms id="epochs-container" plannedCalendar="${plannedCalendar}" selectedArm="${arm.base}"/>
        </div>

        <div id="selected-arm" class="section">
            <templ:arm arm="${arm}" visible="true"/>
        </div>

    </body>
</html>