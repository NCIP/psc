<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<html>
<head>
    <title>${commons:capitalize(verb)} Period</title>
    <style type="text/css">
        form { width: 45em; }
        div.label { width: 9em; }
        div.value { margin-left: 10em; }
        div.tip {
            width: 35em;
            float: right;
        }
        .submit {
            text-align: right;
        }
    </style>
    <tags:includeScriptaculous/>
    <script type="text/javascript">
        function updateSummary() {
            var duration = calculateDuration()
            var startDay = parseInt($F("period.startDay"))
            var reps = $F("period.repetitions")
            var totalDays = reps * duration

            Element.update("summary-days", totalDays + " day" + (totalDays != 1 ? "s" : ""))
            highlightOdd("summary-days")

            if (duration > 1) {
                Element.update("summary-day-range", "(" + startDay + " to " + (startDay + duration * reps - 1) + ")")
            } else {
                Element.update("summary-day-range", "(day " + startDay + ")")
            }
            highlightOdd("summary-day-range")

            if (reps > 1 && duration >= 1) {
                var sameDay = [startDay]
                while (sameDay.length < reps) {
                    sameDay.push(sameDay.last() + duration)
                }
                var eventDays = sameDay.join(", ")
                if (duration > 1) {
                    eventDays += "; and on days "
                    eventDays += sameDay.map(function(e) {
                        return e + 1;
                    }).join(", ")
                    if (duration > 2) {
                        eventDays += "; etc"
                    }
                }
                Element.update("summary-event-days", eventDays)
                $("summary-repetitions").show()
                highlightOdd("summary-event-days")
            } else {
                $("summary-repetitions").hide()
            }
        }

        function highlightOdd(elt) {
            SC.highlight(elt, {
                endcolor: "#eeeeee",
                restorecolor: "#eeeeee"
            })
        }

        function calculateDuration() {
            var q = parseInt($F("period.duration.quantity"))
            var unit = $F("period.duration.unit")
            if (unit == "week") return q * 7;
            else return q;
        }

        Element.observe(window, "load", function() {
            $$("#period-form input").each(function(elt) {
                Element.observe(elt, "change", updateSummary)
            })
            $$("#period-form select").each(function(elt) {
                Element.observe(elt, "change", updateSummary)
            })
            updateSummary();
        })
    </script>
</head>
<body>
<h2>${commons:capitalize(verb)} Period</h2>
<form:form method="post" id="period-form">
    <div class="row odd">
        <div class="label">
            <form:label path="period.name">Name</form:label>
        </div>
        <div class="tip">
            (Optional) A name by which you can refer to this period later
        </div>
        <div class="value">
            <form:input path="period.name"/>
        </div>
    </div>
    <div class="row even">
        <div class="label">
            <form:label path="period.startDay">Start day</form:label>
        </div>
        <div class="tip">
            The relative day of the start of this period.  This may be positive, negative, or zero.
        </div>
        <div class="value">
            <form:input path="period.startDay" size="3" maxlength="3"/>
        </div>
    </div>
    <div class="row odd">
        <div class="label">
            <form:label path="period.duration.quantity">Duration</form:label>
        </div>
        <div class="tip">
            The length of a single repetition of this period.  This must be a positive number,
            and may be expressed in days or weeks.
        </div>
        <div class="value">
            <form:input path="period.duration.quantity" size="3" maxlength="3"/>
            <form:select path="period.duration.unit">
                <form:options items="${durationUnits}"/>
            </form:select>
        </div>
    </div>
    <div class="row even">
        <div class="label">
            <form:label path="period.repetitions">Repetitions</form:label>
        </div>
        <div class="tip">
            The number of times the days of this period will occur.
        </div>
        <div class="value">
            <form:input path="period.repetitions" size="3" maxlength="3"/>
        </div>
    </div>
    <div class="row odd" id="summary">
        <div class="label">Summary</div>
        <div class="value">
            The configured period will last for <span id="summary-days">1 day</span>
            <span id="summary-day-range">(day 1)</span>.
            <span id="summary-repetitions">
                It will have the same events on days <span id="summary-event-days"></span>.
            </span>
        </div>
    </div>
    <div class="even row submit"><input id="submit" type="submit" value="Submit"/></div>
</form:form>
</body>
</html>
