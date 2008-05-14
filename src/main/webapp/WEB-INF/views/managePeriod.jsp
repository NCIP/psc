<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template" %>
<html>
<head>
<title>Set up ${period.displayName} of ${studySegment.qualifiedName} in ${study.name}</title>
<tags:includeScriptaculous/>
<tags:stylesheetLink name="main"/>
<script type="text/javascript">



function currentActivityCount() {
    return $$('.input-row').length;
}


function selectedActivity() {
    return selectedValue('add-activity')
}

function selectedValue(selectorName) {
    var input = $(selectorName)
    return {
        name: input.name,
        id: input.value
    }
}

function addActivityRow() {
    var activity = selectedActivity();
    if ((activity.id == null) || (activity.name == null)) return
    var cells = []
    var dayCount = getDayCount();
    var rowCount = $$("#input-body tr").length - 1
    // indicator
    var indicator = SC.activityIndicator('<c:url value="/"/>', 'row-' + rowCount + '-indicator')
    cells.push(Builder.node('td', { className: 'indicator-column' }, [ indicator ]))

    // header
    var activityName = 'grid[' + rowCount + '].activity';
    var activityInput = Builder.node("input", { id: activityName, name: activityName, type: 'hidden', value: activity.id })
    cells.push(Builder.node('th', {className: 'activity'}, activity.name), [activityInput]);

    // input cells
    for (var i = 0; i < dayCount.length; i++) {
        var value = dayCount[i];
        var name = 'grid[' + rowCount + '].plannedActivities[' + i + ']'
        var input = Builder.node('input', {
            id: name,
            name: name,
            type: 'hidden'
        });
        var marker = Builder.node('span', { className: 'marker' })
        cells.push(Builder.node('td', { className: 'counter' }, [ input, marker ]))
    }
    var detailsName = 'grid[' + rowCount + '].details'
    var detailsInput = Builder.node('input', {
        id: detailsName,
        name: detailsName,
        type: 'text'
    });
    cells.push(Builder.node('td', {}, [detailsInput]))

    // conditionalDetails
    var conditionDetailsName = 'grid[' + rowCount + '].conditionalDetails'
    var conditionDetailsInput = Builder.node('input', {
        id: conditionDetailsName,
        name: conditionDetailsName,
        type: 'text',
        className: 'no-condition',
        value: 'Click to add condition'
    });
    cells.push(Builder.node('td', {}, [conditionDetailsInput]))

    var rowId = 'activity' + activity.id;
    var row = Builder.node('tr', {className: 'input-row', id:rowId}, cells);

    row.style.display = 'none';
    $('input-body').appendChild(row)
    registerRowEventHandlers(row)
    registerDraggablesAndDroppables()
    showEmptyMessage()

    SC.slideAndShow(rowId)
}

function showEmptyMessage() {
    if (currentActivityCount() > 0) {
        $('no-activities-message').hide();
        $$('.activity-column').each(Element.show)
    } else {
        $('no-activities-message').show();
        $$('.activity-column').each(Element.hide)
    }
}

/* ***** CONDITION INPUT HANDLING */

function focusConditionInput(evt) {
    var input = evt.element('input')
    if (input.hasClassName('no-condition')) {
        input.value = ""
        input.removeClassName('no-condition')
    }
}

function changedConditionInput(evt) {
    var input = evt.element("input")
    executeUpdateDetails(input)
    if (input.value.blank()) {
        input.addClassName('no-condition')
        input.value = 'Click to add condition'
    } else {
        input.removeClassName('no-condition')
    }
}

/* ***** ASYNCHRONOUS UPDATERS */

// extracts row from an ID like grid[row].property
function extractRow(gridElementId) {
    return gridElementId.substring(gridElementId.indexOf("[") + 1, gridElementId.indexOf("]"))
}

