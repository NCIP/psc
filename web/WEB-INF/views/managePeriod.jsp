<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
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
            var value = $F(input).strip()
            var nonzero;
            if (value <= 0 || value.length == 0) {
                Element.removeClassName(cell, "nonzero")
            } else {
                Element.addClassName(cell, "nonzero")
            }
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
                var name = 'grid[' + rowCount + '].counts[' + i + ']'
                var input = Builder.node('input', { id: name, name: name, 'class': 'counter', size: 2, type: 'text', value: 0 })
                registerCellInputHandlers(input)
                cells.push(Builder.node('td', {}, [input]))
            }
            var detailsName = 'grid[' + rowCount + '].details'
            var detailsInput = Builder.node('input', { id: detailsName, name: detailsName, type: 'text' });
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
            Event.observe(input, "change", highlightNonZero)
            Event.observe(input, "keyup", highlightNonZero)
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
            text-align: right;
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
    This period has ${period.duration.days} days and repeats ${period.repetitions} times.  It begins on
    day ${period.startDay} of the epoch.  The numbers in the grid below show how many times each
    activity should be performed on each day of the period (most of the time this will be 1 or 0).
</p>

<form:form>
<c:set var="tableWidth" value="${period.duration.days + 2}"/>
<table>
    <tr>
        <td></td>
        <th colspan="${tableWidth - 2}">Days of epoch (${period.repetitions} repetitions)</th>
        <td></td>
    </tr>
    <tr id="days-header">
        <td></td>
        <c:forEach begin="${period.startDay}" end="${period.startDay + period.duration.days - 1}" var="d">
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
        <c:forEach items="${gridRow.counts}" var="count" varStatus="cStatus">
            <td class="counter"><form:input path="grid[${gridStatus.index}].counts[${cStatus.index}]" size="2"/></td>
        </c:forEach>
        <td>
            <form:input path="grid[${gridStatus.index}].details"/>
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
</div>


<p class="submit">
    <input type="submit" value="Save Changes"/>
</p>
</form:form>

</body>
</html>