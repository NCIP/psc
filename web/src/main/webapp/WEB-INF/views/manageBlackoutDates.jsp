<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
<title>Manage Blackout Dates</title>
<tags:includeScriptaculous/>
<style type="text/css">
    div.label {
        width: 35%;
    }

    div.submit {
        text-align: right;
    }

    form {
        width: 20em;
    }

    #mainHolidaysForm  div {
        float: left;
        margin-left: 0%;
        margin-top: 0%;
        padding: 10px;
    }

    #listOfAllHolidays {
        margin-right: 20%;
        margin-top: 0%;
        padding: 10px;
    }
</style>

<script type="text/javascript">

function myCallBackOnFinish(obj) {
    new Effect.Appear(obj)
}

function selectedHoliday() {
    Event.observe("allHolidaysForm", "click", function() {
        var input = $('typeOfHolidays').options[$('typeOfHolidays').selectedIndex].value
        displayTheHolidayParameters(input)
    })
}

function resetInputValue() {
    $("holidayDate").value = "";
    $("holidayDescription").value = "";
    $("nonOcurringDate").value = "";
    $("nonOcurringDescription").value = "";
    $("recurringDescription").value = "";
}

function displayTheHolidayParameters(txt)
{
    if (txt == 'DayOfTheWeek') {
        Effect.Fade('holidayRecurring-div',
        {afterFinish: function() {
            myCallBackOnFinish('dayOfTheWeek-div')
        }});
        Effect.Fade('holidayNotRecurring-div',
        {afterFinish: function() {
            myCallBackOnFinish('dayOfTheWeek-div')
        }});
        Effect.Fade('relativeRecurring-div',
        {afterFinish: function() {
            myCallBackOnFinish('dayOfTheWeek-div')
        }});
    } else if (txt == 'RecurringHoliday') {
        Effect.Fade('dayOfTheWeek-div',
        {afterFinish: function() {
            myCallBackOnFinish('holidayRecurring-div')
        }});
        Effect.Fade('holidayNotRecurring-div',
        {afterFinish: function() {
            myCallBackOnFinish('holidayRecurring-div')
        }});
        Effect.Fade('relativeRecurring-div',
        {afterFinish: function() {
            myCallBackOnFinish('holidayRecurring-div')
        }});
    } else if (txt == 'NotRecurringHoliday') {
        Effect.Fade('dayOfTheWeek-div',
        {afterFinish: function() {
            myCallBackOnFinish('holidayNotRecurring-div')
        }});
        Effect.Fade('holidayRecurring-div',
        {afterFinish: function() {
            myCallBackOnFinish('holidayNotRecurring-div')
        }});
        Effect.Fade('relativeRecurring-div',
        {afterFinish: function() {
            myCallBackOnFinish('holidayNotRecurring-div')
        }});
    } else if (txt == 'RelativeRecurringHoliday') {
        Effect.Fade('dayOfTheWeek-div',
        {afterFinish: function() {
            myCallBackOnFinish('relativeRecurring-div')
        }});
        Effect.Fade('holidayRecurring-div',
        {afterFinish: function() {
            myCallBackOnFinish('relativeRecurring-div')
        }});
        Effect.Fade('holidayNotRecurring-div',
        {afterFinish: function() {
            myCallBackOnFinish('relativeRecurring-div')
        }});
    }
    resetInputValue();
}

function trim(inputString) {
    if (typeof inputString != "string") {
        return inputString;
    }
    var retValue = inputString;
    var ch = retValue.substring(0, 1);
    while (ch == " ") {
        retValue = retValue.substring(1, retValue.length);
        ch = retValue.substring(0, 1);
    }
    ch = retValue.substring(retValue.length - 1, retValue.length);
    while (ch == " ") {
        retValue = retValue.substring(0, retValue.length - 1);
        ch = retValue.substring(retValue.length - 1, retValue.length);
    }
    while (retValue.indexOf("  ") != -1) {
        retValue = retValue.substring(0, retValue.indexOf("  ")) + retValue.substring(retValue.indexOf("  ") + 1, retValue.length);
    }
    return retValue;
}