// extracts row and column from an ID like grid[row].plannedActivities[col]
function extractRowAndColumn(gridElementId) {
    var row = extractRow(gridElementId);
    var plannedActivitiesPortion = gridElementId.substring(gridElementId.indexOf("]") + 1);
    var col = plannedActivitiesPortion.substring(plannedActivitiesPortion.indexOf("[") + 1, plannedActivitiesPortion.indexOf("]"));
    return [row, col];
}

function createBasicPostBody(row) {
    var data = { period: '${period.id}' }
    if (row) addParametersForRow(data, row)
    return data
}

function getDayCount() {
    var dayCount;
    //for week, month, fortnight, and quarter, the range is return a specific array of days,
    //f.e. for week it would be 1, 8, 15. But since on a grid the count starts from 0, we need to decrement actual day by one.
    if (${period.duration.unit == 'week'}) {
        dayCount =
    ${period.dayRanges[0].weeks}
        for (var i = 0; i < dayCount.length; i++) {
            dayCount[i] = dayCount[i] - 1;
        }
    } else if (${period.duration.unit == 'month'}) {
        dayCount =
    ${period.dayRanges[0].months}
        for (var i = 0; i < dayCount.length; i++) {
            dayCount[i] = dayCount[i] - 1;
        }
    } else if (${period.duration.unit == 'fortnight'}) {
        dayCount =
    ${period.dayRanges[0].fortnights}
        for (var i = 0; i < dayCount.length; i++) {
            dayCount[i] = dayCount[i] - 1;
        }
    } else if (${period.duration.unit == 'quarter'}) {
        dayCount =
    ${period.dayRanges[0].quarters}
        for (var i = 0; i < dayCount.length; i++) {
            dayCount[i] = dayCount[i] - 1;
        }
    } else if (${period.duration.unit =='day'}) {
        dayCount =
    ${period.dayRanges[0].days}
    }

    return dayCount;

}

function addParametersForRow(data, row) {
    data.rowNumber = row
    data.activity = $('grid[' + row + '].activity').value;
    data.details = $('grid[' + row + '].details').value;
    var conditionElement = $('grid[' + row + '].conditionalDetails')
    if (!conditionElement.hasClassName('no-condition')) {
        data.conditionalDetails = conditionElement.value;
    }
    var dayCount = getDayCount();
    for (var c = 0; c < dayCount.length; c++) {
        var val = $('grid[' + row + '].plannedActivities[' + c + ']')
        if (val != null) {
            var varValue = val.getAttribute('value')
            if (varValue && varValue != -1) {
                data['plannedActivities[' + c + ']'] = varValue;
            }
        }
    }
}

function executePlannedActivityAdd(targetId, populationId) {
    var rc = extractRowAndColumn(targetId)
    var href = '<c:url value="/pages/cal/managePeriod/addTo"/>'
    var data = createBasicPostBody(rc[0])
    data.columnNumber = rc[1]
    data.population = populationId

    executeManagePeriodPost(href, data, rc[0])
}

function executePlannedActivityRemove(targetId) {
    var rc = extractRowAndColumn(targetId)
    var href = '<c:url value="/pages/cal/managePeriod/remove"/>'
    var data = createBasicPostBody(rc[0])
    data.columnNumber = rc[1]

    executeManagePeriodPost(href, data, rc[0])
}

function executePlannedActivityMove(row, fromCol, toCol) {
    var href = '<c:url value="/pages/cal/managePeriod/move"/>';
    // Set up data variable
    var data = createBasicPostBody(row)
    var plannedActivities = 'grid[' + row + '].plannedActivities';

    var fromElementId = plannedActivities + '[' + fromCol + ']';
    if ($(fromElementId).getAttribute('value') != null) {
        data['plannedActivities[' + fromCol + ']'] = $(fromElementId).getAttribute('value');
    }

    data.columnNumber = toCol;
    data.moveFrom = fromCol;
    data.moveTo = toCol;

    executeManagePeriodPost(href, data, row)
}

