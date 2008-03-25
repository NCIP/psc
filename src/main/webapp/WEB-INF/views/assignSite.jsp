<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>

<html>
<head>
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
        p {
            margin-left:1%;
        }
    </style>
</head>
<body>
<laf:box title="${action} Sites">
    <laf:division>
        <p>
            You are making <strong>${study.assignedIdentifier}</strong> available to specific
            sites.
        </p>
        <c:url value="/pages/cal/assignSite?id=${study.id}" var="formAction"/>

        <form:form action="${formAction}" method="post">
            <tags:errors/>

            <input type="hidden" name="studyId" value="${study.id}"/>
            <input type="hidden" name="assign" value="true"/>
            <div class="row">
                <div class="label">
                    <form:label path="availableSites">Available Sites</form:label>
                </div>
                <div class="value">
                    <form:select path="availableSites" multiple="true">
                        <form:options items="${availableSites}" itemLabel="name" itemValue="id"/>
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
            <input type="hidden" name="studyId" value="${study.id}"/>
            <input type="hidden" name="assign" value="false"/>
            <div class="row">
                <div class="label">
                    <form:label path="assignedSites">Assigned Sites</form:label>
                </div>
                <div class="value">
                    <form:select path="assignedSites" multiple="true">
                        <form:options items="${assignedSites}" itemLabel="name" itemValue="id" />
                    </form:select>
                </div>
            </div>
            <div class="row">
                <div class="submit">
                    <input type="submit" value="Remove"/>
                </div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>