<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>

<html>
<head>
    <%--<title>${action} Subject</title>--%>
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
<laf:box title="${action} Subject">
    <laf:division>
        <%--<h1>${action} Subject</h1>--%>
        <c:url value="/pages/cal/createSubject?id=${studyId}" var="formAction"/>
        <form:form action="${formAction}" method="post">
            <form:errors path="*"/>
                          
            <div class="row">
                <div class="label">
                    <form:label path="firstName">First Name</form:label>
                </div>
                <div class="value">
                    <form:input path="firstName"/>
                </div>
            </div>
            <div class="row">
                <div class="label">
                    <form:label path="lastName">Last Name</form:label>
                </div>
                <div class="value">
                    <form:input path="lastName"/>
                </div>
            </div>
            <div class="row">
                <div class="label">
                    <form:label path="dateOfBirth">Date of Birth (mm/dd/yyyy)</form:label>
                </div>
                <div class="value">
                    <form:input path="dateOfBirth"/>
                </div>
            </div>
            <div class="row">
                <div class="label">
                    <form:label path="gender">Gender</form:label>
                </div>
                <div class="value">
                    <form:select path="gender">
                        <form:options items="${genders}"/>
                    </form:select>
                </div>
            </div>
            <div class="row">
                <div class="label">
                    <form:label path="personId">Person Id</form:label>
                </div>
                <div class="value">
                    <form:input path="personId"/>
                </div>
            </div>
            <div class="row">
                <div class="submit">
                    <input type="submit" value="Create"/>
                </div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>