<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>

<html>
<head>
    <!--<title>Add Planned Event</title>-->
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
<laf:box title="Add a planned event">
    <laf:division>
<!--<h2>Add a planned event</h2>-->

        <p>You are about to create a planned event for the period: ${period.name}. Please choose an activity for this event and the day of the period that the event should occur.</p>
        <c:url value="/pages/newPlannedEvent?id=${period.id}" var="formAction"/>
        <form:form action="${formAction}" method="post">
               <input type="hidden" name="periodId" value="${period.id}"/>
               <div class="row">
                <div class="label">
                    <form:label path="activityId">Activity</form:label>
                </div>
                <div class="value">
                    <form:select path="activityId">
                        <form:options items="${activities}" itemLabel="name" itemValue="id"/>
                    </form:select>
                </div>
            </div>
            <div class="row">
                <div class="label">
                    <form:label path="day">Day</form:label>
                </div>
                <div class="value">
                    <form:select path="day">
                        <c:forEach var="i" begin="${period.startDay}" end="${lastDay}" step="1">
                            <form:option label="${i}" value="${i}"/>
                        </c:forEach>
                    </form:select>
                </div>
            </div>
            <div class="row">
                <div class="submit">
                    <input type="submit" value="Create"/>
                </div>
            </div>
    </laf:division>
</laf:box>
</form:form>


</body>
</html>