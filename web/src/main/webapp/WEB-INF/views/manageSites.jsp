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
    <title>Manage sites</title>
    <tags:includeScriptaculous/>
    <style type="text/css">
        form {
            width: 20em;
        }
    </style>
    <script type="text/javascript">
        function deleteSite(siteIdentifier, siteName) {
            var msg = "This will permanently delete the site " +siteName +
                      ", including all user and study associations. You cannot undo this action."
                       + "\nAre you sure you wish to proceed?"
             if (window.confirm(msg)) {
                var uri = SC.relativeUri("/api/v1/sites/") + siteIdentifier
                SC.asyncRequest(uri, {
                    method: 'DELETE',
                    onSuccess : function() {
                        window.location = SC.relativeUri("/pages/admin/manage/sites")
                    },
                    onFailure: function(response) {
                        var msg = response.responseText;
                        var statusCode = response.status
                        var statusText = response.statusText
                        msg = msg.replace(statusCode, "").replace(statusText, "");
                        alert(msg);
                    }
                })
                return true;
             } else {
                return false;
             }
        }
        function displaySiteList() {
            var columnDefs = [
	            { key: "name", label: "Site Name ", sortable: true },
                { key: "blackoutdates", label: "Manage Blackout Dates" },
                { key: "controls", label: "Controls" },
                { key: "provider", label: "Provider" }
            ];
            var dataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("site-list-table"));
            dataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
            dataSource.responseSchema = {
                fields: [
                    { key: "name" },
                    { key: "blackoutdates" },
                    { key: "controls" },
                    { key: "provider" }
                    ]
	        };
            new YAHOO.widget.DataTable("site-list", columnDefs, dataSource, {
                sortedBy: { key: 'name' }
            });
        }
        $(document).observe("dom:loaded",  displaySiteList)

    </script>
</head>
<body>
<laf:box title="Manage Sites" cssClass="yui-skin-sam" autopad="true">
    <laf:division>
        <c:if test="${command.siteCreationEnabled}">
            <p><a href="<c:url value="/pages/admin/manage/newSite"/>">Create New Site</a></p>
        </c:if>

        <div id="site-list">
        <table id="site-list-table">
            <thead>
            </thead>
            <tbody>
            <c:forEach items="${command.manageableSites}" var="site">
                <tr>
                    <td>${site.name}</td>
                    <td><input type="button" name="manageBlackoutDates" value="Manage Blackout Dates"
                               onclick="location.href='<c:url value="/pages/admin/manage/blackoutDates?site=${site.id}"/>'"/>
                    </td>
                    <td>
                        <c:if test="${site.nameEditable && site.assignedIdentifierEditable}">
                            <input type="button" name="edit" value="Edit"
                               onclick="location.href='<c:url value="/pages/admin/manage/editSite?id=${site.id}"/>'"/>
                        </c:if>
                        <c:if test="${command.siteDeletionEnabled}">
                            <input type="button" name="delete" value="Delete" onclick="deleteSite('${site.assignedIdentifier}', '${site.name}')"/>
                        </c:if>
                    </td>
                    <td><c:if test="${not empty site.provider}">${site.provider}</c:if></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        </div>
    </laf:division>
</laf:box>
</body>
</html>