function executeUpdateDetails(rowElement) {
    var row = extractRow(rowElement.id)
    var href = '<c:url value="/pages/cal/managePeriod/update"/>'
    var data = createBasicPostBody(row)

    executeManagePeriodPost(href, data, row)
}

function executeManagePeriodPost(href, data, row) {
    var indicator = $('row-' + row + '-indicator')
    indicator.reveal()
    new Ajax.Request(href, {
        postBody: Object.toQueryString(data),
        onComplete: function() {
            indicator.conceal()
        }
    });
}

function registerRowEventHandlers(rowElt) {
    var elt = $(rowElt).select('input[name*=conditionalDetails]');
    elt.each(function(input) {
        input.observe('focus', focusConditionInput)
        input.observe('blur', changedConditionInput)
    })

    rowElt.select('input[name*=details]').each(function(input) {
        input.observe('change', function(evt) {
            executeUpdateDetails(input)
        })
    })

    $$('.counter').each(function(input) {
        input.observe("click", function() {
            var spanElt = input.getElementsByClassName('marker')[0].innerHTML.toString()
            var inputElt = input.select("input")[0]
            var spanValue = spanElt.replace(/\s/g, "")
            if (spanValue.empty()) {
                executePlannedActivityAdd(inputElt.id, null)
                input.select(".marker")[0].innerHTML = 'X';
            }
        });

        input.observe('mouseover', function(){
            input.addClassName('hover')
        });

        input.observe('mouseout', function(){
            input.removeClassName('hover')
        });
    })

}

function updateAddActivityButton() {
    addActivityRow();
    resetActivitiesAutocompleter();
    resetActivityType();
}

function registerHandlers() {
    $$('.input-row').each(registerRowEventHandlers)
    Event.observe('return-to-template', 'click', function() {
        location.href = '<c:url value="/pages/cal/template?studySegment=${studySegment.id}&study=${study.id}&amendment=${study.developmentAmendment.id}"/>'
    })
}

function registerDraggablesAndDroppables() {
    $$('.new-marker').each(
            function(item) {
                new SC.Draggable(item, { revert: true });
            }
            );

    $$('.counter').each(
            function(item) {
                Droppables.add(item, {
                    accept: ['marker', 'new-marker'],
                    hoverclass: 'hover',
                    onDrop: moveMarker })
                    registerHoverTip(item, "Click the grid cell to add an activity for all subjects")
            }
            );

    $$('.marker').each(
            function(item) {
                new SC.Draggable(item, { revert: true });
                item.activity = item.up('tr').select('input[id*=activity]')[0].value;
            }
            );

    Droppables.add($('delete-drop'), { accept:'marker', hoverclass: 'hover', onDrop: deleteMarker })
}

function moveMarker(draggedMarker, dropZone) {
    var targetMarker = dropZone.select(".marker")[0]
    var gridCellId = dropZone.select("input")[0].id

    var plannedActivityElement = draggedMarker.up().select('input')[0]
    if (plannedActivityElement == null) {
        // means we are drugging a new event
        if (targetMarker.empty()) {
            var population = null
            if (draggedMarker.id.startsWith('population-marker-')) {
                population = draggedMarker.id.substr(18)
            }
            executePlannedActivityAdd(gridCellId, population)
            updateAddedMarker(draggedMarker, dropZone)
        }
    } else {
        //means we are moving event from one cell to another
        var plannedActivityElementId = plannedActivityElement.id
        var startRC = extractRowAndColumn(plannedActivityElementId)
        var endRC = extractRowAndColumn(gridCellId)
        if (startRC[0] == endRC[0]) {
            // need to set up ajax call for move
            if (targetMarker.empty()) {
                executePlannedActivityMove(startRC[0], startRC[1], endRC[1])
                updateMovedMarker(draggedMarker, dropZone)
            }
        }
    }
}

