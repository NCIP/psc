<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>

<html>
<head>
    <title>Manage sites</title>
    <tags:includeScriptaculous/>
    <style type="text/css">
        form {
            width: 20em;
        }
    </style>
    <script type="text/javascript">
        function deleteSite(siteIdentifier) {
            var uri = SC.relativeUri("/api/v1/sites/") + siteIdentifier
            SC.asyncRequest(uri, {
                method: 'DELETE',
                onSuccess : function() {
                    window.location = SC.relativeUri("/pages/admin/manage/sites")
                }
            })
       }
    </script>
</head>
<body>
<laf:box title="Manage Sites">
    <laf:division>
        <p><a href="<c:url value="/pages/admin/manage/newSite"/>">Create New Site</a></p>

        <table>
            <c:forEach items="${sites}" var="site">
                <tr>
                    <td>${site.name}</td>
                    <td><a href="<c:url value="/pages/admin/manage/blackoutDates?site=${site.id}"/>">Manage Holidays and Weekends</a></td>
                    <td></td>
                    <td><a href="<c:url value="/pages/admin/manage/editSite?id=${site.id}"/>">Edit</a></td>
                       <c:forEach items="${enableDeletes}" var="enableDelete">
                        <c:if test="${site.id == enableDelete.key}">
                            <c:if test="${enableDelete.value==true}">
                               <td></td>
                               <td><a id="deleteSite" href="#deleteSite?site=${site.id}" onclick="deleteSite('${site.assignedIdentifier}')">Delete</a></td>
                            </c:if>
                       </c:if>
                     </c:forEach>
                </tr>
            </c:forEach>
        </table>
    </laf:division>
</laf:box>
</body>
</html>