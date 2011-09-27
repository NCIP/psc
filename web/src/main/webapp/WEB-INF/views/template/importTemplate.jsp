<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>

<html>
<head>
    <title>Import Template</title>

    <style type="text/css">
        div.label {
            width: 35%;
        }

        div.submit {
            text-align: right;
        }

        form {
            width: auto;
        }
        #submit-button {
            margin-top: 1em
        }
        div.errors {
            margin-bottom: 1em;
            color: #900;
        }
        div.errors ul {
            margin: 0.25em 0; padding: 0
        }
        div.errors ul li {
            margin: 0 2em; padding: 0
        }
    </style>
</head>
<body>
<laf:box title="Import Template">
    <laf:division>
        <form:form method="post" enctype="multipart/form-data">
            <div class="errors">
                <form:errors path="*" htmlEscape="false"/>
            </div>
            <div class="row">
                <div class="label">
                    <form:label path="studyXml">Template File (xml):</form:label>
                </div>
                <div class="value">
                    <input type="file" name="studyXml"/>
                </div>
            </div>

            <div class="row">
                <div class="label">&nbsp;</div>
                <div class="submit" style="text-align:left">
                    <input type="submit" id="submit-button" value="Import"/>
                </div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>