function updateAddedMarker(draggedMarker, dropZone) {
    var rowActivity = dropZone.up('tr').select('input[id*=activity]')[0].value
    var activity = draggedMarker.activity ? draggedMarker.activity : rowActivity;
    if (activity == rowActivity) {
        dropZone.select('.marker')[0].update(draggedMarker.innerHTML);

        //FOR IE7 - Have to comment this part, since X is not working after being added to the row once. The Draggables doesn't register clone properly...

        // Have to clone the node to avoid the revert animation
        //        var draggedMarkerParent = draggedMarker.parentNode
        //        draggedMarkerParent.removeChild(draggedMarker);
        //        if (draggedMarker.hasClassName('new-marker')) {
        //            var clone = $(draggedMarker.cloneNode(true))
        //            clone.style.position = ''
        //            clone.style.top = ''
        //            clone.style.left = ''
        //            clone.style.zIndex = ''
        //            clone.style.opacity = '0'
        //            new Draggable(clone, { revert: true })
        //            registerNewMarkerHoverTip(clone)
        //            draggedMarkerParent.appendChild(clone);
        //            new Effect.Opacity(clone, {
        //                duration: 0.8
        //            })
        //        }
    }
}

function updateMovedMarker(draggedMarker, dropZone) {
    var target = dropZone.select(".marker")[0];
    target.innerHTML = draggedMarker.innerHTML;
    draggedMarker.innerHTML = '';
}

function deleteMarker(draggedMarker, dropZone) {
    var element = draggedMarker.parentNode.getElementsBySelector("input")[0].id
    executePlannedActivityRemove(element)

    var prevActivity = draggedMarker.activity;
    draggedMarker.innerHTML = '';
}

function initMethods() {
    registerHandlers();
    showEmptyMessage();
    registerHoverTips();
    registerDraggablesAndDroppables();
    createAutocompleter();
    initSearchField();

}

Event.observe(window, "load", initMethods)
<%--</script>-->

<!--<!-- ////// ACTIVITIES AUTOCOMPLETER -->
<!--<script type="text/javascript">--%>
var activitiesAutocompleter;

function resetActivitiesAutocompleter() {
    activitiesAutocompleter.reset();
    $('add-activity').name = "";
    $('add-activity').value = "";
}

function resetActivityType() {
    $('activity-type-filter').options[0].selected = true;
}

function createAutocompleter() {
    activitiesAutocompleter = new Ajax.ResetableAutocompleter('activities-autocompleter-input', 'activities-autocompleter-div', '<c:url value="/pages/search/fragment/activities"/>',
    {
        method: 'get',
        paramName: 'searchText',
        callback: addAdditionalParameters,
        afterUpdateElement:updateActivity
    });
}

function addAdditionalParameters(inputField, queryString) {
    queryString = queryString + '&source=' + $('activity-sources-filter').value.escapeHTML();
    queryString = queryString + '&activityType=' + $('activity-type-filter').value.escapeHTML();
    return queryString;
}

function updateActivity(input, li) {
    $('add-activity').name = li.innerHTML;
    $('add-activity').value = li.id;
    updateAddActivityButton()
//    $('add-activity-button').focus()
}

function clearHoverTip() {
    $('palette-tip').innerHTML = '&nbsp;'
}

function registerHoverTip(element, message) {
    element.observe('mouseover', function() {
        $('palette-tip').innerHTML = message
    })
    element.observe('mouseout', clearHoverTip)
}

function registerNewMarkerHoverTip(marker) {
    var popName = marker.getAttribute("population-name");
    var message;
    if (popName) {
        message = "Drag into the grid to add an activity for the population " + popName
    } else {
        message = "Drag into the grid to add an activity for all subjects"
    }
    registerHoverTip(marker, message)
}

function registerHoverTips() {
    $$('.new-marker').each(registerNewMarkerHoverTip)
    registerHoverTip($('delete-drop'), "Drag an activity from the grid to delete it")
}

