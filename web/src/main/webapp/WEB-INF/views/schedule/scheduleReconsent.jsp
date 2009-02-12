<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" uri="/WEB-INF/tags/laf.tld"%>
<html>

<head>
    <tags:stylesheetLink name="main"/>
    <tags:javascriptLink name="main"/>

    <title>Schedule Reconsent</title>
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
</head>
<body>
<laf:box title="Schedule Reconsent">
    <laf:division>
        <!--<h1>Schedule Reconsent</h1>-->
        <c:url value="/pages/cal/scheduleReconsent" var="action"/>
        <form:form method="post"action="${action}">
            <form:hidden path="study"/>
            <form:errors path="*"/>
            <div class="row">
                <div class="label">
                    Reconsent Details:
                </div>
                <div class="value">
                    <form:input path="details"/>
                </div>
            </div>
            <div class="row">
                <div class="label">
                    Start Date:<br/>
                    (mm/dd/yyyy)
                </div>
                <div class="value">
                    <laf:dateInput path="startDate"/>
                </div>

            </div>
            <div class="row" id="buttons">
                <div class="label">&nbsp;</div>
                <div class="value">
                    <input type="submit"
                           value="Schedule"/>
                    <input type="submit"
                           name="_cancel"
                           value="Cancel"/>
                </div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>