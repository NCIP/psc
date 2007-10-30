<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>

<html>
<head>
    <!--<title>Manage sites</title>-->
    <tags:includeScriptaculous/>
    <style type="text/css">
        form {
            width: 20em;
        }
    </style>
</head>
<body>
<laf:box title="Manage Sites">
    <laf:division>
        <p><a href="<c:url value="/pages/admin/manage/newSite"/>">Create New Site</a></p>

        <table>
            <c:forEach items="${sites}" var="site">
                <tr>
                    <td><a href="<c:url value="/pages/admin/manage/holidays?site=${site.id}"/>">Manage Holidays and Weekends</a></td>
                </tr>
            </c:forEach>
        </table>
    </laf:division>
</laf:box>
</body>
</html>