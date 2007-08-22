<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<html>

<head>
    <tags:stylesheetLink name="main"/>
    <tags:javascriptLink name="main"/>
    <style type="text/css">
        div.label {
            width: 25%;
        }
        div.submit {
            text-align: left;
        }
        form {
            width: 27em;
        }
        #buttons {
            margin-top:10px;
        }
    </style>
</head>
<body>
<laf:box title="Amendment Login">
    <laf:division>
        <c:url value="/pages/cal/amendmentLogin" var="action"/>
        <form:form method="post"action="${action}">
            <form:hidden path="study"/>
            <%--<form:errors path="*"/>--%>
            <div class="row">
                <div class="label">
                    Amendment Number:
                </div>
                <div class="value">
                    <form:input path="amendmentNumber"/>
                </div>
            </div>
            <div class="row">
                <div class="label">
                    Amendment Date:<br/>
                    (mm/yyyy)
                </div>
                <div class="value">
                    <form:input path="date"/>
                </div>
            </div>
            <div class="row" id="buttons">
                <div class="label">&nbsp;</div>
                <div class="value">
                    <input type="submit" name="action"
                           value="Submit"/>
                    <input type="submit" name="action"
                           value="Cancel"/>
                </div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>