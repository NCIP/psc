<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
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
    //var dayCount = $$("#days-header th").length - 1 //8
    //            var dayCount = $$('th.day-number').length -1 //6
    var dayCount = ${period.dayRanges[0].days}.length
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
        onChange:"return ajaxform(null, this, null);"
    });
    cells.push(Builder.node('td', {}, [detailsInput]))

    //corresponding to the conditional behavior
    cells.push(Builder.node('td', {id:'emptyCell'}));

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
        onChange:"return ajaxform(null, null, this);"});
    cells.push(Builder.node('td', {}, [conditionDetailsInput]))

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
    Event.observe(input, "click", function(e){return ajaxform(input, null, null)})
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
    formdata = formdata + 'grid[' + index + '].conditionalUpdated' + "=" + escape(true) + "&";
    formdata = formdata + 'grid[' + index + '].addition' + "=" + escape(false) + "&";
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

    formdata = formdata + 'grid[' + index + '].conditionalUpdated' + "=" + escape(true) + "&";
    formdata = formdata + 'grid[' + index + '].addition' + "=" + escape(false) + "&";
    return formdata
}

function getInfoFromEventCheckbox(formdata, checkbox, index1, index2) {
    var details = 'grid[' + index1 + '].details';
    formdata = formdata + details + "=" + escape($(details).value) + "&";
    formdata = formdata + 'grid[' + index1 + '].columnNumber'+ "=" + index2 + "&";
    formdata = formdata + 'grid[' + index1 + '].addition' + "=" + escape($(checkbox).checked) + "&";
    formdata = formdata + 'grid[' + index1 + '].conditionalUpdated' + "=" + escape(false) + "&";
    return formdata;
}

function getInfoFromEventDetails(formdata, details, index) {
    formdata = formdata + $(details).name + "=" + escape($(details).value) + "&";
    formdata = formdata + 'grid[' + index + '].addition' + "=" + escape(false) + "&";
    formdata = formdata + 'grid[' + index + '].columnNumber'+ "=" + escape(-1) + "&";
    formdata = formdata + 'grid[' + index + '].conditionalUpdated' + "=" + escape(false) + "&";
    return formdata;
}

function ajaxform(checkbox, details, conditionalDetails) {

    // Set up data variable
    var formdata = "";
    formdata = formdata + 'id='+${period.id}+"&";
    var arrayOfIndexes

    if (checkbox != null){
        arrayOfIndexes = parseInput(checkbox.name)
        if (checkbox.name.indexOf(".conditionalCheckbox")>=0) {
            formdata = getInfoFromConditionalCheckbox(formdata, checkbox, arrayOfIndexes[0])
        } else {
            formdata = getInfoFromEventCheckbox(formdata, checkbox, arrayOfIndexes[0], arrayOfIndexes[1])
        }
    } else if (details != null) {
        arrayOfIndexes = parseInput($(details).name);
        formdata = getInfoFromEventDetails(formdata, details, arrayOfIndexes[0]);
    } else if (conditionalDetails != null) {
        arrayOfIndexes = parseInput(conditionalDetails.name)
        formdata = getInfoFromConditionalDetails(formdata, conditionalDetails, arrayOfIndexes[0])
    }

    var activity = 'grid[' + arrayOfIndexes[0] + '].activity';
    formdata = formdata + activity + "=" + escape($(activity).value) +"&";

    var arrayOfCounts = 'grid[' + arrayOfIndexes[0] + '].eventIds';
    for (var i = 0; i < ${period.duration.days}; i++) {
        var singleElement = arrayOfCounts +'[' +i + ']'+1;
        if ($(singleElement) == null) {
            singleElement = arrayOfCounts +'[' +i + ']';
        }
        formdata = formdata + $(singleElement).name +  "=" + $(singleElement).value + "&" ;
    }

    formdata = formdata + 'grid[' + arrayOfIndexes[0] + '].rowNumber'+ "=" + arrayOfIndexes[0] + "&";
    formdata = formdata + 'grid[' + arrayOfIndexes[0] + '].updated' + "=" + escape(true) + "&";


    var checkboxName = 'grid[' + arrayOfIndexes[0] + '].conditionalCheckbox1';
    formdata = formdata + $(checkboxName).name +  "=" + $(checkboxName).checked + "&" ;

    var details1 = 'grid[' +  arrayOfIndexes[0] + '].conditionalDetails';
    formdata = formdata + $(details1).name +  "=" + $(details1).value + "&";

    var href = '<c:url value="/pages/cal/managePeriod"/>'

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
    $$('.input-row td.conditional').each(function(cell) {
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
    #gridTable  div {
        float: left;
        margin-left:1%;
        margin-top: 1%;
        padding: 10px;
    }
    td#emptyCell {
        empty-cells:hide;
        border:none;
        width:100px;
    }
    td#emptyCellNoWidth {
        empty-cells:hide;
        border:none;
    }

</style>
</head>
<body>
<laf:box title="Set up ${period.name} (period) of ${arm.qualifiedName} in ${study.name}">
    <laf:division>
        <p>
            This period has ${period.duration.days} days and repeats ${commons:pluralize(period.repetitions, "time")}.
            It begins on day ${period.startDay} of the ${arm.qualifiedName}.
        </p>

        <form:form>
            <c:set var="tableWidth" value="${period.duration.days + 2}"/>
            <table>
                <tr>
                    <td></td>
                    <th colspan="${tableWidth - 2}">Days of arm (${commons:pluralize(period.repetitions, "repetition")})</th>
                    <td></td>
                    <td id="emptyCellNoWidth"> </td>
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

                    <td id="emptyCell" ></td>
                    <td id="emptyCellNoWidth"></td>
                    <th>Condition Details</th>
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
                                            onchange="ajaxform(null, this, null);" />
                            </td>
                            <!--corresponds to the conditional behavior-->
                            <td id="emptyCell" ></td>
                            <td class="conditional">
                                <form:checkbox path="grid[${gridStatus.index}].conditionalCheckbox"
                                               value="${gridRow.conditionalCheckbox}" />

                                <%--<input type=checkbox <c:if test="${gridRow.conditionalDetails != null}">checked</c:if> name="grid[${gridStatus.index}].conditionalCheckbox"/>--%>

                            </td>
                            <td>
                                <form:input path="grid[${gridStatus.index}].conditionalDetails"
                                            onchange="return ajaxform(null, null, this);"
                                            disabled="${empty gridRow.conditionalDetails}"
                                        />
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
    </laf:division>
</laf:box>
</body>
</html>