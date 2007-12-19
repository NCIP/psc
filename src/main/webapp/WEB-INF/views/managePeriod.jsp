<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
<html>
<head>
<title>Set up ${period.displayName} of ${studySegment.qualifiedName} in ${study.name}</title>
<tags:includeScriptaculous/>
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
    var activity = selectedActivity()
    if (!activity.id || !activity.name) return
    var cells = []
    var dayCount = ${period.dayRanges[0].days}.length
    var rowCount = $$("#input-body tr").length - 1
    // header
    var activityName = 'grid[' + rowCount + '].activity';
    var activityInput = Builder.node("input", { id: activityName, name: activityName, type: 'hidden', value: activity.id })
    cells.push(Builder.node('th', {className: 'activity'}, activity.name), [activityInput]);

    // input cells
    for (var i = 0; i < dayCount; i++) {
        var name = 'grid[' + rowCount + '].eventIds[' + i + ']'
        var input = Builder.node('span', {
            id:name,
            name:name,
            class: "marker",
            value: -1
        });
        cells.push(Builder.node('td', {class:'counter', style:'position: relative;'}, [input]))
    }
    var detailsName = 'grid[' + rowCount + '].details'
    var detailsInput = Builder.node('input', {
        id: detailsName,
        name: detailsName,
        type: 'text',
        onChange:"return ajaxform(null, null, null,  this, null);"
    });
    cells.push(Builder.node('td', {}, [detailsInput]))

    // corresponding to the conditional behavior
    cells.push(Builder.node('td', {className:'emptyCell'}));

    // condition checkbox
    var name = 'grid[' + rowCount + '].conditionalCheckbox'
    var namePlusOne = name + 1
    var input = Builder.node('input', {
        type: 'checkbox',
        id: namePlusOne,
        name: name,
        value: 'true'
    });
    registerCellInputHandlers(input)
    cells.push(Builder.node('td', {}, [input]))

    // conditionalDetails
    var conditionDetailsName = 'grid[' + rowCount + '].conditionalDetails'
    var conditionDetailsInput = Builder.node('input', {
        id: conditionDetailsName,
        name: conditionDetailsName,
        type: 'text',
        disabled: 'true',
        onChange:"return ajaxform(null, null, null, null, this);"});
    cells.push(Builder.node('td', {}, [conditionDetailsInput]))

    var rowId = 'activity' + activity.id;
    var row = Builder.node('tr', {className: 'input-row', id:rowId}, cells);

    row.style.display = 'none';
    $('input-body').appendChild(row)
    registerDraggablesAndDroppables()
    showEmptyMessage()

    // displaying the very fist empty column, that is not shown when no activities is present
    if (currentActivityCount() > 0) {
        var td1Element = document.getElementById("td1")
        var td2Element = document.getElementById("td2")

        td1Element.style.display = 'table-cell';
        td2Element.style.display = 'table-cell';
    }
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
    Event.observe(input, "click", function(e) {
        return ajaxform(input, null, null, null, null)
    })
}

// extracts row and column from an ID like grid[row].eventIds[col]
function extractRowAndColumn(gridElementId) {
    var row = gridElementId.substring(gridElementId.indexOf("[") + 1, gridElementId.indexOf("]"));
    var eventIdsPortion = gridElementId.substring(gridElementId.indexOf("]") + 1);
    var col = eventIdsPortion.substring(eventIdsPortion.indexOf("[") + 1, eventIdsPortion.indexOf("]"));

    return [row, col];
}

function parseDetailName(name) {
    return name.substring(name.indexOf("[") + 1, name.indexOf("]"));

}

function getInfoFromConditionalDetails(formdata, conditionalDetails, index) {
    formdata = formdata + 'conditionalUpdated' + "=" + escape(true) + "&";
    formdata = formdata + 'addition' + "=" + escape(false) + "&";
    return formdata
}

