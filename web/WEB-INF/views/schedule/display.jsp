<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<html>
<head>
    <title>Participant Schedule for ${participant.fullName} on ${plannedCalendar.name}</title>
    <tags:includeScriptaculous/>
    <style type="text/css">
        .section {
            border: 2px solid #666;
            margin: 1em 0;
        }
        .section p {
            margin: 1em;
        }

        h3 {
            margin: 0;
            padding: 4px;
            font-family: sans-serif;
            color: white;
            background-color: #999;
        }

        .epochs-and-arms, #next-arm-form {
            margin: 1em;
        }

        .epochs-and-arms {
            width: 75%;
        }

        #next-arm-form {
            float: right;
            height: 100%;
            width: 20%;
        }

        #next-arm-form .row .label {
            width: 40%;
            color: #666;
        }

        .day {
            margin: 1em;
            position: relative;
        }
        .day h4 {
            float: left;
            margin: 0;
        }
        .day ul {
            margin-left: 4em;
        }
        .day ul li {
            list-style-type: none;
        }

        .tip {
            font-size: 80%;
        }

        #scheduled-arms ul li {
            display: inline;
            padding: 0.5em;
            border: 1px solid #aaa;
        }

        #scheduled-arms a {
            text-decoration: none;
        }

        #scheduled-arms ul li.selected {
            color: white;
            background-color: #999;
        }

        #scheduled-arms ul li.selected a {
            color: white;
        }
    </style>
    <script type="text/javascript">
        function registerSelectArmHandlers() {
            $$('#scheduled-arms a').each(function(a) {
                Event.observe(a, "click", function(e) {
                    Event.stop(e)
                    SC.slideAndHide('selected-arm', { afterFinish: function() {
                        // deselect current
                        var sel = $$("#scheduled-arms li.selected")
                        if (sel && sel.length > 0) Element.removeClassName(sel[0], "selected")

                        new Ajax.Request(a.href, { asynchronous: true,
                            onSuccess: function() {
                                SC.slideAndShow('selected-arm')
                            }, onFailure: function() {
                                Element.update('selected-arm', "Loading failed")
                                SC.slideAndShow('selected-arm')
                            }
                        });
                    } });
                })
            })
        }

        function registerSelectNextArmHandlers() {
            $$(".epochs-and-arms a.arm").each(function(a) {
                Event.observe(a, "click", function(e) {
                    Event.stop(e)
                    $('next-arm-id').value = a.id.substring(4)
                    Element.update('next-arm-name', a.title)
                    SC.highlight('next-arm-name')
                    $('next-arm-button').disabled = false
                })
            })

            Event.observe('next-arm-form', "submit", function(e) {
                var form = $('next-arm-form')
                Event.stop(e)
                new Ajax.Request(form.action, {
                    asynchronous: true,
                    parameters: Form.serialize(form)
                })
            })
        }

        Event.observe(window, "load", registerSelectArmHandlers);
        Event.observe(window, "load", registerSelectNextArmHandlers);
    </script>
</head>
<body>
<h1>Participant Schedule for ${participant.fullName} on ${plannedCalendar.name}</h1>

<div class="section">
    <h3>Schedule next arm</h3>
    <div class="content autoclear">
        <p class="tip">Select an arm from the calendar to run next.  Then select a start date.</p>
        <form id="next-arm-form" action="<c:url value="/pages/schedule/nextArm"/>">
            <div class="row">
                <div class="label">Next arm</div>
                <div class="value"><span id="next-arm-name"></span></div>
            </div>
            <input type="hidden" name="arm" value="-1" id="next-arm-id"/>
            <div class="row">
                <div class="label"><label for="start-date-input">Start date</label></div>
                <div class="value"><input type="text" name="startDate" id="start-date-input" size="10"/></div>
            </div>
            <div class="row">
                <div class="value"><input type="submit" value="Schedule next arm" disabled="disabled" id="next-arm-button"/></div>
            </div>
        </form>
        <tags:epochsAndArms plannedCalendar="${plannedCalendar}" widthPercent="75"/>
    </div>
</div>

<div id="scheduled-arms" class="section">
    <h3>Arms scheduled</h3>
    <p class="tip">Select an arm to show its detailed schedule below.</p>
    <ul>
    <c:forEach items="${calendar.scheduledArms}" var="scheduledArm">
        <li id="select-arm-${scheduledArm.id}" ${arm.id == scheduledArm.id ? 'class="selected"' : ''}>
            <a href="<c:url value="/pages/schedule/select?arm=${scheduledArm.id}"/>">${scheduledArm.name}</a>
        </li>
    </c:forEach>
    </ul>
</div>

<div id="selected-arm" class="section">
    <tags:scheduledArm arm="${arm}"/>
</div>
</body>
</html>