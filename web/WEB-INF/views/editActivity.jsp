<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
<head>
    <title>${action} Activity</title>
    <tags:javascriptLink name="scriptaculous/scriptaculous"/>
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
<h1>${action} Activity</h1>
<c:url value="/pages/newActivity" var="formAction"/>
<form:form action="${formAction}" method="post">
    <div class="row">
        <div class="label">
            <form:label path="activityName">Activity name</form:label>
        </div>
        <div class="value">
            <form:input path="activityName"/>
        </div>
    </div>
    <div class="row">
        <div class="label">
            <form:label path="activityDescription">Activity description</form:label>
        </div>
        <div class="value">
            <form:input path="activityDescription"/>
        </div>
    </div>
    <div class="row">
        <div class="label">
            <form:label path="activityType">Activity type</form:label>
        </div>
        <div class="value">
            <form:select path="activityType">
                <form:options items="${activityTypes}" itemLabel="name" itemValue="id"/>
            </form:select>
        </div>
    </div>
    <div class="row">
        <div class="submit">
            <input type="submit" value="Create"/>
        </div>
    </div>
    <form:hidden path="returnToPeriodId"/>
</form:form>

</body>
</html>