function getInfoFromConditionalCheckbox(formdata, conditionalCheckbox, index) {
    var checkboxName = 'grid[' + index + '].conditionalCheckbox1';
    var isChecked = $(checkboxName).checked;
    var details = 'grid[' + index + '].conditionalDetails';
    if (!isChecked) {
        $(details).value = "";
        $(details).disabled = true;
    } else {
        $(details).disabled = false;
    }

    formdata = formdata + 'conditionalUpdated' + "=" + escape(true) + "&";
    formdata = formdata + 'addition' + "=" + escape(false) + "&";
    return formdata
}

function getInfoFromEventCheckbox(formdata, checkbox, index1, index2) {
    var details = 'grid[' + index1 + '].details';
    formdata = formdata + "details=" + escape($(details).value) + "&";
    formdata = formdata + 'columnNumber' + "=" + index2 + "&";
    formdata = formdata + 'addition' + "=" + escape($(checkbox).checked) + "&";
    formdata = formdata + 'conditionalUpdated' + "=" + escape(false) + "&";
    return formdata;
}

function getInfoFromEventDetails(formdata, details, index) {
    formdata = formdata + "details=" + escape($(details).value) + "&";
    formdata = formdata + 'addition' + "=" + escape(false) + "&";
    formdata = formdata + 'columnNumber' + "=" + escape(-1) + "&";
    formdata = formdata + 'conditionalUpdated' + "=" + escape(false) + "&";
    return formdata;
}

function getInfoFromEventTextbox(formdata, isAdding, index1, index2) {
    var details = 'grid[' + index1 + '].details';
    formdata = formdata + "details=" + escape($(details).value) + "&";
    formdata = formdata + 'columnNumber' + "=" + index2 + "&";
    formdata = formdata + 'addition' + "=" + isAdding + "&";
    formdata = formdata + 'conditionalUpdated' + "=" + escape(false) + "&";
    return formdata;
}

function createAddControl(text, objectType, objectId) {
    return createAjaxBaseHref('<c:url value="/pages/cal/managePeriod/addTo"/>')
}

function createRemoveControl(text, objectType, objectId) {
    return createAjaxBaseHref('<c:url value="/pages/cal/managePeriod/remove"/>')
}


function updateDetails(text, objectType, objectId) {
    return createAjaxBaseHref('<c:url value="/pages/cal/managePeriod/update"/>')
}

function move(text, objectType, objectId) {
    return createAjaxBaseHref('<c:url value="/pages/cal/managePeriod/move"/>')
}

function createAjaxBaseHref(baseHref) {
    var href = baseHref;
    if (href.indexOf('?') >= 0) {
        href += '&'
    } else {
        href += '?'
    }
    return href
}

function moveAjaxForm(fromRC, toRC) {
    var href = move("Move event", "event ", toRC[0]);
    // Set up data variable
    var formdata = "";
    formdata = formdata + 'id='+${period.id}+"&";
    formdata = getInfoFromEventTextbox(formdata, false, toRC[0], toRC[1])

    var activity = 'grid[' + toRC[0] + '].activity';
    formdata = formdata + "activity=" + escape($(activity).value) + "&";
    var arrayOfCounts = 'grid[' + toRC[0] + '].eventIds';

    var singleElement = arrayOfCounts + '[' + fromRC[1] + ']';
    if ($(singleElement).getAttribute('value') != null) {
        formdata = formdata + 'eventIds' + '[' + fromRC[1] + ']' + "=" + $(singleElement).getAttribute('value') + "&";
    }
    formdata = formdata + 'rowNumber' + "=" + toRC[0] + "&";
    formdata = formdata + 'updated' + "=" + escape(true) + "&";

    var checkboxName = 'grid[' + toRC[0] + '].conditionalCheckbox1';
    formdata = formdata + "conditionalCheckbox1=" + $(checkboxName).checked + "&";
    var details1 = 'grid[' + toRC[0] + '].conditionalDetails';
    formdata = formdata + "conditionalDetails=" + $(details1).value + "&";

    formdata = formdata + "moveFrom=" + fromRC[1] + "&";
    formdata = formdata + "moveTo=" + toRC[1] + "&";

    var lastRequest = new Ajax.Request(href,
    {
        postBody: formdata
    });

    return true;


}

