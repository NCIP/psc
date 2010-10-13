<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<html>
<head>
    <title>${commons:capitalize(verb)} Period</title>
    <style type="text/css">
        form { width: 60em; }
        div.row div.label { width: 9em; }
        div.row div.value { margin-left: 10em; }
        div.tip {
            width: 35em;
            float: right;
        }
        .submit {
            text-align: right;
        }
        div.tab {margin-left:5em;}
    </style>
    <tags:includeScriptaculous/>
    <script type="text/javascript">
        function updateSummary() {
            var duration = calculateDuration()
            var startDay = parseInt($F("period.startDay"))
            var reps = $F("period.repetitions")

            var unit = $F("period.duration.unit")
            var totalDays;
            var numberOfDaysInUnit;
            if (unit == "week") {
                numberOfDaysInUnit = 7;
            } else if (unit == "fortnight") {
                numberOfDaysInUnit = 14;
            } else if (unit == "month") {
                numberOfDaysInUnit = 28;
            } else if (unit == "quarter") {
                numberOfDaysInUnit = 91;
            } else {
                numberOfDaysInUnit =1;
            }
            totalDays = reps * duration * numberOfDaysInUnit;
            if (isCorrectInput()) {
                Element.update("summary-beginning", " The configured period will last for ")
                Element.update("summary-days", totalDays + " day" + (totalDays != 1 ? "s" : ""))
                highlightOdd("summary-days")

                resetElement("periodError","");
                if (duration > 1) {
                    Element.update("summary-day-range", "(" + startDay + " to " + (startDay + duration * numberOfDaysInUnit * reps - 1) + ")")
                } else {
                    Element.update("summary-day-range", "(day " + startDay + ")")
                }
            } else {
                disableSummaryFields()
            }
            highlightOdd("summary-day-range")

            if (reps > 1 && duration >= 1 && isCorrectInput()) {
                var sameDay = [startDay]
                while (sameDay.length < reps) {
                    sameDay.push(sameDay.last() + duration * numberOfDaysInUnit)
                }
                var eventDays = sameDay.join(", ")
                if (duration > 1) {
                    eventDays += "; and on days "
                    eventDays += sameDay.map(function(e) {
                        return e + numberOfDaysInUnit;
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

        function disableSummaryFields(){
            Element.update("summary-beginning", "Incorrect input")
            Element.update("summary-days", "")
            Element.update("summary-day-range", "")
        }

        function highlightOdd(elt) {
            SC.highlight(elt, {
                endcolor: "#eeeeee",
                restorecolor: "#eeeeee"
            })
        }

        function calculateDuration() {
            return parseInt($F("period.duration.quantity"));
        }

        function resetElement(elementId, text, color) {
            var element = document.getElementById(elementId);
            element.style.color = color;
            element.innerHTML = text;
        }

        // TODO: client-side validation is anathema

        function isCorrectStartDay() {
            var startDay = document.getElementById("period.startDay").value;
            var isDataCorrect = true;
            if(startDay.length <= 0 || startDay.indexOf(".")>0 || startDay.indexOf(",") > 0 || (isNaN(startDay*1))) {
                isDataCorrect = false;
                resetElement("periodError",
                  "ERROR: Start Day must be positive, negative, or zero.", "black");
            }
            return isDataCorrect;
        }

        function isCorrectDuration() {
            var duration = document.getElementById("period.duration.quantity").value;
            var isDataCorrect = true;
            if (duration.length <=0 ||duration.indexOf(".")>0 || duration.indexOf(",") > 0 || ((isNaN(duration*1)) || (duration <=0))) {
                isDataCorrect = false;
                resetElement("periodError",
                  "ERROR: Duration must be a positive number.", "black");
            }
            return isDataCorrect;
        }

        function isCorrectRepetitions() {
            var repetitions = document.getElementById("period.repetitions").value;
            var isDataCorrect = true;
            if (repetitions.length <=0 || repetitions.indexOf(".")>0 || repetitions.indexOf(",") > 0
                    ||((isNaN(repetitions*1)) || (repetitions <=0))) {
                isDataCorrect = false;
                resetElement("periodError",
                                  "ERROR: Repetitions must be a positive number.", "black");
            }
            return isDataCorrect;
        }

        function isCorrectInput() {
            var isDataCorrect = true;
            isDataCorrect = isCorrectStartDay();
            if(!isDataCorrect) { return false; }
            isDataCorrect = isCorrectDuration();
            if(!isDataCorrect) { return false; }
            isDataCorrect= isCorrectRepetitions();
            return isDataCorrect
        }

        Element.observe(window, "load", function() {
           $$("#period-form input").each(function(elt) {
                new Form.Element.Observer(elt, 1, updateSummary);

           })

           $$("#period-form select").each(function(elt) {
                new Form.Element.Observer(elt, 1, updateSummary);
           })

           updateSummary();
       })

        //scripts for copy period functionality
        var templateAutocompleter;
        function unselectPeriodBox(selectedCheckBox) {
            $('periods').select('[checkbox[name="periodName"]').each(function(item) {
                item.checked = false;
              });
            selectedCheckBox.checked = true
            $('selectedPeriod').value=selectedCheckBox.id

        }
        function updatePeriodsDisplay(input, li) {
            var id = li.id;
            $('template-autocompleter-input').value = "";
            $('template-autocompleter-input').hint = "Search for study";
            updatePeriods(id)
            $('template-autocompleter-input-id').value = id
        }

        function updatePeriods(studyId) {
            var aElement = '<c:url value="/pages/cal/template/selectInDevelopmentAndReleasedStudy"/>?study=' + studyId
            var lastRequest = new Ajax.Request(aElement);
        }

        function createTemplatesAutocompleter() {
           if ($('copy')) {
            templateAutocompleter = new SC.FunctionalAutocompleter(
                'template-autocompleter-input', 'template-autocompleter-div', studyAutocompleterChoices, {
                    select: "templates-name",
                    afterUpdateElement: updatePeriodsDisplay,
                    revertOnEsc:true

                }
            );
           }
        }

        function studyAutocompleterChoices(str, callback) {
            studyAutocompleterChoiceProcessing(function(data) {
                var lis = data.map(function(study) {
                    var id = study.id
                    var name = study.assigned_identifier
                    var listItem = "<li id='"  + id + "'>" + name + "</li>";
                    return listItem
                }).join("\n");
                callback("<ul>\n" + lis + "\n</ul>");
            });
        }

        function studyAutocompleterChoiceProcessing(callback) {
            var searchString = $F("template-autocompleter-input")
            var uri = SC.relativeUri("/api/v1/studies")
            if (searchString.blank()) {
                return;
            }

            var params = { };
            if (!searchString.blank()) params.q = searchString;

            SC.asyncRequest(uri+".json", {
                method: "GET", parameters: params,
                onSuccess: function(response) {
                    callback(response.responseJSON.studies)
                }
            })
        }

        function currentTemplateSelected() {
            if ($('copy')) {
                updatePeriods($('template-autocompleter-input-id').value, 'true')
            }
        }
        
        Event.observe(window, "load", currentTemplateSelected)
        Event.observe(window, "load", createTemplatesAutocompleter)

        // Temporary.  Validation should really be on the server side.
        $(document).observe("dom:loaded", function() {
            if ($('copy-form')) {
                $('copy-form').observe("submit", function(evt) {
                    if ($F('selectedPeriod').blank()) {
                        resetElement("periodError", "ERROR: Please select one period.", "black");
                        Event.stop(evt);
                    }
                })
            }

            $('period-form').observe("submit", function(evt) {
                if (!isCorrectInput()) {
                    Event.stop(evt);
                }
            })
        })

    </script>
</head>
<body>
<laf:box title="${commons:capitalize(verb)} period">
    <laf:division>
        <%--<h2>${commons:capitalize(verb)} Period</h2>--%>
        <form:form method="post" id="period-form" >
            <h5 id="periodError"></h5>
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
                <div class="tip" id="startDayText">
                    The relative day of the start of this period.  This may be positive, negative, or zero.
                </div>
                <div class="value">
                    <form:input path="period.startDay" size="4" maxlength="4"/>
                </div>
            </div>
            <div class="row odd">
                <div class="label">
                    <form:label path="period.duration.quantity">Duration</form:label>
                </div>
                <div class="tip" id="durationText">
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
                    <form:label path="period.repetitions">Occurrences</form:label>
                </div>
                <div class="tip" id="repetitionsText">
                    The number of times the days of this period will occur.
                </div>
                <div class="value">
                    <form:input path="period.repetitions" size="3" maxlength="3"/>
                </div>
            </div>
            <div class="row odd" id="summary">
                <div class="label">Summary</div>
                <div class="value" id="summaryValue">
                   <span id="summary-beginning"> The configured period will last for </span>
                   <span id="summary-days">1 day</span>
                   <span id="summary-day-range">(day 1)</span>.
            <span id="summary-repetitions">
                It will have the same events on days <span id="summary-event-days"></span>.
            </span>
                </div>
            </div>
            <div class="row">
                <input id="submitButton" type="submit" value="Submit"/>
            </div>
        </form:form>
    </laf:division>
</laf:box>
<c:if test="${period.id == null}">
    <laf:box title="Copy existing period">
        <laf:division>
            <div class="row">
                Search for study:
                <input id="template-autocompleter-input" type="text" autocomplete="off" value="" hint="Search for study" class="autocomplete"/>
                <input type="hidden" id="template-autocompleter-input-id" value="${studyId}"/>
                <input type="hidden" id="isDevelopmentTemplateSelected" name="isDevelopmentTemplateSelected" value="true"/>
                <div id="template-autocompleter-div" class="autocomplete" style="z-index:1000" ></div>
            </div>
        </laf:division>
        <h3 id="studyName" >${study.name}</h3>
        <laf:division>
            <form:form method="post" id="copy-form" >
                <div class="row" id="selected-epochs">
                </div>

                <div class="row">
                    <input id="copy" type="submit" value="Copy"/>
                </div>                
            </form:form>
        </laf:division>
    </laf:box>
</c:if>
</body>
</html>
