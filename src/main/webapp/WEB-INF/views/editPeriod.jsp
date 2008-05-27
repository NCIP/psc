<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
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

        function isCorrectStartDay() {
            var startDay = document.getElementById("period.startDay").value;
            var isDataCorrect = true;
            if(startDay.length <= 0 || (isNaN(startDay*1))) {
                isDataCorrect = false;
                resetElement("periodError",
                  "ERROR: Start Day must be positive, negative, or zero.", "black");
            }
            return isDataCorrect;
        }

        function isCorrectDuration() {
            var duration = document.getElementById("period.duration.quantity").value;
            var isDataCorrect = true;
            if (duration.length <=0 || ((isNaN(duration*1)) || (duration <=0))) {
                isDataCorrect = false;
                resetElement("periodError",
                  "ERROR: Duration must be a positive number.", "black");
            }
            return isDataCorrect;
        }

        function isCorrectRepetitions() {
            var repetitions = document.getElementById("period.repetitions").value;
            var isDataCorrect = true;
            if (repetitions.length <=0 || ((isNaN(repetitions*1)) || (repetitions <=0))) {
                isDataCorrect = false;
                resetElement("periodError",
                                  "ERROR: Repetitions must be a positive number.", "black");
            }
            return isDataCorrect;
        }

        function isCorrectInput() {
            var isDataCorrect = true;
            isDataCorrect = isCorrectStartDay();
            if(!isDataCorrect) { return isDataCorrect; }
            isDataCorrect = isCorrectDuration();
            if(!isDataCorrect) { return isDataCorrect; }
            return isCorrectRepetitions();
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

            var isDevelopmentTemplateSelected='false'

            if (id.indexOf('dev') == 0) {
                isDevelopmentTemplateSelected = 'true'
                $('isDevelopmentTemplateSelected').value = 'true'
            }
            if (id.indexOf('rel') == 0) {
                isDevelopmentTemplateSelected = 'false'
                $('isDevelopmentTemplateSelected').value = 'false'

            }

            id = id.substring(3, id.length)

            updatePeriods(id,isDevelopmentTemplateSelected)
            $('template-autocompleter-input-id').value = id

        }
        function updatePeriods(studyId,isDevelopmentTemplateSelected) {

            var aElement = '<c:url value="/pages/cal/template/selectInDevelopmentAndReleasedStudy"/>?study=' + studyId+"&isDevelopmentTemplateSelected="+isDevelopmentTemplateSelected
            var lastRequest = new Ajax.Request(aElement);

        }
        function createAutocompleter() {
            if ($('copy') != null) {

                templateAutocompleter = new Ajax.ResetableAutocompleter('template-autocompleter-input', 'template-autocompleter-div', '<c:url value="/pages/cal/search/fragment/inDevelopmentAndReleasedTemplates"/>',
                {
                    method: 'get',
                    paramName: 'searchText',
                    afterUpdateElement:updatePeriodsDisplay,
                    revertOnEsc:true
                });
            }
        }

        function currentTemplateSelected() {
            if ($('copy') != null) {
                updatePeriods($('template-autocompleter-input-id').value, 'true')
            }
        }
        function isUserSelectedAnyPeriod() {
            var isDataCorrect = true;
             if($('selectedPeriod').value==''){    isDataCorrect = false;
                resetElement("periodError",
                                  "ERROR: Please select one period.", "black");
            }
            return isDataCorrect;
        }
        Event.observe(window, "load", currentTemplateSelected)
        Event.observe(window, "load", createAutocompleter)
        Event.observe(window, "load", initSearchField);

    </script>
</head>
<body>
<laf:box title="${commons:capitalize(verb)} Period">
    <laf:division>
        <%--<h2>${commons:capitalize(verb)} Period</h2>--%>
        <form:form method="post" id="period-form">
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
                    <form:input path="period.startDay" size="3" maxlength="3"/>
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
                    <form:label path="period.repetitions">Repetitions</form:label>
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
                <div class="value">
                    The configured period will last for <span id="summary-days">1 day</span>
                    <span id="summary-day-range">(day 1)</span>.
            <span id="summary-repetitions">
                It will have the same events on days <span id="summary-event-days"></span>.
            </span>
                </div>
            </div>
            <div class="even row submit">
                <input id="submit" type="submit" value="Submit" onclick="return(isCorrectInput())" />
            </div>
            <!--<div class="even row submit"><input id="submit" type="submit" value="Submit"/></div>    -->
            <!---->

            <!--html code for copy period functionality-->

            <c:if test="${period.id == null}">
                <laf:body title="Copy from another period">

                        <div class="row">

                            <input id="template-autocompleter-input" type="text" autocomplete="off" value="${selectedStudy}" class="search"/>
                            <input type="hidden" id="template-autocompleter-input-id" value="${studyId}"/>
                            <input type="hidden" id="isDevelopmentTemplateSelected" name="isDevelopmentTemplateSelected" value="true"/>

                            <div id="template-autocompleter-div" class="autocomplete"></div>
                        <div class="row" id="periods">
                        <input type="hidden" id="selectedPeriod" name="selectedPeriod" value=""/>

                        <div class="row" id="selected-epochs">

                     </div>


                    </div>
                        </div>

                     <div class="row">
                        <input id="copy" type="submit" value="Copy" name="copyPeriod" onclick="return(isUserSelectedAnyPeriod())"/>
                      </div>
                  </laf:body>

            </c:if>
        </form:form>

    </laf:division>
</laf:box>
</body>
</html>