function ajaxform(checkbox, textbox, isAdd, details, conditionalDetails) {
    var href;

    // Set up data variable
    var formdata = "";
    formdata = formdata + 'id='+${period.id}+"&";
    var rc

    if (checkbox != null) {
        rc = extractRowAndColumn(checkbox.name)
        if (checkbox.name.indexOf(".conditionalCheckbox") >= 0) {
            formdata = getInfoFromConditionalCheckbox(formdata, checkbox, rc[0])
            href = updateDetails("Update details", "details", rc[0]);
        } else {
            formdata = getInfoFromEventCheckbox(formdata, checkbox, rc[0], rc[1])
            if ($(checkbox).checked) {
                href = createAddControl("Add checkbox", "checkbox", rc[0])
            } else {
                href = createRemoveControl("Remove checkbox", "checkbox", rc[0])
            }
        }

    } else if (details != null) {
        rc = extractRowAndColumn($(details).name);
        formdata = getInfoFromEventDetails(formdata, details, rc[0]);
        href = updateDetails("Update details", "details", rc[0]);
    } else if (conditionalDetails != null) {
        rc = extractRowAndColumn(conditionalDetails.name)
        formdata = getInfoFromConditionalDetails(formdata, conditionalDetails, rc[0])
        href = updateDetails("Update details", "details", rc[0]);
    } else if (textbox != null) {
        rc = extractRowAndColumn(textbox)
        formdata = getInfoFromEventTextbox(formdata, isAdd, rc[0], rc[1])
        if (isAdd) {
            href = createAddControl("Add checkbox", "checkbox", rc[0])
        } else {
            href = createRemoveControl("Remove checkbox", "checkbox", rc[0])
        }
    }

    var activity = 'grid[' + rc[0] + '].activity';
    formdata = formdata + "activity=" + escape($(activity).value) + "&";

    var arrayOfCounts = 'grid[' + rc[0] + '].eventIds';
    for (var i = 0; i < ${period.duration.days}; i++) {
        var singleElement = arrayOfCounts + '[' + i + ']';
        if ($(singleElement).getAttribute('value') == null) {
            formdata = formdata + 'eventIds' + '[' + i + ']' + "=-1" + "&";
        } else {
            formdata = formdata + 'eventIds' + '[' + i + ']' + "=" + $(singleElement).getAttribute('value') + "&";
        }
    }

    formdata = formdata + 'rowNumber' + "=" + rc[0] + "&";
    formdata = formdata + 'updated' + "=" + escape(true) + "&";


    var checkboxName = 'grid[' + rc[0] + '].conditionalCheckbox1';
    formdata = formdata + "conditionalCheckbox1=" + $(checkboxName).checked + "&";

    var details1 = 'grid[' + rc[0] + '].conditionalDetails';
    formdata = formdata + "conditionalDetails=" + $(details1).value + "&";

    var lastRequest = new Ajax.Request(href,
    {
        postBody: formdata
    });

    return true;
}

function registerHandlers() {
    $$('.input-row td.conditional').each(function(cell) {
        var input = cell.getElementsByTagName("INPUT")[0]
        registerCellInputHandlers(input)
    });
    Event.observe('add-activity-button', 'click', addActivityRow)
    Event.observe('add-activity-button', 'click', resetActivitiesAutocompleter)
    Event.observe('return-to-template', 'click', function() {
        location.href = '<c:url value="/pages/cal/template?studySegment=${studySegment.id}&study=${study.id}&amendment=${study.developmentAmendment.id}"/>'
    })
}

function makeCellDraggableAndDroppable(input) {
    document.getElementsByClassName('marker').each(
        function(item) {
            new Draggable(item, {revert: true});
            item.currentDurationIndex = item.parentNode.durationIndex;
            item.activity = item.parentNode.parentNode.getElementsByClassName('activity')[0].innerHTML.strip();
        }
        );

    Droppables.add($('deleteDrop'), {accept:'marker',hoverclass: 'hoverActive',onDrop:deleteEvent})
}

