<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html>
<head>
    <title>Set up Period ${period.name} of ${arm.qualifiedName} in ${study.name}</title>
    <tags:includeScriptaculous/>
    <script type="text/javascript">
        var activitiesByType = { }
        <c:forEach items="${activityTypes}" var="activityType">
        activitiesByType[${activityType.id}] = []
        </c:forEach>
        <c:forEach items="${activities}" var="activity">
        activitiesByType[${activity.type.id}].push({ name: '${activity.name}', id: ${activity.id} })
        </c:forEach>
        var initiallySelectedActivity = ${empty selectedActivity ? 0 : selectedActivity.id}

            function currentActivityCount() {
                return $$('.input-row').length;
            }

        function highlightNonZero(source) {
            var input;
            if (source.tagName) {
                input = source;
            } else {
                input = Event.findElement(source, "INPUT")
            }
            var cell = input.parentNode
//            var value = $F(input).strip()
            var nonzero;
//            if (value <= 0 || value.length == 0) {
//                Element.removeClassName(cell, "nonzero")
//            } else {
                Element.addClassName(cell, "nonzero")
//            }
        }

        function selectedActivity() {
            return selectedValue('add-activity')
        }

        function selectedValue(selectorName) {
            var selector = $(selectorName)
            var selected = selector.options[selector.selectedIndex]
            return {
                name: selected.text,
                id: selected.value
            }
        }

        function addActivityRow() {
            var activity = selectedActivity()
            var cells = []
            var dayCount = $$("#days-header th").length - 1
            var rowCount = $$("#input-body tr").length - 1
            // header
            var activityName = 'grid[' + rowCount + '].activity';
            var activityInput = Builder.node("input", { id: activityName, name: activityName, type: 'hidden', value: activity.id })
            cells.push(Builder.node('th', {className: 'activity'}, activity.name), [activityInput]);

            // input cells
            for (var i = 0; i < dayCount; i++) {
                var name = 'grid[' + rowCount + '].eventIds[' + i + ']'
                var namePlusOne = name+1
                var input = Builder.node('input', {
                                            type:'checkbox',
                                            id:namePlusOne,
                                            name:name,
                                            value:'-1'
                });
                registerCellInputHandlers(input)
                cells.push(Builder.node('td', {}, [input]))
            }
            var detailsName = 'grid[' + rowCount + '].details'
            var detailsInput = Builder.node('input', {
                                            id: detailsName,
                                            name: detailsName,
                                            type: 'text',
//                                            onChange:"return ajaxformOne(this);" });
                                            onChange:"return ajaxform(null, this);"});
            cells.push(Builder.node('td', {}, [detailsInput]))

            var rowId = 'activity' + activity.id;
            var row = Builder.node('tr', {className: 'input-row', id:rowId}, cells);
            row.style.display = 'none';
            $('input-body').appendChild(row)
            showEmptyMessage()
            SC.slideAndShow(rowId)
        }

        function updateActivitySelector() {
            var selector = $('add-activity')
            selector.options.length = 0
            var selectedTypeId = selectedValue('select-activity-type').id
            var activities = activitiesByType[selectedTypeId]
            activities.each(function(elt) {
                var opt = new Option(elt.name, elt.id)
                if (elt.id == initiallySelectedActivity) opt.selected = true
                selector.options[selector.options.length] = opt
            })
        }

        function showEmptyMessage() {
            if (currentActivityCount() > 0) {
                SC.slideAndHide('no-activities-message');
            } else {
                SC.slideAndShow('no-activities-message');
            }
        }

        function registerCellInputHandlers(input) {
            highlightNonZero(input);
            Event.observe(input, "click", function(e){return ajaxform(input, null)})
            Event.observe(input, "change", highlightNonZero)
            Event.observe(input, "keyup", highlightNonZero)
        }

        function parseInput(input) {
            var substring1 = input.substring(input.indexOf("[")+1, input.indexOf("]"));
            var substring2 = input.substring(input.indexOf("]") + 1);
            var substring3 = substring2.substring(substring2.indexOf("[")+1, substring2.indexOf("]"));
            var rowNumber = input.substring(input.length-1, input.length);

            var array = new Array();
            array[0] = substring1;
            array[1] = substring3;
            return array;
        }

        function parseDetailName(name) {
            return name.substring(name.indexOf("[")+1, name.indexOf("]"));

        }

        function ajaxform(checkbox, details) {
            console.log("Click= " + checkbox.name)
            // Set up data variable
            var formdata = "";
            formdata = formdata + 'id='+${period.id}+"&";
            var arrayOfIndexes
            if (checkbox != null) {
                arrayOfIndexes = parseInput(checkbox.name);
                var details = 'grid[' + arrayOfIndexes[0] + '].details';
                formdata = formdata + details + "=" + escape($(details).value) + "&";
                formdata = formdata + 'grid[' + arrayOfIndexes[0] + '].columnNumber'+ "=" + arrayOfIndexes[1] + "&";
                formdata = formdata + 'grid[' + arrayOfIndexes[0] + '].addition' + "=" + escape($(checkbox).checked) + "&";
            } else {
                arrayOfIndexes = parseInput($(details).name);
                formdata = formdata + $(details).name + "=" + escape($(details).value) + "&";
                formdata = formdata + 'grid[' + arrayOfIndexes[0] + '].addition' + "=" + escape(false) + "&";
                formdata = formdata + 'grid[' + arrayOfIndexes[0] + '].columnNumber'+ "=" + escape(-1) + "&";                
            }
            var activity = 'grid[' + arrayOfIndexes[0] + '].activity';
            formdata = formdata + activity + "=" + escape($(activity).value) +"&";

            var arrayOfCounts = 'grid[' + arrayOfIndexes[0] + '].eventIds';
            for (var i = 0; i < ${period.duration.days}; i++) {
                var singleElement = arrayOfCounts +'[' +i + ']'+1;
                if ($(singleElement) == null) {
                    singleElement = arrayOfCounts +'[' +i + ']';
                }
                 formdata = formdata + $(singleElement).name +  "=" +
                          $(singleElement).value + "&" ;
            }
            formdata = formdata + 'grid[' + arrayOfIndexes[0] + '].rowNumber'+ "=" + arrayOfIndexes[0] + "&";
            formdata = formdata + 'grid[' + arrayOfIndexes[0] + '].updated' + "=" + escape(true) + "&";

            var href = '<c:url value="/pages/managePeriod"/>'

            var lastRequest = new Ajax.Request(href,
                {
                    postBody: formdata
                });
            return true;
        }

        function registerHandlers() {
            $$('.input-row td.counter').each(function(cell) {
                var input = cell.getElementsByTagName("INPUT")[0]
                registerCellInputHandlers(input)
            });
            Event.observe('add-activity-button', 'click', addActivityRow)
            Event.observe('select-activity-type', 'change', updateActivitySelector)
        }

        Event.observe(window, "load", registerHandlers)
        Event.observe(window, "load", showEmptyMessage)
        Event.observe(window, "load", updateActivitySelector)
    </script>
    <style type="text/css">
        #no-activities-message td {
            text-align: center;
        }

        th.activity {
            text-align: right;
        }

        th.day-number {
            text-align: center;
            font-weight: normal;
        }

        .input-row td.nonzero, .input-row td.nonzero input {
            background-color: #9c9;
        }

        .input-row td {
            text-align: right;
        }

        .input-row td input {
            border-width: 0; /*border-bottom: 1px dotted #666;*/
            text-align: center;
            padding: 2px;
        }

        table {
            border-collapse: collapse;
        }

        td, th {
            border: 1px solid #666;
        }

        a#newActivityLink {
            padding-left: 2em
        }

        #activities-input label {
            font-weight: bold;
        }

        #activities-input {
            margin: 1em 0;
            background-color: #ddd;
            padding: 0.5em;
        }
    </style>
