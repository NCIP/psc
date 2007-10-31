<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<html>

<head>
    <title>Add an amendment for ${study.name}</title>
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

</head>
<body>
<laf:box title="Create amendment">
    <laf:division>
        <form:form method="post">
            <form:errors path="*"/>
            <div class="row">
                <div class="label" id="dateDescription">
                    <tags:requiredIndicator/> Amendment date 
                </div>
                <div class="value">
                    <laf:dateInput path="date"/>
                </div>
            </div>
            <div class="row">
                <div class="label">
                    Amendment name
                </div>
                <div class="value">
                    <form:input path="name" id="name"/>
                </div>
            </div>
            <div class="row" id="buttons">
                <div class="label">&nbsp;</div>
                <div class="value">
                    <input type="submit" name="action" value="Submit" />
                    <input type="submit" name="action" value="Cancel"/>
                </div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>