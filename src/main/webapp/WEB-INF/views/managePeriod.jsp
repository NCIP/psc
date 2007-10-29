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
<%--<title>Set up Period ${period.name} of ${arm.qualifiedName} in ${study.name}</title>--%>
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
    var value = $F(input)
    var nonzero;
    if (value == null) {
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



    //corresponding to the conditional behavior
    cells.push(Builder.node('td', {className:'emptyCell'}));

    //conditionCheckBox
    var name = 'grid[' + rowCount + '].conditionalCheckbox'
    var namePlusOne = name+1
    var input = Builder.node('input', {
        type:'checkbox',
        id:namePlusOne,
        name:name,
        value:'true'

    });
    registerCellInputHandlers(input)
    cells.push(Builder.node('td', {}, [input]))

    //conditionalDetails
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

    //displaying the very fist empty column, that is not shown when no activities is present
    if (currentActivityCount() > 0) {
        var td1Element = document.getElementById("td1")
        var td2Element = document.getElementById("td2")

        td1Element.style.display = 'table-cell';
        td2Element.style.display = 'table-cell';
    }
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
    Event.observe(input, "click", function(e){return ajaxform(input, null, null, null, null)})
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

function getInfoFromConditionalDetails(formdata, conditionalDetails, index) {
    formdata = formdata + 'conditionalUpdated' + "=" + escape(true) + "&";
    formdata = formdata + 'addition' + "=" + escape(false) + "&";
    return formdata
}

function getInfoFromConditionalCheckbox(formdata, conditionalCheckbox, index) {
    var checkboxName = 'grid[' + index + '].conditionalCheckbox1';
    var isChecked = $(checkboxName).checked;
    var details = 'grid[' +  index + '].conditionalDetails';
    if(!isChecked) {
        $(details).value ="";
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
    formdata = formdata + 'columnNumber'+ "=" + index2 + "&";
    formdata = formdata + 'addition' + "=" + escape($(checkbox).checked) + "&";
    formdata = formdata + 'conditionalUpdated' + "=" + escape(false) + "&";    
    return formdata;
}

function getInfoFromEventDetails(formdata, details, index) {
    formdata = formdata + "details=" + escape($(details).value) + "&";
    formdata = formdata + 'addition' + "=" + escape(false) + "&";
    formdata = formdata + 'columnNumber'+ "=" + escape(-1) + "&";
    formdata = formdata + 'conditionalUpdated' + "=" + escape(false) + "&";
    return formdata;
}

function getInfoFromEventTextbox(formdata, isAdding, index1, index2) {
   var details = 'grid[' + index1 + '].details';
    formdata = formdata + "details=" + escape($(details).value) + "&";
    formdata = formdata + 'columnNumber'+ "=" + index2 + "&";
    formdata = formdata + 'addition' + "=" + isAdding + "&";
    formdata = formdata + 'conditionalUpdated' + "=" + escape(false) + "&";
    return formdata;
}

function createAddControl(text, objectType, objectId) {
    return createControlAnchorOne("add", text, "Add this " + objectType, '<c:url value="/pages/cal/managePeriod/addTo"/>')
}

function createRemoveControl(text, objectType, objectId) {
    return createControlAnchorOne("remove", text, "Remove this " + objectType, '<c:url value="/pages/cal/managePeriod/remove"/>')
}


function updateDetails(text, objectType, objectId) {
    return createControlAnchorOne("update", text, "Update " + objectType, '<c:url value="/pages/cal/managePeriod/update"/>')
}

function move(text, objectType, objectId) {
    return createControlAnchorOne("move", text, "Move " + objectType, '<c:url value="/pages/cal/managePeriod/move"/>')
}

function createControlAnchorOne(controlName, text, title, baseHref) {
    var href = baseHref;
    if (href.indexOf('?') >= 0) {
        href += '&'
    } else {
        href += '?'
    }
    return href
}

function moveAjaxForm(arrayOfIndicesForParent, arrayOfIndicesForChild, isAdd) {
    var href = move("Move event", "event ", arrayOfIndicesForChild[0]);
    // Set up data variable
    var formdata = "";
    formdata = formdata + 'id='+${period.id}+"&";
    formdata = getInfoFromEventTextbox(formdata, isAdd, arrayOfIndicesForChild[0], arrayOfIndicesForChild[1])

    var activity = 'grid[' + arrayOfIndicesForChild[0] + '].activity';
    formdata = formdata + "activity=" + escape($(activity).value) +"&";
    var arrayOfCounts = 'grid[' + arrayOfIndicesForChild[0] + '].eventIds';

    singleElement = arrayOfCounts +'[' +arrayOfIndicesForParent[1] + ']';
    if ($(singleElement).getAttribute('value') !=  null ) {
        formdata = formdata + 'eventIds' + '[' +arrayOfIndicesForParent[1] + ']'+  "=" + $(singleElement).getAttribute('value') + "&" ;
    }
    formdata = formdata + 'rowNumber'+ "=" + arrayOfIndicesForChild[0] + "&";
    formdata = formdata + 'updated' + "=" + escape(true) + "&";

    var checkboxName = 'grid[' + arrayOfIndicesForChild[0] + '].conditionalCheckbox1';
    formdata = formdata + "conditionalCheckbox1=" + $(checkboxName).checked + "&" ;
    var details1 = 'grid[' +  arrayOfIndicesForChild[0] + '].conditionalDetails';
    formdata = formdata + "conditionalDetails=" + $(details1).value + "&";

    formdata = formdata+ "moveFrom=" + arrayOfIndicesForParent[1] + "&";
    formdata = formdata+ "moveTo=" + arrayOfIndicesForChild[1] + "&";

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
    var arrayOfIndexes

    if (checkbox != null){
        arrayOfIndexes = parseInput(checkbox.name)
        if (checkbox.name.indexOf(".conditionalCheckbox")>=0) {
            formdata = getInfoFromConditionalCheckbox(formdata, checkbox, arrayOfIndexes[0])
            href = updateDetails ("Update details", "details", arrayOfIndexes[0]);
        } else {
            formdata = getInfoFromEventCheckbox(formdata, checkbox, arrayOfIndexes[0], arrayOfIndexes[1])
            if($(checkbox).checked) {
                href = createAddControl("Add checkbox", "checkbox", arrayOfIndexes[0])
            } else {
                href = createRemoveControl("Remove checkbox", "checkbox", arrayOfIndexes[0])
            }
        }

    } else if (details != null) {
        arrayOfIndexes = parseInput($(details).name);
        formdata = getInfoFromEventDetails(formdata, details, arrayOfIndexes[0]);
        href = updateDetails ("Update details", "details", arrayOfIndexes[0]);
    } else if (conditionalDetails != null) {
        arrayOfIndexes = parseInput(conditionalDetails.name)
        formdata = getInfoFromConditionalDetails(formdata, conditionalDetails, arrayOfIndexes[0])
        href = updateDetails ("Update details", "details", arrayOfIndexes[0]);
    } else if (textbox != null) {
        arrayOfIndexes = parseInput(textbox)
        formdata = getInfoFromEventTextbox(formdata, isAdd, arrayOfIndexes[0], arrayOfIndexes[1])
        if (isAdd) {
            href = createAddControl("Add checkbox", "checkbox", arrayOfIndexes[0])
        } else {
            href = createRemoveControl("Remove checkbox", "checkbox", arrayOfIndexes[0])
        }
    }

    var activity = 'grid[' + arrayOfIndexes[0] + '].activity';
    formdata = formdata + "activity=" + escape($(activity).value) +"&";

    var arrayOfCounts = 'grid[' + arrayOfIndexes[0] + '].eventIds';
    for (var i = 0; i < ${period.duration.days}; i++) {
        singleElement = arrayOfCounts +'[' +i + ']';
        if ($(singleElement).getAttribute('value') ==  null ) {
            formdata = formdata + 'eventIds' + '[' +i + ']'+  "=-1" + "&" ;
        } else {
            formdata = formdata + 'eventIds' + '[' +i + ']'+  "=" + $(singleElement).getAttribute('value') + "&" ;
        }
    }

    formdata = formdata + 'rowNumber'+ "=" + arrayOfIndexes[0] + "&";
    formdata = formdata + 'updated' + "=" + escape(true) + "&";


    var checkboxName = 'grid[' + arrayOfIndexes[0] + '].conditionalCheckbox1';
    formdata = formdata + "conditionalCheckbox1=" + $(checkboxName).checked + "&" ;

    var details1 = 'grid[' +  arrayOfIndexes[0] + '].conditionalDetails';
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
    Event.observe('select-activity-type', 'change', updateActivitySelector)
//    Event.observe('return-to-template', 'click', returnToTemplate)
}

function makeCellDraggableAndDroppable(input) {
        document.getElementsByClassName('marker').each(
			function(item) {
				new Draggable(item,{revert: true});
				item.currentDurationIndex = item.parentNode.durationIndex;
				item.activity = item.parentNode.parentNode.getElementsByClassName('activity')[0].innerHTML.trim();
			}
        );

		Droppables.add( $('deleteDrop'), {accept:'marker',hoverclass: 'hoverActive',onDrop:deleteEvent})


}

function registerDraggablesAndDroppables() {
    document.getElementsByClassName('newMarker').each(
        function(item) {new Draggable(item, {revert: true});
      }
    );

    var x=0;
    document.getElementsByClassName('counter').each(
        function(item) {Droppables.add(item, {
                    accept:['marker','newMarker'],
                    hoverclass: 'hoverActive',
                    onDrop: moveEvent })
            item.durationIndex = x;
            x = (x == PERIOD_DURATION-1) ? 0 : (x+1);
        }
    );

    document.getElementsByClassName('marker').each(
        function(item) {new Draggable(item,{revert: true});
            item.currentDurationIndex = item.parentNode.durationIndex;
            item.activity = item.parentNode.parentNode.getElementsByClassName('activity')[0].innerHTML.trim();
        }
    );

    Droppables.add( $('deleteDrop'), {accept:'marker',hoverclass: 'hoverActive',onDrop:deleteEvent})
}

Event.observe(window, "load", registerHandlers)
Event.observe(window, "load", showEmptyMessage)
Event.observe(window, "load", updateActivitySelector)
Event.observe(window, "load", registerDraggablesAndDroppables)



    String.prototype.trim = function() {
		this.replace( /^\s+/g, "" );
  		return this.replace( /\s+$/g, "" );
	}

	var PERIOD_DURATION = 7;
	var PERIOD_REPITITION = 3;        


function trim(inputString) {
    if (typeof inputString != "string") { return inputString; }
    var retValue = inputString;
    var ch = retValue.substring(0, 1);
    while (ch == " ") {
        retValue = retValue.substring(1, retValue.length);
        ch = retValue.substring(0, 1);
    }
    ch = retValue.substring(retValue.length-1, retValue.length);
    while (ch == " ") {
        retValue = retValue.substring(0, retValue.length-1);
        ch = retValue.substring(retValue.length-1, retValue.length);
    }
    while (retValue.indexOf("  ") != -1) {
        retValue = retValue.substring(0, retValue.indexOf("  ")) + retValue.substring(retValue.indexOf("  ")+1, retValue.length);
    }
    return retValue;
}

function moveEvent(draggable,dropZone) {
    var wholeElement = trim(dropZone.getElementsBySelector("span")[0])
    var elementId = dropZone.getElementsBySelector("span")[0].id

    var parentElement = trim(draggable.parentNode.getElementsBySelector("span")[0])
    if (parentElement == null) {
        //means we are drugging a new event
        if (wholeElement.firstChild == null) {
            ajaxform(null, elementId, true, null, null)
            setUpMarker(draggable, dropZone)
        }
    } else {
        //means we are moving event from one cell to another
        var parentElementId = parentElement.id
        var arrayOfIndisesForParent = parseInput(parentElementId)
        var arrayOfIndisesForChild = parseInput(elementId)
        if (arrayOfIndisesForParent[0]== arrayOfIndisesForChild[0]) {
            //need to set up ajax call for move
            if (wholeElement.firstChild == null) {
                moveAjaxForm(arrayOfIndisesForParent, arrayOfIndisesForChild, false)
                setUpMovingMarker(draggable, dropZone)
            }
        }
    }
}


function setUpMarker(draggable, dropZone) {
    var prevDurationIndex = draggable.currentDurationIndex;
    var newDurationIndex = dropZone.durationIndex;
    var activity = (typeof(draggable.activity) != 'undefined')? draggable.activity : dropZone.parentNode.getElementsByClassName('activity')[0].innerHTML.trim();
    if(prevDurationIndex != newDurationIndex && activity == dropZone.parentNode.getElementsByClassName('activity')[0].innerHTML.trim()) {
        var marker = createMarker(newDurationIndex, activity);
        dropZone.appendChild(marker);

        draggable.parentNode.removeChild(draggable);
        if(draggable.className=='newMarker') {
            var div = Builder.node("div", { className: 'newMarker' })
            div.innerHTML = 'X';
            new Draggable(div, { revert: true } );
            $('newMarkerArea').appendChild(div);
        }
    }
}    


function setUpMovingMarker(draggable, dropZone) {
    draggable.innerHTML='';
    var element = trim(dropZone.getElementsBySelector("span")[0]);
    element.innerHTML='X';
}

function deleteEvent(draggable,dropZone) {
    var element = draggable.parentNode.getElementsBySelector("span")[0].id
    ajaxform(null, element, false, null, null)

    var prevDurationIndex = draggable.currentDurationIndex;
    var prevActivity = draggable.activity;
    draggable.innerHTML = '';
}


function createMarker(currentDurationIndex, activityName) {
    var marker = document.createElement('span');
    marker.innerHTML = 'X';
    marker.className='marker';
    marker.currentDurationIndex = currentDurationIndex;
    marker.activity = activityName;
    new Draggable(marker,{revert: true});
    return marker;
}


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

</style>
</head>
<body>

<c:set var="showChanges" value="${not empty developmentRevision and not study.inInitialDevelopment}"/>
<%--'${revisionChanges.flattened}'--%>

  <c:if test="${showChanges}">
      <div id="with-changes">
          <div id="revision-changes" class="section">
              <templ:changes revision="${developmentRevision}" changes="${revisionChanges}"/>
          </div>
      <%-- #with-changes is closed below --%>
  </c:if>

<div id="period" class="section">
<laf:box title="Set up ${period.name} (period) of ${arm.qualifiedName} in ${study.name}">
    <laf:division>
        <p>
            This period has ${period.duration.days} days and repeats ${commons:pluralize(period.repetitions, "time")}.
            It begins on day ${period.startDay} of the ${arm.qualifiedName}.
        </p>

        <form:form>
            <c:set var="tableWidth" value="${period.duration.days + 2}"/>
            <table>
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


                    <th colspan="${tableWidth - 2}">Days of arm (${commons:pluralize(period.repetitions, "repetition")})</th>
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


                    <c:forEach items="${period.dayRanges[0].days}" var="d">
                        <th id="day-number" class="day-number">
                            <c:forEach begin="0" end="${period.repetitions - 1}" var="x" varStatus="xStatus">
                                ${d + x * period.duration.days}
                                <c:if test="${not xStatus.last}"><br/></c:if>
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
             <input align="right" type="button" name="action" value="Return to template" onclick="location.href='<c:url value="/pages/cal/template?arm=${arm.id}&study=${study.id}"/>'" />
        </form:form>
    </laf:division>
</laf:box>

</div>          

  <c:if test="${showChanges}"></div></c:if>


</body>
</html>