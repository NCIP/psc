<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>

<html>
<head>
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
</head>
<body>

<laf:box title="${action} site">
    <laf:division>
        <form:form method="post">
            <form:errors path="*"/>
            <div class="row">
                <div class="label">
                    Site Name
                </div>
                <div class="value">
                    <input id="name" name="name" value="${name.name}" size="30"/>
                </div>
                <div class="label">
                    Assigned Identifier
                </div>
                <div class="value">
                    <input id="assignedIdentifier" name="assignedIdentifier" value="${assignIdentifier}" size="30" />
                </div>
            </div>
            <div class="row">
                <div class="submit">
                    <input type="submit" value="Save"/>
                </div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>