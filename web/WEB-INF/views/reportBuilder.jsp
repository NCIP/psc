<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
<head>
    <title>Report Builder</title>
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
<h1>Report Builder</h1>

<form:form action="${formAction}" method="post">
    <div class="row">
        <div class="label">
            <form:label path="studies">Studies</form:label>
        </div>
        <div class="value">
            <form:select path="studies" multiple="true">
                <form:options items="${studies}" itemLabel="name" itemValue="id"/>
            </form:select>
        </div>
    </div>
    
    <div class="row">
        <div class="label">
            <form:label path="sites">Sites</form:label>
        </div>
        <div class="value">
            <form:select path="sites" multiple="true">
                <form:options items="${sites}" itemLabel="name" itemValue="id"/>
            </form:select>
        </div>
    </div>
    
    <div class="row">
        <div class="label">
            <form:label path="participants">Participants</form:label>
        </div>
        <div class="value">
            <form:select path="participants" multiple="true">
                <form:options items="${participants}" itemLabel="firstName" itemValue="id"/>
            </form:select>
        </div>
    </div>
    
    <div class="row">
        <div class="label">
            <label for="startTimeSelector">Start Time</label>
        </div>
        <div class="value">
            <form:input path="startDate"/>
        </div>
    </div>
             
    <div class="row">
        <div class="label">
            <label for="endTimeSelector">End Time</label>
        </div>
        <div class="value">
            <form:input path="endDate"/>
        </div>
    </div>
    
    <div class="row">
        <div class="submit">
            <input type="submit" value="Report"/>
        </div>
    </div>
</form:form>
</body>
</html>