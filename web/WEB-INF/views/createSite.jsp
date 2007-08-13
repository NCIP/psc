<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
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
    </style>
</head>
<body>

<laf:box title="${action} site">
    <laf:division>
        <form:form method="post">
            <form:errors path="*"/>
            <div class="row">
                <div class="label">
                    <form:label path="name">Site Name</form:label>
                </div>
                <div class="value">
                    <form:input path="name"/>
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