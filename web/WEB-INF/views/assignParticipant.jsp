<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>

<html>
<head>
    <%--<title>${action} Participant</title>--%>
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
<laf:box title="Assign Participant">
    <laf:division>
        <!--<h1>Assign Participant</h1>-->
        <p>
            Study: ${study.name}
        </p>

        <form:form method="post">
            <form:errors path="*"/>
            <input type="hidden" name="studySite" value="${studySite.id}"/>
            <div class="row">
                <div class="label">
                    <form:label path="participant">Participant</form:label>
                </div>
                <div class="value">
                    <form:select path="participant">
                        <form:options items="${participants}" itemLabel="fullName" itemValue="id"/>
                    </form:select>
                </div>
            </div>
            <p><a href="<c:url value="/pages/createParticipant?id=${study.id}"/>">Create New Participant</a></p>

            <c:if test="${not empty arms}">
                <div class="row">
                    <div class="label">
                        <form:label path="arm">Select arm for first epoch</form:label>
                    </div>
                    <div class="value">
                        <form:select path="arm">
                            <form:options items="${arms}" itemLabel="name" itemValue="id"/>
                        </form:select>
                    </div>
                </div>
            </c:if>

            <div class="row">
                <div class="label">
                    <form:label path="startDate">Start date of first epoch (mm/dd/yyyy)</form:label>
                </div>
                <div class="value">
                    <form:input path="startDate"/>
                </div>
            </div>
            <div class="row">
                <div class="submit">
                    <input type="submit" value="Assign"/>
                </div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>