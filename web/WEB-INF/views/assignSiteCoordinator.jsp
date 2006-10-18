<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
<head>
    <title>Assign Site Coordinators to Site</title>
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
<h1>Assign Site Coordinators to ${site.name}</h1>
<div><a href="<c:url value="/pages/studyList"/>">Calendar Menu</a></div>

<form:form action="${formAction}" method="post">
<input type="hidden" name="siteId" value="${site.id}"/>
<input type="hidden" name="available" value="assign"/>
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
<input type="hidden" name="assign" value="remove"/>
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
</body>
</html>