<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<html>
<head>
    <title>Add Period</title>
    <style type="text/css">
        form { width: 20em; }
        div.label { width: 35%; }
    </style>
</head>
<body>
    <h2>Add Period</h2>
    <c:url value="/pages/newPeriod" var="action"/>
    <form:form action="${action}" method="post">
        <input type="hidden" name="armId" value="${arm.id}"/>
        <div class="row">
            <div class="label">
                <form:label path="name">Name</form:label>
            </div>
            <div class="value">
                <form:input path="name"/>
            </div>
        </div>
        <div class="row">
            <div class="label">
                <form:label path="startDay">Start day</form:label>
            </div>
            <div class="value">
                <form:input path="startDay" size="3" maxlength="3"/>
            </div>
        </div>
        <div class="row">
            <div class="label">
                <label for="duration.quantity">Duration</label>
            </div>
            <div class="value">
                <form:input path="duration.quantity" size="3" maxlength="3"/>
                <form:select path="duration.unit">
                    <form:options items="${durationUnits}"/>
                </form:select>
            </div>
        </div>
        <div class="row">
            <div class="label">
                <form:label path="repetitions">Repetitions</form:label>
            </div>
            <div class="value">
                <form:input path="repetitions" size="3" maxlength="3"/>
            </div>
        </div>
        <div class="submit"><input id="submit" type="submit" value="Add"/></div>
    </form:form>
</body>
</html>