function addANewActivityToTheRow() {
    $('add-activity').name = '${selectedActivity.name}';
    $('add-activity').value = '${selectedActivity.id}';
    addActivityRow()
}
<c:if test="${selectedActivity !=null && !empty selectedActivity}">
Event.observe(window, 'load', addANewActivityToTheRow)
        </c:if>
    //Event.observe(window, "dom:loaded", registerHoverTips)

</script>

<style type="text/css">
#no-activities-message td {
    text-align: left;
    padding-left: 2em;
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
    text-align: center;
}

.input-row td span {
    cursor: move;
}

.input-row td input {
    border-width: 0;
    border-bottom: 1px dotted #666;
    text-align: center;
    padding: 2px;
    background-color: transparent;
}

.input-row td input[disabled] {
    background-color: #ddc; /* from ctms-laf fields.css */
    border-color: #bba;
}

.input-row .no-condition {
    color: #666;
    font-style: italic;
}

table {
    border-collapse: collapse;
}

td, th {
    border: 1px solid #666;
    text-align: center;
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

#gridTable  div {
    float: left;
    margin-left: 1%;
    margin-top: 1%;
    padding: 10px;
}

#revision-changes {
    float: right;
    width: 29%;
}

#with-changes #period {
    width: 70%;
    float: left;
}

#new-activities-link-separator {
    padding: 0 1em 0 1em;
}

div.autocomplete {
    position: absolute;
    width: 400px;
    background-color: white;
    border: 1px solid #ccc;
    margin: 0;
    padding: 0;
    font-size: 0.8em;
    text-align: left;
    max-height: 200px;
    overflow: auto;
}

div.autocomplete ul {
    list-style-type: none;
    margin: 0;
    padding: 0;
}

div.autocomplete ul li.selected {
    background-color: #EAF2FB;
}

div.autocomplete ul li {
    list-style-type: none;
    display: block;
    margin: 0;
    padding: 2px;
    cursor: pointer;
}

table#manage-period {
    padding: 5px; /*overflow-x:auto;*/
    display: block;
}

.indicator-column {
    border-style: none;
}

.palette {
/*commening the posititon attribute, as it was causing the misplacing of the droppables on IE7*/
/*position: relative;*/
    padding-left: 12em;
}

.palette .header {
    line-height: 100%;
    text-align: left;
}

.palette div.well {
    width: 50px;
    text-align: center;
    margin: 2px 10px 10px 0px;
    float: left
}

.palette .new-marker {
    height: 50px;
    vertical-align: middle;
    line-height: 50px;
    cursor: move;
}

.palette div#delete-drop {
    z-index: 1;
    margin-left: 25px
}

.palette div#delete-drop p {
    height: 40px;
    padding: 0;
    margin: 5px;
    font-size: 120%;
}

.palette div#delete-drop.hover {
    background-color: #f99;
    cursor: pointer;
}

.counter.hover {
    background-color: #f99;
}

.palette .tip {
    clear: both;
}
</style>
</head>
<body>

<c:set var="showChanges" value="${not empty developmentRevision and not study.inInitialDevelopment}"/>

<c:if test="${showChanges}">
<div id="with-changes">
<div id="revision-changes" class="section">
    <templ:changes revision="${developmentRevision}" changes="${revisionChanges}"/>
</div>
    <%-- #with-changes is closed below --%>
</c:if>

<div id="period" class="section">
<laf:box title="Set up ${period.displayName} of ${studySegment.qualifiedName} in ${study.assignedIdentifier}">
<laf:division>
<p>
    This period has ${period.duration.days} days and repeats ${commons:pluralize(period.repetitions, "time")}.
    It begins on day ${period.startDay} of ${studySegment.qualifiedName}.
</p>