function resetElement(elementId, text, color) {
    var element = $(elementId);
    element.style.color = color;
    element.innerHTML = text;
}

function isMonthInputCorrect(month) {
    return !(isNaN(month) || (month <= 0 || month > 12));
}

function isDayInputCorrect(day) {
    return !(isNaN(day) || (day <= 0 || day > 31));
}

function isDayMonthCombinationCorrect(day, month) {
    return !(((month==4 || month==6 || month==9 || month==11) && day>30) || (month==2 && day>29));
}

function isYearCorrect(year) {
    return !(isNaN(year) || year <= 1900);
}

function isDayMonthYearCombinationCorrect(day, month, year) {
    return !((month==2 && year%4!=0 && day>28) || (month==2 && day>29))
}

function isCorrectOcurringInput() {
    var date = trim($("holidayDate").value);
    var format = $("recurringDateText").getAttribute("format");
    var description = $("holidayDescription").value;
    var isDataCorrect = true;
    if (isDataCorrect && (date.length < 3 || date.length > 5)) {
        isDataCorrect = false;
        resetElement("recurringDescriptionText", "Please enter the holiday Description", "black");
        resetElement("recurringDateText", "Error enterring the date -<br>" +
                                          "Please verify the format is " + format, "red");
    } else if (isDataCorrect && (date.length <= 5 && date.length >= 3)) {
        if (date.indexOf("/") < 0) {
            isDataCorrect = false;
            resetElement("recurringDescriptionText", "Please enter the holiday Description", "black");
            resetElement("recurringDateText", "Error enterring the date -<br>" +
                                              "Please verify the format is " + format, "red");
        } else {
            date = date.split("/");
            if (format.startsWith("mm")) {
                isDataCorrect = isMonthInputCorrect(date[0]) && isDayInputCorrect(date[1]) && isDayMonthCombinationCorrect(date[1], date[0]);
            } else {
                isDataCorrect = isMonthInputCorrect(date[1]) && isDayInputCorrect(date[0]) && isDayMonthCombinationCorrect(date[0], date[1]);
            }
            if (!isDataCorrect) {
                resetElement("recurringDescriptionText", "Please enter the holiday Description", "black");
                resetElement("recurringDateText", "Error enterring the date -<br>" +
                                                  "Please verify the format is " + format, "red");
            }
        }
    }
    if (isDataCorrect && description.length == 0) {
        isDataCorrect = false;
        resetElement("recurringDateText", "Please enter month, day in the format<br>" + format, "black");
        resetElement("recurringDescriptionText", "Error - <br>" +
                                                 "Missing Description field", "red");
    }
    if (isDataCorrect) {
        resetElement("recurringDescriptionText", "Please enter the holiday Description", "black");
        resetElement("recurringDateText", "Please enter month, day in the format<br>" + format , "black");
    }
    return isDataCorrect;
}