function registerDraggablesAndDroppables() {
    document.getElementsByClassName('newMarker').each(
        function(item) {
            new Draggable(item, {revert: true});
        }
        );

    var x = 0;
    document.getElementsByClassName('counter').each(
        function(item) {
            Droppables.add(item, {
                accept:['marker','newMarker'],
                hoverclass: 'hoverActive',
                onDrop: moveEvent })
            item.durationIndex = x;
            x = (x == PERIOD_DURATION - 1) ? 0 : (x + 1);
        }
        );

    document.getElementsByClassName('marker').each(
        function(item) {
            new Draggable(item, {revert: true});
            item.currentDurationIndex = item.parentNode.durationIndex;
            item.activity = item.parentNode.parentNode.getElementsByClassName('activity')[0].innerHTML.strip();
        }
        );

    Droppables.add($('deleteDrop'), {accept:'marker',hoverclass: 'hoverActive',onDrop:deleteEvent})
}


var PERIOD_DURATION = 7;

function moveEvent(draggable, dropZone) {
    var wholeElement = dropZone.getElementsBySelector("span")[0]
    var elementId = dropZone.getElementsBySelector("span")[0].id

    var parentElement = draggable.parentNode.getElementsBySelector("span")[0]
    if (parentElement == null) {
        //means we are drugging a new event
        if (wholeElement.firstChild == null) {
            ajaxform(null, elementId, true, null, null)
            setUpMarker(draggable, dropZone)
        }
    } else {
        //means we are moving event from one cell to another
        var parentElementId = parentElement.id
        var parentRC = extractRowAndColumn(parentElementId)
        var childRC = extractRowAndColumn(elementId)
        if (parentRC[0] == childRC[0]) {
            //need to set up ajax call for move
            if (wholeElement.firstChild == null) {
                moveAjaxForm(parentRC, childRC)
                setUpMovingMarker(draggable, dropZone)
            }
        }
    }
}


function setUpMarker(draggable, dropZone) {
    var prevDurationIndex = draggable.currentDurationIndex;
    var newDurationIndex = dropZone.durationIndex;
    var activity = (typeof(draggable.activity) != 'undefined') ? draggable.activity : dropZone.parentNode.getElementsByClassName('activity')[0].innerHTML.strip();
    if (prevDurationIndex != newDurationIndex && activity == dropZone.parentNode.getElementsByClassName('activity')[0].innerHTML.strip()) {
        var marker = createMarker(newDurationIndex, activity);
        dropZone.appendChild(marker);

        draggable.parentNode.removeChild(draggable);
        if (draggable.className == 'newMarker') {
            var div = Builder.node("div", { className: 'newMarker' })
            div.innerHTML = 'X';
            new Draggable(div, { revert: true });
            $('newMarkerArea').appendChild(div);
        }
    }
}


function setUpMovingMarker(draggable, dropZone) {
    draggable.innerHTML = '';
    var element = dropZone.getElementsBySelector("span")[0];
    element.innerHTML = 'X';
}

function deleteEvent(draggable, dropZone) {
    var element = draggable.parentNode.getElementsBySelector("span")[0].id
    ajaxform(null, element, false, null, null)

    var prevDurationIndex = draggable.currentDurationIndex;
    var prevActivity = draggable.activity;
    draggable.innerHTML = '';
}


function createMarker(currentDurationIndex, activityName) {
    var marker = document.createElement('span');
    marker.innerHTML = 'X';
    marker.className = 'marker';
    marker.currentDurationIndex = currentDurationIndex;
    marker.activity = activityName;
    new Draggable(marker, {revert: true});
    return marker;
}


Event.observe(window, "load", registerHandlers)
Event.observe(window, "load", showEmptyMessage)
Event.observe(window, "load", registerDraggablesAndDroppables)

</script>

