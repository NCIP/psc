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
    <style type="text/css">
        .epochs-and-arms, #next-arm-form {
            margin: 1em;
        }

        .epochs-and-arms {
            width: 75%;
        }

        #epochs-indicator {
            display: none;
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
                $("scheduled-arms-indicator").reveal();
                SC.slideAndHide('selected-arm-content', { afterFinish: function() {
                    // deselect current
                    var sel = $$("#scheduled-arms li.selected")
                    if (sel && sel.length > 0) Element.removeClassName(sel[0], "selected")

                    new Ajax.Request(aElement.href, { asynchronous: true,
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
            })
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
                    },
                    onFailure: function() {
                        alert("TODO: need to handle errors")
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

        Event.observe(window, "load", registerSelectArmHandlers);
        Event.observe(window, "load", registerSelectNextArmHandlers);
        Event.observe(window, "load", registerDefaultDateSetterHandlers);
    </script>
</head>
<body>
<h1>Participant Schedule for ${participant.fullName} on ${plannedCalendar.name}</h1>

<div class="section autoclear">
    <h2>Schedule next arm</h2>
    <div class="content">
        <p class="tip">Select an arm from the calendar to run next.  Then select a start date.</p>
        <form id="next-arm-form" class="autoclear" action="<c:url value="/pages/schedule/nextArm"/>">
            <div class="row">
                <div class="label">Next arm</div>
                <div class="value"><span id="next-arm-name"><em>Select at left</em></span></div>
            </div>
            <input type="hidden" name="arm" value="-1" id="next-arm-id"/>
            <input type="hidden" name="calendar" value="${calendar.id}"/>
            <div class="row">
                <div class="label"><label for="start-date-input">Start date</label></div>
                <div class="value"><input type="text" name="startDate" id="start-date-input" value="<tags:formatDate value="${dates['PER_PROTOCOL']}"/>" size="10"/></div>
            </div>
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
                <div class="value"><tags:activityIndicator id="next-arm-indicator"/><input type="submit" value="Schedule next arm" disabled="disabled" id="next-arm-button"/></div>
            </div>
        </form>
        <tags:epochsAndArms plannedCalendar="${plannedCalendar}"/>
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