function isCorrectNonOccuringInput() {
    var date = trim($("nonOcurringDate").value);
    var format = $("nonRecurringDateText").getAttribute("format");
    var description =$("nonOcurringDescription").value;
    var isDataCorrect = true;
    if (isDataCorrect && (date.length < 8 || date.length > 10)) {
        isDataCorrect = false;
        resetElement("nonRecurringDescriptionText", "Please enter the holiday Description", "black");
        resetElement("nonRecurringDateText", "Error enterring the date -<br>" +
                                             "Please verify the format is " + format, "red");
    } else if (isDataCorrect && (date.length >= 8 && date.length <= 10)) {
        if (date.indexOf("/") < 0) {
            isDataCorrect = false;
            resetElement("nonRecurringDescriptionText", "Please enter the holiday Description", "black");
            resetElement("nonRecurringDateText", "Error enterring the date -<br>" +
                                                 "Please verify the format is "+ format, "red");
        } else {
            date = date.split("/");
            if (date.length != 3 || !isYearCorrect(date[2])) {
                isDataCorrect = false;
            } else {
                if (format.startsWith("mm")) {
                   isDataCorrect = isMonthInputCorrect(date[0]) && isDayInputCorrect(date[1]) && isDayMonthCombinationCorrect(date[1], date[0])
                           && isDayMonthYearCombinationCorrect(date[1], date[0], date[2]);
                } else {
                    isDataCorrect = isMonthInputCorrect(date[1]) && isDayInputCorrect(date[0]) && isDayMonthCombinationCorrect(date[0], date[1])
                           && isDayMonthYearCombinationCorrect(date[0], date[1], date[2]);
                }
             }

             if (!isDataCorrect) {
                 resetElement("nonRecurringDescriptionText", "Please enter the holiday Description", "black");
                 resetElement("nonRecurringDateText", "Error enterring the date -<br>" +
                                                      "Please verify the format is " + format, "red");
             }
        }
    }
    if (isDataCorrect && description.length == 0) {
        isDataCorrect = false;
        resetElement("nonRecurringDateText", "Please enter month, day in the format<br>" + format, "black");
        resetElement("nonRecurringDescriptionText", "Error - <br>" +
                                                    "Missing Description field", "red");
    }
    if (isDataCorrect) {
        resetElement("nonRecurringDescriptionText", "Please enter the holiday Description", "black");
        resetElement("nonRecurringDateText", "Please enter month, day in the format<br>" + format, "black");
    }
    return isDataCorrect;
}


function isCorrectRecurringInput() {
    var description = $("recurringDescription").value;
    var isDataCorrect = true;
    if (description.length == 0) {
        isDataCorrect = false;
        resetElement("recurringHolidayText", "Error - <br>" +
                                             "Missing Description field", "red");
    }
    if (isDataCorrect) {
        resetElement("recurringHolidayText", "Please enter the holiday Description", "black");
    }
    return isDataCorrect;
}

Event.observe(window, "load", selectedHoliday)

</script>
</head>

<body>
<laf:box title="Manage Holidays And Weekends">
<laf:division>
<br>
<c:set var="origPattern" value="${configuration.map.displayDateFormat}"/>    
<div id="mainHolidaysForm">
<div align="left">
    <form id="allHolidaysForm" name="myformOne">
        <h5>Please select the holiday from the list:</h5>
        <select name="typeOfHolidays" id="typeOfHolidays" size="4">
            <option value="DayOfTheWeek" selected="yes">Day Of The Week</option>
            <option value="RecurringHoliday">Recurring Holiday</option>
            <option value="NotRecurringHoliday">Non Recurring Holiday</option>
            <option value="RelativeRecurringHoliday">Relative Recurring Holiday</option>
        </select>
    </form>
</div>
<div align="left" id="dayOfTheWeek-div">
    <h5>Please select days when office is closed. <br>Multiple selection is allowed</h5>
    <form:form id="dayOfTheWeekForm" name="dayOfTheWeekForm">
        <select name="dayOfTheWeek" id="dayOfTheWeek" size="7">
            <option value="Monday" selected="yes" >Monday</option>
            <option value="Tuesday">Tuesday</option>
            <option value="Wednesday">Wednesday</option>
            <option value="Thursday">Thursday</option>
            <option value="Friday">Friday</option>
            <option value="Saturday">Saturday</option>
            <option value="Sunday">Sunday</option>
        </select>
        <div id="blah">
            <input type="submit" name="action" value="Add"/>
        </div>
    </form:form>
</div>

