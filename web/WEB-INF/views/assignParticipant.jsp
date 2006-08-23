<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
<head>
    <title>${action} Participant</title>
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
<h1>${action} Participant</h1>
<p>
	Participant : ${participant.firstName} ${participant.lastName}
</p>	
<c:url value="/pages/assignParticipant" var="formAction"/>

<form:form action="${formAction}" method="post">
<input name="participantId" type="hidden" value="${participant.id}"/>
	<div class="row">
        <div class="label">
            <form:label path="studySiteId">Site</form:label>
        </div>
        <div class="value">
            <form:select path="studySiteId">
            	<form:options items="${studySites}" itemLabel="studyIdentifier" itemValue="id"/>
            </form:select>
        </div>
	</div>
	<div class="row">
        <div class="label">
            <form:label path="studyId">Study</form:label>
        </div>
        <div class="value">
            <form:select path="studyId">
            	<form:options items="${studies}" itemLabel="name" itemValue="id"/>
            </form:select>
        </div>
	</div>
	<div class="row">
        <div class="label">
            <form:label path="dateOfEnrollment">Date of Enrollment (mm/dd/yyyy)</form:label>
        </div>
        <div class="value">
            <form:input path="dateOfEnrollment"/>
        </div>
	</div>
	<div class="row">
        <div class="submit">
            <input type="submit" value="Assign"/>
        </div>
    </div>
</form:form>
</body>
</html>