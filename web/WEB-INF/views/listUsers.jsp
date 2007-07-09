<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
<head>
    <title>New User</title>
    <tags:includeScriptaculous/>
    <style type="text/css">
        #container {
            width: 50em;background-color:#eee;
        }
        h2 {
            margin-bottom:0px;
            font-size:15px;
        }
        .nameColumn {
            width:20%;font-weight:bold;
        }
        .rolesColumn {
            width:60%;font-weight:bold;
        }
        .statusColumn {
            width:10%;font-weight:bold;
        }
    </style>
</head>
<body>

    <h1>List User</h1>
    
    <div class="row">
        <a href="<c:url value="/pages/createUser"/>">Create User</a>
    </div>

        <h3>User List</h3>
    <table cellspacing="0" cellpadding="0" border="0" id="container">
        <tr>
            <td class="nameColumn">Name</td>
            <td class="rolesColumn">Role(s)</td>
            <td class="statusColumn">Status</td>
        </tr>    

        <c:forEach items="${users}" var="user">
            <tr>
            <td>
                <a href="<c:url value="/pages/createUser?editId=${user.id}"/>">${user.name}</a></td>
            <td>
                <c:forEach items="${user.roles}" var="role" varStatus="counter">
                    ${role.displayName}<c:if test="${not counter.last}">,</c:if>                                                                
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
</body>
</html>