<div class="palette autoclear">
    <div class="well card" id="add-marker-area">
        <div class="header" title="Add an activity for all subjects">Add</div>
        <div class="new-marker">X</div>
    </div>

    <c:forEach items="${study.populations}" var="pop">
        <div class="well card" title="Add an activity for subjects in the ${pop.name} population">
            <div class="header">Add</div>
            <div class="new-marker" id="population-marker-${pop.id}"
                 population-name="${pop.name}">${pop.abbreviation}</div>
        </div>
    </c:forEach>

    <div class="well card" id="delete-drop">
        <div class="header">Delete</div>
        <p>Drop Here</p>
    </div>

    <p class="tip" id="palette-tip">&nbsp;</p>
</div>

<form:form>

<div id="newDiv" style="overflow:auto; padding-bottom:5px; position:relative;">
    <%--<c:set var="tableWidth" value="${period.duration.days + 4}"/>--%>
<c:set var="range" value="0"/>
<c:if test="${period.duration.unit == 'week'}">
    <c:set var="range" value="${period.dayRanges[0].weeks}"/>
</c:if>
<c:if test="${period.duration.unit == 'month'}">
    <c:set var="range" value="${period.dayRanges[0].months}"/>
</c:if>
<c:if test="${period.duration.unit == 'fortnight'}">
    <c:set var="range" value="${period.dayRanges[0].fortnights}"/>
</c:if>
<c:if test="${period.duration.unit == 'quarter'}">
    <c:set var="range" value="${period.dayRanges[0].quarters}"/>
</c:if>
<c:set var="tableWidth" value="${period.duration.quantity}"/>
<c:if test="${period.duration.unit =='day'}">
    <c:set var="range" value="${period.dayRanges[0].days}"/>
    <c:set var="tableWidth" value="${period.duration.days}"/>
</c:if>
<c:set var="tableWidth" value="${tableWidth +4}"/>

<table id="manage-period">
<tbody id="input-body">
<tr>
    <th class="indicator-column spacer"></th>
    <th class="activity-column spacer"><!-- Spacer --></th>
    <th colspan="${tableWidth - 4}">Days of segment (${commons:pluralize(period.repetitions, "repetition")}
        of ${commons:pluralize(period.duration.quantity, period.duration.unit)})
    </th>
    <th colspan="2">Notes</th>
</tr>
<tr id="days-header">
    <th class="indicator-column spacer"></th>
    <th class="activity-column spacer"><!-- Spacer --></th>
    <c:set var="MAX_REPETITION_SIZE" value="4"/>
    <c:set var="MAX_REPETITIONS_DISPLAYED_WHEN_COMPRESSED" value="2"/>
    <c:set var="compressPeriod" value="${period.repetitions gt MAX_REPETITION_SIZE}"/>
    <c:set var="counter" value="1"/>
    <c:forEach items="${range}" var="d">
        <th id="day-number" class="day-number">
                ${counter} ${fn:substring(period.duration.unit, 0, 1)} <br>
            <c:forEach begin="0" end="${period.repetitions - 1}" var="x" varStatus="xStatus">
                <c:set var="visibleRow"
                       value="${x lt MAX_REPETITIONS_DISPLAYED_WHEN_COMPRESSED or not compressPeriod}"/>
                <c:set var="showCompressionRow" value="${x eq (MAX_REPETITIONS_DISPLAYED_WHEN_COMPRESSED)}"/>

                <c:if test="${visibleRow or xStatus.last}">
                    ${d + x * period.duration.days}
                </c:if>

                <c:if test="${not visibleRow and showCompressionRow and not xStatus.last}">
                    &hellip;
                </c:if>

                <c:if test="${not xStatus.last and (visibleRow or showCompressionRow)}"><br/></c:if>
            </c:forEach>
            <c:set var="counter" value="${counter +1}"/>
        </th>
    </c:forEach>
    <th>Details</th>
    <th>Condition</th>
</tr>

