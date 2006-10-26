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
            .controls a, .inplaceeditor-form a {
                font-weight: bold;
                text-decoration: none;
            }
            .controls a {
                padding: 2px;
                margin: 0 2px;
                color: #444;
                border: 1px solid #999;
                background-color: #ccc;
            }
            .controls a:hover {
                border-color: #444;
                background-color: #ddd;
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

            <c:if test="${not plannedCalendar.complete}">
            Event.observe(window, "load", createStudyControls)
            </c:if>
            Event.observe(window, "load", epochsAreaSetup)
        </script>
    </head>
    <body>
        <h1>Template for <span id="study-name">${study.name}</span></h1>

        <security:secureOperation element="/studycalendar/pages/markComplete" operation="ACCESS">
        <c:if test="${not study.plannedCalendar.complete}">
            <p><a href="<c:url value="/pages/markComplete?id=${study.id}"/>">Mark this template complete</a>.</p>
        </c:if>
        </security:secureOperation>
        <security:secureOperation element="/studycalendar/pages/assignParticipantCoordinator" operation="ACCESS">
        <c:if test="${study.plannedCalendar.complete}">
            <p><a href="<c:url value="/pages/assignParticipantCoordinator?id=${study.id}"/>">Assign Participant Coordinators</a>.</p>
        </c:if>
        </security:secureOperation>
        <security:secureOperation element="/studycalendar/pages/assignParticipant" operation="ACCESS">
        <c:if test="${study.plannedCalendar.complete}">
            <p><a href="<c:url value="/pages/assignParticipant?id=${study.id}"/>">Assign Participants</a>.</p>
        </c:if>
        </security:secureOperation>

        <div id="epochs" class="section">
            <h2>Epochs and arms</h2>
            <tags:epochsAndArms id="epochs-container" plannedCalendar="${calendar}" selectedArm="${arm.base}"/>
        </div>

        <div id="selected-arm" class="section">
            <templ:arm arm="${arm}" visible="true"/>
        </div>

    </body>
</html>