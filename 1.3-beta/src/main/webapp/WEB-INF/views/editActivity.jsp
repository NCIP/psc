<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>

<html>
<head>
    <tags:javascriptLink name="scriptaculous/scriptaculous"/>
    <style type="text/css">
        div.label {
            width: 35%;
        }
        div.submit {
            text-align: right;
        }
        form {
            width: 25em;
        }
    </style>
</head>
<body>
<laf:box title="${action} Activity">
    <laf:division>
        <c:url value="/pages/newActivity" var="formAction"/>
        <form:form action="${formAction}" method="post">
            <div style="height:10px;padding-bottom:20px; width:40em; color:red;">
                <form:errors path="*"/>
            </div>
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
                    <form:label path="activityCode">Activity code</form:label>
                </div>
                <div class="value">
                    <form:input path="activityCode"/>
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
                <div class="label">
                    Activity source
                </div>
                <div class="value">
                    ${sourceName}
                </div>
            </div>
            <div class="row">
                <div class="submit">
                    <input type="submit" value="Create"/>
                </div>
            </div>
            <form:hidden path="returnToPeriodId"/>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>