<!-- ////// ACTIVITIES AUTOCOMPLETER -->
<script type="text/javascript">
var activitiesAutocompleter;

function resetActivitiesAutocompleter() {
    activitiesAutocompleter.reset();
    $('add-activity').name = "";
    $('add-activity').value = "";
}

function createAutocompleter() {
    activitiesAutocompleter = new Ajax.RevertableAutocompleter('activities-autocompleter-input', 'activities-autocompleter-div', '<c:url value="/pages/fragment/search/activities"/>',
    { method: 'get', paramName: 'searchText', callback: addAdditionalParameters, afterUpdateElement:updateActivity, revertOnEsc:true});
}

function addAdditionalParameters(inputField, queryString) {
    queryString = queryString + '&source=' + $('activity-sources-filter').value.escapeHTML();
    queryString = queryString + '&activityType=' + $('activity-type-filter').value.escapeHTML();
    return queryString;
}

function updateActivity(input, li) {
    $('add-activity').name = li.innerHTML;
    $('add-activity').value= li.id;
    $('add-activity-button').focus()
}

/*
   Replicated a lot of functions from the superclass because in protype 1.5, if you override a function
   child function is not given access to the parent function.  In protype 1.6 you are.
*/
Ajax.RevertableAutocompleter = Class.create();
Object.extend(Object.extend(Ajax.RevertableAutocompleter.prototype, Ajax.Autocompleter.prototype), {
    initialize: function(element, update, url, options) {
        this.baseInitialize(element, update, options);
        this.options.asynchronous  = true;
        this.options.onComplete    = this.onComplete.bind(this);
        this.options.defaultParams = this.options.parameters || null;
        this.url                   = url;
    },

    onKeyPress: function(event) {
       if(this.active)
         switch(event.keyCode) {
          case Event.KEY_TAB:
          case Event.KEY_RETURN:
            this.selectEntry();
            Event.stop(event);
          case Event.KEY_ESC:
            this.hide();
            this.active = false;
            if(this.options.revertOnEsc) this.revertOnEsc()
            Event.stop(event);
            return;
          case Event.KEY_LEFT:
          case Event.KEY_RIGHT:
            return;
          case Event.KEY_UP:
            this.markPrevious();
            this.render();
            if(Prototype.Browser.WebKit) Event.stop(event);
            return;
          case Event.KEY_DOWN:
            this.markNext();
            this.render();
            if(Prototype.Browser.WebKit) Event.stop(event);
            return;
         }
        else
          if(event.keyCode==Event.KEY_TAB || event.keyCode==Event.KEY_RETURN ||
            (Prototype.Browser.WebKit > 0 && event.keyCode == 0)) return;

       this.changed = true;
       this.hasFocus = true;

       if(this.observer) clearTimeout(this.observer);
         this.observer =
           setTimeout(this.onObserverEvent.bind(this), this.options.frequency*1000);
     },

    revertOnEsc: function() {
        if(this.oldEntry) this.updateElement(this.oldEntry);
    },

    selectEntry: function() {
        this.active = false;
        this.updateElement(this.getCurrentEntry());

        this.oldEntry = this.getCurrentEntry();
    },

    onBlur: function(event) {
        // needed to make click events working
        setTimeout(this.hide.bind(this), 250);
        this.hasFocus = false;
        this.active = false;

        if(this.options.revertOnEsc) this.revertOnEsc()
    },

    reset: function() {
        this.active = false;
        this.hide();
        this.element.value = null;
        this.oldEntry = null;
    }

}) ;

