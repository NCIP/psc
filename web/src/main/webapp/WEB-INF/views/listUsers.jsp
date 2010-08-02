<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>

<html>
<head>
    <title>List Users</title>
    <tags:includeScriptaculous/>
    <style type="text/css">
        #container {
            width: 50em;background-color:#ddd; border:1px solid #999;
        }
        h2 {
            margin-bottom:0px;
            font-size:15px;
        }
        .nameColumn, .rolesColumn, .statusColumn {border-bottom:1px solid #999;}
        .nameColumn {
            width:20%;font-weight:bold;
        }
        .rolesColumn {
            width:60%;font-weight:bold;
        }
        .statusColumn {
            width:10%;font-weight:bold;
        }
        #container td {
            vertical-align:top; padding:2px;
        }
        tr.oddrow {
            background-color:#fff;
        }
    </style>
</head>
<body>
<laf:box title="List User">
    <laf:division>
        <div class="row">
            <a href="<c:url value="/pages/admin/manage/createUser"/>">Create User</a>
        </div>

        <table cellspacing="0" cellpadding="0" border="0" id="container">
            <tr>
                <td class="nameColumn">Name</td>
                <td class="rolesColumn">Role(s)</td>
                <td class="statusColumn">Status</td>
            </tr>

            <c:forEach items="${users}" var="user" varStatus="outterCounter">
                <tr class="<c:if test="${outterCounter.index%2 != 0}">oddrow</c:if>">
                    <td>
                        <a href="<c:url value="/pages/admin/manage/editUser?id=${user.id}"/>">${user.displayName}</a>
                        <a href="<c:url value="/pages/admin/manage/oneUser?user=${user.name}"/>">(new prov)</a>
                    </td>
                    <td>
                        <c:forEach items="${user.userRoles}" var="userRole" varStatus="innerCounter">
                            ${userRole.role.displayName}<c:if test="${not innerCounter.last}">,</c:if>
                        </c:forEach>
                    </td>
                    <td>
                        <c:if test="${user.activeFlag}">
                            Enabled
                        </c:if>
                        <c:if test="${not user.activeFlag}">
                            Disabled
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </laf:division>
</laf:box>
</body>
</html>