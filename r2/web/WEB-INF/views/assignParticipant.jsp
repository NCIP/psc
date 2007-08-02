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
	Study : ${study.name}
</p>	
<c:url value="/pages/assignParticipant?id=${study.id}" var="formAction"/>

<form:form action="${formAction}" method="post">
<input type="hidden" name="studyId" value="${study.id}"/>
<input type="hidden" name="studySiteId" value="${studySite.id}"/>
	<div class="row">
        <div class="label">
            <form:label path="participantId">Participant</form:label>
        </div>
        <div class="value">
            <form:select path="participantId">
            	<form:options items="${participants}" itemLabel="firstName" itemValue="id"/>
            </form:select>
        </div>
	</div>
	<p><a href="<c:url value="/pages/createParticipant?id=${study.id}"/>">Create New Participant</a></p>
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