<div align="left" id="holidayRecurring-div" style="display: none;">
    <form:form id="recurringHoliday" name="ocurringHoliday">
        <c:set var="format" value="${fn:substring(fn:toLowerCase(origPattern), 0, fn:length(origPattern) - fn:length('/yyyy') )}"/>
        <h5 id="recurringDateText" format="${format}">Please enter month, day in the format
            <br>${format}</h5>
        <form:input path="holidayDate" id="holidayDate" size="25"/>
        <h5 id="recurringDescriptionText">Please enter the holiday Description</h5>
        <form:input path="holidayDescription" id="holidayDescription"size="25"/>
        <br> <br>
        <div id="recurringDate">
                <%--<input type="submit" name="action" value="Add" onclick="isCorrectInput"/>--%>
            <input type="submit" name="action" value="Add"
                   onClick="return (isCorrectOcurringInput())" />
        </div>
    </form:form>
</div>
<div align=left id="holidayNotRecurring-div" style="display: none;">
    <form:form name="nonOcurringHoliday" method="post">
        <c:set var="format" value="${fn:toLowerCase(origPattern)}"/>
        <h5 id="nonRecurringDateText" format="${format}">Please enter month, day and year in the format ${format}</h5>
        <form:input path="holidayDate" id="nonOcurringDate" size="25"/>
        <h5 id="nonRecurringDescriptionText">Please enter the holiday Description</h5>
        <form:input path="holidayDescription" id="nonOcurringDescription" size="25" />
        <br> <br>
        <!--<input type="submit" name="action" value="Add" />-->
        <input type="submit" name="action" value="Add"
               onclick="return(isCorrectNonOccuringInput())" />
    </form:form>
</div>

<div align="left" id="relativeRecurring-div" style="display: none;">
    <form:form name="relativeRecurringHolidayForm" method="post">
        <table>
            <tr>
                <td valign="top">
                    <h5>Week:</h5>
                    <select name="week" size="5">
                        <option value="1" selected="yes">First</option>
                        <option value="2">Second</option>
                        <option value="3">Third</option>
                        <option value="4">Fourth</option>
                        <option value="5">Fifth</option>
                    </select>
                </td>
                <td>
                    <h5>Day of the Week:</h5>
                    <select name="dayOfTheWeek" size="7">
                        <option value="Monday" selected="yes">Monday</option>
                        <option value="Tuesday">Tuesday</option>
                        <option value="Wednesday">Wednesday</option>
                        <option value="Thursday">Thursday</option>
                        <option value="Friday">Friday</option>
                        <option value="Saturday">Saturday</option>
                        <option value="Sunday">Sunday</option>
                    </select>
                </td>
                <td>
                    <h5>Month:</h5>
                    <select name="month" size="7">
                        <option value="0" selected="yes">January</option>
                        <option value="1">February</option>
                        <option value="2">March</option>
                        <option value="3">April</option>
                        <option value="4">May</option>
                        <option value="5">June</option>
                        <option value="6">July</option>
                        <option value="7">August</option>
                        <option value="8">September</option>
                        <option value="9">October</option>
                        <option value="10">November</option>
                        <option value="11">December</option>
                    </select>
                </td>
            </tr>
        </table>
        <h5 id="recurringHolidayText">Please enter the holiday Description</h5>
        <form:input path="holidayDescription" id="recurringDescription" size="25" />
        <!--<input type="submit" name="action" value="Add"/>-->
        <input type="submit" name="action" value="Add"
               onclick="return(isCorrectRecurringInput())" />
    </form:form>
</div>

</div>

<div id="listOfAllHolidays" align="right">
    <form:form name="listOfHolidaysFormOne" method="post">
        <h5>List of Selected Holidays: </h5>
        <%--<select name="listTypeOfHolidays" id="listTypeOfHolidays" size="10"  STYLE="width: 370px">--%>
        <select name="selectedHoliday" id="selectedHoliday" size="10"  STYLE="width: 370px">
            <c:forEach items="${command.site.blackoutDates}" var="holiday">
                <option value=${holiday.id}>${holiday.displayName} (${holiday.description})</option>
            </c:forEach>
        </select>

        <input type="submit" name="action" value="Remove"/>
    </form:form>
</div>
</laf:division>
</laf:box>
</body>
</html>