</head>
<body>
<h1>Set up ${period.name} (period) of ${arm.qualifiedName} in ${study.name}</h1>

<p>
    This period has ${period.duration.days} days and repeats ${commons:pluralize(period.repetitions, "time")}.
    It begins on day ${period.startDay} of the ${arm.qualifiedName}.  The numbers in the grid below
    show how many times each activity should be performed on each day of the period
    (most of the time this will be 1 or 0).
</p>

<form:form>
<c:set var="tableWidth" value="${period.duration.days + 2}"/>
<table>
    <tr>
        <td></td>
        <th colspan="${tableWidth - 2}">Days of arm (${commons:pluralize(period.repetitions, "repetition")})</th>
        <td></td>
    </tr>
    <tr id="days-header">
        <td></td>
        <c:forEach items="${period.dayRanges[0].days}" var="d">
            <th class="day-number">
            <c:forEach begin="0" end="${period.repetitions - 1}" var="x" varStatus="xStatus">
                ${d + x * period.duration.days}
                <c:if test="${not xStatus.last}"><br/></c:if>
            </c:forEach>
            </th>
        </c:forEach>
        <th>Details</th>
    </tr>
    <tbody id="input-body">
    <tr id="no-activities-message" style="display:none">
        <td></td>
        <td colspan="${tableWidth - 1}">This period does not have any activities yet</td>
    </tr>
    <c:forEach items="${command.grid}" var="gridRow" varStatus="gridStatus">
    <tr class="input-row">
        <th class="activity">
            ${gridRow.activity.name}
            <form:hidden path="grid[${gridStatus.index}].activity"/>
        </th>
        <c:forEach items="${gridRow.eventIds}" varStatus="cStatus">
            <td class="counter">
                <form:checkbox path="grid[${gridStatus.index}].eventIds[${cStatus.index}]"
                               value="${empty gridRow.eventIds[cStatus.index] ? -1 : gridRow.eventIds[cStatus.index]}"/>
            </td>
        </c:forEach>
        <td>
            <form:input path="grid[${gridStatus.index}].details"
                    onchange="ajaxform(null, this);" />
        </td>
    </tr>
    </c:forEach>
    </tbody>
</table>

<div id="activities-input">
    <label for="add-activity">Activities:</label>
    <select id="select-activity-type">
        <c:forEach items="${activityTypes}" var="activityType"><option value="${activityType.id}" <c:if test="${selectedActivity.type.id == activityType.id}">selected="selected"</c:if>>${activityType.name}</option></c:forEach>
    </select>
    <select id="add-activity">
        <option>Loading...</option>
    </select>
    <input type="button" id="add-activity-button" value="Add to period"/>
    <a id="newActivityLink" href="<c:url value="/pages/newActivity?returnToPeriodId=${period.id}"/>">Create new activity</a>
    <span class="tip">(Be sure to save your changes before leaving this page)</span>
</div>
</form:form>

</body>
</html>