<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>

<html>
<head>
    <tags:stylesheetLink name="yui-sam/2.7.0/datatable"/>
    <c:forEach items="${fn:split('yahoo-dom-event element-min datasource-min logger-min json-min connection-min get-min datatable-min', ' ')}" var="script">
        <tags:javascriptLink name="yui/2.7.0/${script}"/>
    </c:forEach>
    <tags:javascriptLink name="psc-tools/misc"/>
    <tags:javascriptLink name="source/manage-sources"/>
    <title>Manage sources</title>
    <style type="text/css">
        form {
            width: 20em;
        }
    </style>
</head>
<body>
<laf:box title="Manage Sources" cssClass="yui-skin-sam" autopad="true">
    <laf:division>
        <div id="source-list">
        <table id="source-list-table">
            <thead>
            </thead>
            <tbody>
            <c:forEach items="${sources}" var="source">
                <tr>
                    <td>${source.name}</td>
                    <td><Input type= radio name="radioGroup" <c:if test="${source.manualFlag}"> checked="true"</c:if> class="radioButton" sourceName="${source.name}"/> </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        </div>
        <div class="row">
            <div class="submit">
                <input type="button" name="updateManualActivityTarget" value="Update Manual Activity Target" id="update-manual-activity-target"/>
            </div>
        </div>
    </laf:division>
</laf:box>
</body>
</html>