<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
<head>
    <title>Assign Study Templates To Participant Coordinator</title>
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
<h1>Assign Study Templates To Participant Coordinator ${participantcoordinator.name}</h1>
<div><a href="<c:url value="/pages/studyList"/>">Calendar Menu</a></div>

<form:form action="${formAction}" method="post">
<input type="hidden" name="siteId" value="${site.id}"/>
<input type="hidden" name="pcId" value="${participantcoordinator.userId}"/>
<input type="hidden" name="assign" value="true"/>
    <div class="row">
        <div class="label">
            <form:label path="availableTemplates">Available Study Templates</form:label>
        </div>
        <div class="value">
            <form:select path="availableTemplates" multiple="true">
                <form:options items="${availableTemplates}" itemLabel="protectionElementName" itemValue="protectionElementId"/>
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
<input type="hidden" name="pcId" value="${participantcoordinator.userId}"/>
<input type="hidden" name="assign" value="false"/>
    <div class="row">
        <div class="label">
            <form:label path="assignedTemplates">Assigned Study Templates</form:label>
        </div>
        <div class="value">
            <form:select path="assignedTemplates" multiple="true">
                <form:options items="${assignedTemplates}" itemLabel="protectionElementName" itemValue="protectionElementId" />
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