<tr id="no-activities-message" style="display: none">
    <th class="indicator-column spacer"></th>
    <th class="activity-column spacer"><!-- Spacer --></th>
    <td colspan="${tableWidth - 4}">This period does not have any activities yet</td>
    <th colspan="2" class="spacer"></th>
</tr>
<c:forEach items="${command.grid}" var="gridRow" varStatus="gridStatus">
    <tr class="input-row">
        <td class="indicator-column">
            <tags:activityIndicator id="row-${gridStatus.index}-indicator"/>
        </td>
        <th class="activity">
                ${gridRow.activity.name}
            <form:hidden path="grid[${gridStatus.index}].activity"/>
        </th>
        <c:choose>
            <c:when test="${period.duration.unit == 'day'}">
                <c:forEach items="${gridRow.plannedActivities}" varStatus="cStatus">
                    <td class="counter">
                        <form:hidden path="grid[${gridStatus.index}].plannedActivities[${cStatus.index}]"/>
                                            <span class="marker">
                                                <c:if test="${not empty command.grid[gridStatus.index].plannedActivities[cStatus.index]}">
                                                    ${empty command.grid[gridStatus.index].plannedActivities[cStatus.index].population
                                                            ? 'X'
                                                            : command.grid[gridStatus.index].plannedActivities[cStatus.index].population.abbreviation }
                                                </c:if>
                                            </span>
                    </td>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <c:set var="count" value="0"/>
                <c:forEach items="${range}" var="rangeVar" varStatus="rangeStatus">
                    <td class="counter">
                        <form:hidden path="grid[${gridStatus.index}].plannedActivities[${count}]"/>
                                                <span class="marker">
                                                    <c:if test="${not empty command.grid[gridStatus.index].plannedActivities[count]}">
                                                        ${empty command.grid[gridStatus.index].plannedActivities[count].population
                                                                ? 'X'
                                                                : command.grid[gridStatus.index].plannedActivities[count].population.abbreviation }
                                                    </c:if>
                                                </span>
                    </td>
                    <c:set var="count" value="${count+1}"/>
                </c:forEach>

            </c:otherwise>

        </c:choose>
        <td>
            <form:input path="grid[${gridStatus.index}].details"/>
        </td>
        <td>
            <input name="grid[${gridStatus.index}].conditionalDetails"
                   id="grid[${gridStatus.index}].conditionalDetails"
                   class="${empty gridRow.conditionalDetails ? 'no-condition' : ''}"
                   value="${empty gridRow.conditionalDetails ? 'Click to add condition' : gridRow.conditionalDetails}"
                    />
        </td>
    </tr>
</c:forEach>
</tbody>
</table>
</div>
<br style="clear:both"/>

<div id="activities-input">
    <label for="add-activity">Activities:</label>
    <select id="activity-sources-filter">
        <option value="">All sources</option>
        <c:forEach items="${sources}" var="source">
            <option value="${source.id}">${source.name}</option>
        </c:forEach>
    </select>
    <select id="activity-type-filter">
        <option value="">All types</option>
        <c:forEach items="${activityTypes}" var="activityType">
            <option value="${activityType.id}">${activityType.name}</option>
        </c:forEach>
    </select>
    <input id="activities-autocompleter-input" type="text" autocomplete="off" class="autocomplete"
           value="Search for activity"/>

    <div id="activities-autocompleter-div" class="autocomplete"></div>


    <input type="hidden" id="add-activity"/>

    <a id="newActivityLink" href="<c:url value="/pages/newActivity?returnToPeriodId=${period.id}"/>">Create new
        activity</a> <span id="new-activities-link-separator">or</span>
    <a id="importActivitiesLink" href="<c:url value="/pages/cal/import/activities?returnToPeriodId=${period.id}"/>">Import
        activities from xml</a>
</div>

<input id="return-to-template" type="button" value="Return to template"/>
</form:form>
</laf:division>
</laf:box>

</div>

<c:if test="${showChanges}"></div>
</c:if>


</body>
</html>