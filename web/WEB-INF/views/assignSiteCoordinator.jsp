<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>

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
<laf:box title="Assign Site Coordinators to ${site.name}">

<form:form action="${formAction}" method="post">
<input type="hidden" name="siteId" value="${site.id}"/>
<input type="hidden" name="assign" value="true"/>
    <div class="row">
        <div class="label">
            <form:label path="availableCoordinators">Available Site Coordinators</form:label>
        </div>
        <div class="value">
            <form:select path="availableCoordinators" multiple="true">
                <form:options items="${availableUsers}" itemLabel="firstName" itemValue="userId"/>
            </form:select>
        </div>
    </div>
    <div class="row">
        <div class="submit">
            <input type="submit" value="Assign"/>
        </div>
    </div>
</form:form>

<form:form action="${formAction}" method="post">
<input type="hidden" name="siteId" value="${site.id}"/>
<input type="hidden" name="assign" value="false"/>
    <div class="row">
        <div class="label">
            <form:label path="assignedCoordinators">Assigned Site Coordinators</form:label>
        </div>
        <div class="value">
            <form:select path="assignedCoordinators" multiple="true">
                <form:options items="${assignedUsers}" itemLabel="firstName" itemValue="userId" />
            </form:select>
        </div>
    </div>
    <div class="row">
        <div class="submit">
            <input type="submit" value="Remove"/>
        </div>
    </div>
</form:form>
</laf:box>
</body>
</html>