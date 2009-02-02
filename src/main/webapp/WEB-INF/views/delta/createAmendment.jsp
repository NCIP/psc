<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" uri="/WEB-INF/tags/laf.tld"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<html>

<head>
    <title>Add an amendment</title>
    <tags:stylesheetLink name="main"/>
    <tags:javascriptLink name="main"/>
    <style type="text/css">
        div.label {
            width: 25%;
        }
        form {
            width: 40em;
        }
        #buttons {
            margin-top:10px;
        }
        p.tip {
            line-height: 110%;
            margin-left: 3em;
        }
    </style>

</head>
<body>
<laf:box title="Create amendment">
    <laf:division>
        <form:form method="post">
            <form:errors path="*"/>
            <div class="row">
                <div class="label" id="dateDescription">
                    <form:label path="date"><tags:requiredIndicator/> Amendment date</form:label> 
                </div>
                <div class="value">
                    <laf:dateInput path="date"/>
                </div>
            </div>
            <div class="row">
                <div class="label">
                    <form:label path="name">Amendment name</form:label>
                </div>
                <div class="value">
                    <form:input path="name" id="name"/>
                </div>
            </div>
            <div class="row">
                <div class="label">
                    Mandatoriness
                </div>
                <div class="value">
                    <div>
                        <form:radiobutton path="mandatory" value="true" />&nbsp;Mandatory
                        <p class="tip">
                            All subjects currently on this study must adopt the schedule in
                            this amendment in order to remain in the trial.  When the
                            amendment is released and approved, existing subject schedules will
                            automatically be amended to match.
                        </p>
                    </div>
                    <div>
                        <form:radiobutton path="mandatory" value="false"/>&nbsp;Non-mandatory
                        <p class="tip">
                            Subjects currently in the study have the option of staying on
                            their current schedules instead of adopting this amendment's changes.
                            When the amendment is released and approved, no existing subject
                            schedules will be automatically changed.
                        </p>
                    </div>
                </div>
            </div>
            <div class="row" id="buttons">
                <div class="label">&nbsp;</div>
                <div class="value">
                    <input type="submit" value="Submit" />
                    <input type="submit" name="_cancel" value="Cancel"/>
                </div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>