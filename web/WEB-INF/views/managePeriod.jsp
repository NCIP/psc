<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<html>
<head>
    <title>Set up Period ${period.name} of ${study.name}</title>
    <tags:includeScriptaculous/>
    <script type="text/javascript">
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
            if ($F(input) > 0) {
                Element.addClassName(cell, "nonzero")
            } else {
                Element.removeClassName(cell, "nonzero")
            }
        }

        function selectedActivity() {
            var selector = $('add-activity')
            var selected = selector.options[selector.selectedIndex]
            return {
                name: selected.text,
                id: selected.value
            }
        }

        function addActivityRow() {
            var activity = selectedActivity()
            var cells = []
            // header
            cells.push(Builder.node('th', {className: 'activity'}, activity.name));
            // input cells
            var dayCount = $$("#days-header th").length
            for (var i = 0 ; i < dayCount ; i++) {
                var name = 'grid[' + activity.id + '][' + i + ']'
                var input = Builder.node('input', { id: name, name: name, size: 2, type: 'text', value: 0 })
                registerCellInputHandlers(input)
                cells.push(Builder.node('td', {}, [input]))
            }
            var rowId = 'activity' + activity.id;
            var row = Builder.node('tr', {className: 'input-row', id:rowId}, cells);
            row.style.display = 'none';
            $('input-body').appendChild(row)
            showEmptyMessage()
            SC.slideAndShow(rowId)
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
            $$('.input-row td').each( function(cell) {
                var input = cell.getElementsByTagName("INPUT")[0]
                registerCellInputHandlers(input)
            });
            Event.observe('add-activity-button', "click", addActivityRow)
        }

        Event.observe(window, "load", registerHandlers)
        Event.observe(window, "load", showEmptyMessage)
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
            border-width: 0;
            /*border-bottom: 1px dotted #666;*/
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
<h1>Set up ${period.name} (period) of ${study.name}</h1>

<p>
    This period has ${period.duration.days} days and repeats ${period.repetitions} times.  It begins on
    day ${period.startDay} of the epoch.  The numbers in the grid below show how many times each
    activity should be performed on each day of the period (most of the time this will be 1 or 0).
</p>

<form:form>
<c:set var="tableWidth" value="${period.duration.days + 1}"/>
<table>
    <tr>
        <td></td>
        <th colspan="${tableWidth - 1}">Days of epoch (${period.repetitions} repetitions)</th>
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
    </tr>
    <tbody id="input-body">
    <tr id="no-activities-message" style="display:none">
        <td></td>
        <td colspan="${tableWidth - 1}">This period does not have any activities yet</td>
    </tr>
    <c:forEach items="${command.grid}" var="entry">
    <tr class="input-row">
        <th class="activity">${activitiesById[entry.key].name}</th>
        <c:forEach items="${entry.value}" var="count" varStatus="cStatus">
            <td><form:input path="grid[${entry.key}][${cStatus.index}]" size="2"/></td>
        </c:forEach>
    </tr>
    </c:forEach>
    </tbody>
</table>

<div id="activities-input">
    <label>Activities:
    <select id="add-activity">
        <c:forEach items="${activities}" var="activity"><option value="${activity.id}">${activity.name}</option></c:forEach>
    </select>
    </label>
    <input type="button" id="add-activity-button" value="Add to period"/>
    <a id="newActivityLink" href="<c:url value="/pages/newActivity"/>">Create new activity</a>
</div>


<p class="submit">
    <input type="submit" value="Save Changes"/>
</p>
</form:form>

</body>
</html>