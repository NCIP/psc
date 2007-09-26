<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<html>

<head>
    <tags:stylesheetLink name="main"/>
    <tags:javascriptLink name="main"/>
    <style type="text/css">
        div.label {
            width: 25%;
        }
        div.submit {
            text-align: left;
        }
        form {
            width: 27em;
        }
        #buttons {
            margin-top:10px;
        }
    </style>

<script type="text/javascript">

    function isCorrectOcurringInput() {
        var date = trim(document.getElementById("date").value);
        var isDataCorrect = true;
        if (isDataCorrect && (date.length < 6 || date.length > 7)) {
            isDataCorrect = false;
            resetElement("dateDescription", "Error <br>"+
                                              "Please verify the format is mm/yyyy", "red");
        } else if (isDataCorrect && (date.length >= 6 || date.length <= 7)) {
            if (date.indexOf("/") < 0) {
                isDataCorrect = false;
                resetElement("dateDescription", "Error <br>"+
                                                  "Please verify the format is mm/yyyy", "red" );
            } else {
                date = date.split("/");
                if (isNaN(date[0]*1) || isNaN(date[1]*1)) {
                    isDataCorrect = false;
                    resetElement("dateDescription", "Error <br>"+
                                                      "Please verify the format is mm/yyyy", "red" );
                }

                if (date[1].length < 4 || date[0].length >2 ) {
                    isDataCorrect = false;
                    resetElement("dateDescription", "Error <br>"+
                                                      "Please verify the format is mm/yyyy", "red" );
                }
            }
        }
        if(isDataCorrect) {
            resetElement("dateDescription", "Amendment Date:<br/>(mm/yyyy)", "black");
        }
        return isDataCorrect;
    }

            
    function resetElement(elementId, text, color) {
        var element = document.getElementById(elementId);
        element.style.color = color;
        element.innerHTML = text;
    }

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

</script>
</head>
<body>
<laf:box title="Amendment Login">
    <laf:division>
        <c:url value="/pages/cal/amendment" var="action"/>
        <form:form method="post"action="${action}">
            <form:hidden path="study"/>
            <%--<form:errors path="*"/>--%>
            <div class="row">
                <div class="label">
                    Amendment Name:
                </div>
                <div class="value">
                    <form:input path="name" id="name"/>
                </div>
            </div>
            <div class="row">
                <div class="label" id="dateDescription">
                    Amendment Date:<br/>
                    (mm/yyyy)
                </div>
                <div class="value">
                    <form:input path="date" id="date"/>
                </div>
            </div>
            <div class="row" id="buttons">
                <div class="label">&nbsp;</div>
                <div class="value">
                    <input type="submit" name="action" value="Submit"
                            onClick="return (isCorrectOcurringInput())" />

                    <input type="submit" name="action"
                           value="Cancel"/>
                </div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>