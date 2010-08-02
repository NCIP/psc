<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>

<jsp:useBean id="users" scope="request"
             type="java.util.List<edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser>"/>

<html>
<head>
    <title>List Users</title>
    <tags:stylesheetLink name="yui-sam/2.7.0/datatable"/>
    <c:forEach items="${fn:split('yahoo-dom-event element-min datasource-min logger-min json-min connection-min get-min datatable-min', ' ')}" var="script">
        <tags:javascriptLink name="yui/2.7.0/${script}"/>
    </c:forEach>
    <script type="text/javascript">
        var USERS = [
            <c:forEach items="${users}" var="user" varStatus="uStatus">
            {
                username: '${user.username}',
                active: ${user.active},
                name: '${user.displayName}',
                last_first: '${user.lastFirst}'
            }<c:if test="${not uStatus.last}">,</c:if>
            </c:forEach>
        ];

        jQuery(function () {
            var usersDataSource = new YAHOO.util.LocalDataSource(USERS);
            usersDataSource.responseSchema = {
                fields: ['username', 'name', 'active', 'last_first']
            };
            var usersDataTable = new YAHOO.widget.DataTable("users", [
                { key: "username",   label: "Username", sortable: true },
                { key: "last_first", label: "Name",     sortable: true,
                    formatter: function (elCell, oRecord, oColumn, oData) {
                        elCell.innerHTML = "<a href='<c:url value="/pages/admin/users/one?user="/>" + oRecord.getData('username') + "'>" + oData + "</a>";
                    }
                },
                { key: "active",     label: "Status",  sortable: true,
                    formatter: function (elCell, oRecord, oColumn, oData) {
                        if (!oData) elCell.innerHTML = "Disabled";
                    }
                }
            ], usersDataSource)
        });
    </script>
</head>
<body>
<laf:box title="List users" cssClass="yui-skin-sam">
    <laf:division>
        <div class="row">
            <a href="<c:url value="/pages/admin/users/one"/>">Create user</a>
        </div>

        <div id="users">
            <tags:activityIndicator/> Users loading...
        </div>
    </laf:division>
</laf:box>
</body>
</html>