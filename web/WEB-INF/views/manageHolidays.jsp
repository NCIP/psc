<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
<head>
    <title>Manage Holidays And Weekends</title>
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
    </style>
    <style type="text/css">
        #mainHolidaysForm  div {
            float: left;
            margin-left:0%;
            margin-top: 0%;
            padding: 10px;
        }
        #listOfAllHolidays {
            margin-right:20%;
            margin-top: 0%;
            padding: 10px;
        }
        #errorMessage h5{
            color:red;
        }

    </style>


    <script type="text/javascript">

        function myCallBackOnFinish(obj){
          new Effect.Appear(obj)
        }

        function selectedHoliday() {
            Event.observe("allHolidaysForm", "click", function() {
                var input = $('typeOfHolidays').options[$('typeOfHolidays').selectedIndex].value
                displayTheHolidayParameters(input)
            })
        }

        function displayTheHolidayParameters(txt)
        {
            if (txt == 'DayOfTheWeek'){
                Effect.Fade('holidayRecurring-div', {afterFinish: function() {
                    myCallBackOnFinish('dayOfTheWeek-div')}});
                Effect.Fade('holidayNotRecurring-div',
                    {afterFinish: function() {myCallBackOnFinish('dayOfTheWeek-div')}});
            } else if (txt == 'RecurringHoliday') {
                Effect.Fade('dayOfTheWeek-div',
                    {afterFinish: function() {myCallBackOnFinish('holidayRecurring-div')}});
                Effect.Fade('holidayNotRecurring-div',
                    {afterFinish: function() {myCallBackOnFinish('holidayRecurring-div')}});
            } else if (txt == 'NotRecurringHoliday'){
                Effect.Fade('dayOfTheWeek-div',
                    {afterFinish: function() {myCallBackOnFinish('holidayNotRecurring-div')}});
                Effect.Fade('holidayRecurring-div',
                    {afterFinish: function() {myCallBackOnFinish('holidayNotRecurring-div')}});
            }
        }

        function resetElement(elementId, text, color) {
            var element = document.getElementById(elementId);
            element.style.color = color;
            element.innerHTML = text;
        }

        function isCorrectOcurringInput() {
            var date = document.getElementById("holidayDate").value
            var description = document.getElementById("holidayDescription").value;
            var isDataCorrect = true;
            if (isDataCorrect && (date.length < 3 || date.length >5)) {
                isDataCorrect = false;
                resetElement("recurringDescriptionText", "Please enter the holiday Description", "black");
                resetElement("recurringDateText", "Error enterring the date -<br>"+
                "Please verify the format is mm/dd", "red");
            } else if (isDataCorrect && (date.length == 5 || date.length ==3)) {
                date = date.split("/");
                if (isNaN(date[0]*1) || isNaN(date[1]*1)) {
                    isDataCorrect = false;
                    resetElement("recurringDescriptionText", "Please enter the holiday Description", "black");
                    resetElement("recurringDateText", "Error enterring the date -<br>"+
                    "Please verify the format is mm/dd", "red" );
                }
            }
            if (isDataCorrect && description.length == 0) {
                isDataCorrect = false;
                resetElement("recurringDateText", "Please enter month, day in the format<br>mm/dd", "black");
                resetElement("recurringDescriptionText", "Error - <br>"+
                "Missing Description field", "red");
            }
            if(isDataCorrect) {
                resetElement("recurringDescriptionText", "Please enter the holiday Description", "black");
                resetElement("recurringDateText", "Please enter month, day in the format<br>mm/dd", "black");
            }
            return isDataCorrect;
        }


        function isCorrectNonOccuringInput(){
            var date = document.getElementById("nonOcurringDate").value
            var description = document.getElementById("nonOcurringDescription").value
            var isDataCorrect = true;
            if (isDataCorrect &&(date.length < 8 || date.length > 10)) {
              isDataCorrect = false;
              resetElement("nonRecurringDescriptionText", "Please enter the holiday Description", "black");
              resetElement("nonRecurringDateText", "Error enterring the date -<br>"+
                "Please verify the format is mm/dd/yyyy", "red");
            } else if (isDataCorrect && (date.length == 8 || date.length ==10)){
              date = date.split("/");
              if(date.length != 3 || ((isNaN(date[0]*1) || isNaN(date[1]*1) || isNaN(date[2]*1)))) {
                isDataCorrect = false;
                resetElement("nonRecurringDescriptionText", "Please enter the holiday Description", "black");
                resetElement("nonRecurringDateText", "Error enterring the date -<br>"+
                "Please verify the format is mm/dd/yyyy", "red" );
              }
            }
            if (isDataCorrect && description.length == 0) {
                isDataCorrect = false;
                resetElement("nonRecurringDateText", "Please enter month, day in the format<br>mm/dd/yyyy", "black");
                resetElement("nonRecurringDescriptionText", "Error - <br>"+
                  "Missing Description field", "red");
            }
            if(isDataCorrect) {
                resetElement("nonRecurringDescriptionText", "Please enter the holiday Description", "black");
                resetElement("nonRecurringDateText", "Please enter month, day in the format<br>mm/dd", "black");
            }
            return isDataCorrect
        }

        Event.observe(window, "load", selectedHoliday)

    </script>

    

</head>


<body>
    <h1>Manage Holidays And Weekends</h1>
   <br>
        <div id="mainHolidaysForm">
                <div>
                    <form id="allHolidaysForm" name="myformOne">
                        <h5>Please select the holiday from the list:</h5>
                        <select name="typeOfHolidays" id="typeOfHolidays" size="3">
                            <option value="DayOfTheWeek" selected="yes">Day Of The Week</option>
                            <option value="RecurringHoliday">Recurring Holiday</option>
                            <option value="NotRecurringHoliday">Non Recurring Holiday</option>
                        </select>
                    </form>
                </div>
                <div align="left" id="dayOfTheWeek-div">
                    <h5>Please select days when office is closed. <br>Multiple selection is allowed</h5>
                    <form:form id="dayOfTheWeekForm" name="dayOfTheWeekForm">
                        <select name="dayOfTheWeek" id="dayOfTheWeek" size="7">
                            <option value="Monday" >Monday</option>
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
                        <h5 id="recurringDateText">Please enter month, day in the format
                            <br>mm/dd</h5>
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
                        <h5 id="nonRecurringDateText">Please enter month, day and year in the format mm/dd/yyyy</h5>
                        <form:input path="holidayDate" id="nonOcurringDate" size="25"/>
                        <h5 id="nonRecurringDescriptionText">Please enter the holiday Description</h5>
                        <form:input path="holidayDescription" id="nonOcurringDescription" size="25" />
                        <br> <br>
                        <!--<input type="submit" name="action" value="Add" />-->
                        <input type="submit" name="action" value="Add"
                            onclick="return(isCorrectNonOccuringInput())" />
                    </form:form>
                </div>
        </div>

        <div id="listOfAllHolidays" align="right">
                <form:form name="listOfHolidaysFormOne" method="post">
                    <h5>List of Selected Holidays: </h5>
                        <select name="listTypeOfHolidays" id="listTypeOfHolidays" size="10"  STYLE="width: 270px">

                        <c:forEach items="${command.site.holidaysAndWeekends}" var="holiday">
                                 <option value=${holiday.id}>${holiday.displayName} (${holiday.status})</option>
                        </c:forEach>
                        </select>
                  
                    <input type="submit" name="action" value="Remove" />
                    <input type="submit" name="action" value="Save" />                     
                </form:form>
        </div>
    </body>
</html>