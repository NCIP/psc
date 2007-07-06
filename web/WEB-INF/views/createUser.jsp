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
        div.label {
            width: 50%;
        }
        div.submit {
            text-align: right;
        }
        form {
            width: 30em;
        }
        div {
            /*border: 1px solid #000;*/
        }
        h2 {
            margin-bottom:0px;
            font-size:15px;
        }
    </style>
</head>
<body>
<h1>${actionText} User</h1>

<c:url value="/pages/newUser" var="formAction"/>
<form:form method="post" action="${formAction}">
    <form:hidden path="id"/>
    <form:errors path="*"/>
    <div class="row">
        <div class="label" >
            <form:label path="name">User Name:</form:label>
        </div>
        <div class="value">
            <form:input path="name"/>
        </div>
    </div>
    <div class="row">
        <div class="label" >
            <form:label path="name">Enable User:</form:label>
        </div>
        <div class="value">
            <form:checkbox path="activeFlag" value=""/>
        </div>
    </div>
    <div class="row">
        <div class="label">
            <h2>Please select a role</h2>
        </div>
    </div>
    <c:forEach items="${roles}" var="role">
        <div class="row">
            <div class="label">
                <form:label path="userRoles">${role.displayName}:</form:label>
            </div>
            <div class="value">
                    <form:checkbox path="userRoles" value="${role}"/>
            </div>
        </div>
    </c:forEach>
    <div class="row">
        <div class="submit">
            <input type="submit"
                   name="action"
                   value="<c:if test="${actionText=='Create'}">Create</c:if><c:if test="${actionText=='Edit'}">Save</c:if>"/>
        </div>
    </div>
    <div class="row">
        <a href="<c:url value="/pages/newUser"/>">Create User</a>
    </div>
    <div class="row">
        <h3>User List</h3>
        <ul>
            <c:forEach items="${users}" var="user">               
                <li>${user.name} - <a href="<c:url value="/pages/newUser?editId=${user.id}"/>">Edit User</a> -
                    <c:if test="${user.activeFlag}">
                        Enabled
                    </c:if>
                    <c:if test="${not user.activeFlag}">
                        Disabled
                    </c:if>                    
               </li>
            </c:forEach>
        </ul>
    </div>
    <div class="row">
        <
    </div>
</form:form>
</body>
</html>