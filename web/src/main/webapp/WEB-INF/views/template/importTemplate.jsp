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
            width: 25em;
        }
       div.errros{
           width:85em;
       }
        #submit-button {
            margin-top: 1em
        }
    </style>
</head>
<body>
<laf:box title="Import Template">
    <laf:division>
        <form:form method="post" enctype="multipart/form-data">
            <div class="errros">
                <form:errors path="*"/>
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