Event.observe(window, "load", createAutocompleter)
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
        text-align:center;
    }

    .input-row td span{
        cursor: move;
    }

    .input-row td input {
        /*border-width: 0; border-bottom: 1px dotted #666;*/
        border-width: 0; border-bottom: 1px #666;
        text-align: center;
        padding: 2px;
    }

    table {
        border-collapse: collapse;
    }

    td, th {
        border: 1px solid #666;
        text-align:center;
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
        margin-left:1%;
        margin-top: 1%;
        padding: 10px;
    }
    td.emptyCell{
        empty-cells:hide;
        border:none;
        width:100px;
    }
    td.emptyCellNoWidth {
        empty-cells:hide;
        border:none;
    }

   #revision-changes {
        float: right;
        width: 29%;
    }

    #with-changes #period {
        width: 70%;
        float: left;
    }
    label#emptyLabel {
        empty-cells:hide;
        border:none;
        width:100px;
    }

    #td1, #td2, #td3 {
        display:none;
    }

    #new-activities-link-separator {
        padding:0 1em 0 1em;
    }

    div.autocomplete {
        position:absolute;
        width:400px;
        background-color:white;
        border:1px solid #ccc;
        margin:0;
        padding:0;
        font-size:0.8em;
        text-align:left;
        max-height:200px;
        overflow:auto;
    }

    div.autocomplete ul {
        list-style-type:none;
        margin:0;
        padding:0;
    }

    div.autocomplete ul li.selected {
        background-color: #EAF2FB;
    }

    div.autocomplete ul li {
        list-style-type:none;
        display:block;
        margin:0;
        padding:2px;
        cursor:pointer;
    }

    .limited-display-box {
        width:670px;
        padding:5px;
        overflow-x:auto;
        display:block;
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

        <form:form>
            <c:set var="tableWidth" value="${period.duration.days + 2}"/>
            <table class="limited-display-box">
             <tbody id="input-body">
                <tr>
                    <c:choose>
                        <c:when test="${not empty command.grid}">
                            <td id="td1" style="display:table-cell;"></td>
                        </c:when>
                        <c:otherwise>
                            <td id="td1"></td>
                        </c:otherwise>
                    </c:choose>


                    <th colspan="${tableWidth - 2}">Days of segment (${commons:pluralize(period.repetitions, "repetition")})</th>
                    <td></td>
                    <td class="emptyCellNoWidth"> </td>
                </tr>
                <tr id="days-header">
                    <c:choose>
                        <c:when test="${not empty command.grid}">
                            <td id="td2" style="display:table-cell;"></td>
                        </c:when>
                        <c:otherwise>
                            <td id="td2"></td>
                        </c:otherwise>
                    </c:choose>

                    <c:set var="MAX_REPETITION_SIZE" value="4"/>
                    <c:set var="MAX_REPETITIONS_DISPLAYED_WHEN_COMPRESSED" value="2"/>
                    <c:set var="compressPeriod" value="${period.repetitions gt MAX_REPETITION_SIZE}"/>
                    <c:forEach items="${period.dayRanges[0].days}" var="d">
                        <th id="day-number" class="day-number">
                            <c:forEach begin="0" end="${period.repetitions - 1}" var="x" varStatus="xStatus">
                                <c:set var="visibleRow" value="${x lt MAX_REPETITIONS_DISPLAYED_WHEN_COMPRESSED or not compressPeriod}"/>
                                <c:set var="showCompressionRow" value="${x eq (MAX_REPETITIONS_DISPLAYED_WHEN_COMPRESSED)}"/>

                                <c:if test="${visibleRow or xStatus.last}">
                                    ${d + x * period.duration.days}
                                </c:if>

                                <c:if test="${not visibleRow and showCompressionRow and not xStatus.last}">
                                    &hellip;
                                </c:if>

                                <c:if test="${not xStatus.last and (visibleRow or showCompressionRow)}"><br/></c:if>
                            </c:forEach>
                        </th>
                    </c:forEach>
                    <th>Details</th>

                    <td class="emptyCell" ></td>
                    <td class="emptyCellNoWidth"></td>
                    <th>Condition Details</th>
                </tr>

                    <tr id="no-activities-message" >
                        <td id="td3"></td>
                        <c:choose>
                            <c:when test="${not empty command.grid}">
                            </c:when>
                            <c:otherwise>
                                <td colspan="${tableWidth - 1}">This period does not have any activities yet</td>
                            </c:otherwise>
                        </c:choose>
                        <td class="emptyCell"/>
                        <td class="emptyCellNoWidth"/>
                        <td class="emptyCellNoWidth"/>
                    </tr>
                    <c:forEach items="${command.grid}" var="gridRow" varStatus="gridStatus">
                        <tr class="input-row">
                            <th class="activity">
                                    ${gridRow.activity.name}
                                <form:hidden path="grid[${gridStatus.index}].activity"/>
                            </th>
                            <c:forEach items="${gridRow.eventIds}" varStatus="cStatus">
                                <td class="counter">
                                    <c:choose>
                                        <c:when test="${not empty gridRow.eventIds[cStatus.index]}">
                                        <span id="grid[${gridStatus.index}].eventIds[${cStatus.index}]" value="${gridRow.eventIds[cStatus.index]}" class="marker">X</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span id="grid[${gridStatus.index}].eventIds[${cStatus.index}]" value="-1" class="marker"></span>
                                    </c:otherwise>
                                    </c:choose>
                                </td>
                            </c:forEach>
                            <td>
                                <form:input path="grid[${gridStatus.index}].details"
                                            onchange="ajaxform(null, null, null, this, null);" />
                            </td>
                            <!--corresponds to the conditional behavior-->
                            <td class="emptyCell" ></td>
                            <td class="conditional">
                                <form:checkbox path="grid[${gridStatus.index}].conditionalCheckbox"
                                               value="${gridRow.conditionalCheckbox}" />
                            </td>
                            <td>
                                <form:input path="grid[${gridStatus.index}].conditionalDetails"
                                            onchange="return ajaxform(null, null, null, null, this);"
                                            disabled="${empty gridRow.conditionalDetails}"
                                        />
                            </td>
                        </tr>
                    </c:forEach>


                    <div  style="float:left;position:relative;margin-right:20px">
                        <span style="border:1px solid #ccc;margin-left:5px;font-size:12px;padding:2px">Add</span>
                        <div style="border:1px solid #ccc;width:50px;height:50px;text-align:center;vertical-align:middle;line-height:50px;margin:2px 5px 10px 5px;cursor:move;" id="newMarkerArea">
                            <div class="newMarker">X</div>
                        </div>
                        <span style="border:1px solid #ccc;margin-left:5px;font-size:12px;padding:2px">Delete</span>

                        <div style="border:1px solid #ccc;width:50px;text-align:center;margin:2px 5px 10px 5px;z-index:1" id="deleteDrop">
                            <p style="margin:5px;font-size:16px">Drop Here</p>
                        </div>
                     </div>

                </tbody>
            </table>

            <br style="clear:both"/>

            <div id="activities-input">
                <label for="add-activity">Activities:</label>
                <select id="activity-sources-filter">
                    <option value="">All sources</option>
                    <c:forEach items="${sources}" var="source"><option value="${source.id}">${source.name}</option></c:forEach>
                </select>
                <select id="activity-type-filter">
                    <option value="">All types</option>
                    <c:forEach items="${activityTypes}" var="activityType"><option value="${activityType.id}" <c:if test="${selectedActivity.type.id == activityType.id}">selected="selected"</c:if>>${activityType.name}</option></c:forEach>
                </select>
                <input id="activities-autocompleter-input" type="text" autocomplete="off"/>
                <div id="activities-autocompleter-div" class="autocomplete"></div>


                <input type="hidden" id="add-activity"/>

                <input type="button" id="add-activity-button" value="Add to period"/>
                <a id="newActivityLink" href="<c:url value="/pages/newActivity?returnToPeriodId=${period.id}"/>">Create new activity</a> <span id="new-activities-link-separator">or</span>
                <a id="importActivitiesLink" href="<c:url value="/pages/cal/import/activities?returnToPeriodId=${period.id}"/>">Import activities from xml</a>
             </div>

             <input id="return-to-template" type="button" value="Return to template"/>
        </form:form>
    </laf:division>
</laf:box>

</div>          

  <c:if test="${showChanges}"></div></c:if>


</body>
</html>