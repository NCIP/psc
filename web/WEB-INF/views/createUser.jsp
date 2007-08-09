<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>

<html>
<head>
    <title>New User</title>
    <tags:includeScriptaculous/>
    <style type="text/css">
        div.label {
            width: 50%;
        }
        div.submit {
            text-align: left;
        }
        form {
            width: 28em;
        }
        h2 {
            margin-bottom:0px;
            font-size:15px;
        }
    </style>
</head>
<body>
<laf:box title="List User">
<h1>${actionText} User</h1>

<form:form method="post">
    <form:hidden path="id"/>
    <form:errors path="*"/>
    <div class="row">
        <div class="label" >
            <form:label path="name" >User Name:</form:label>
        </div>
        <div class="value">
            <c:if test="${actionText=='Create'}">
                <form:input path="name"/>
            </c:if>
            <c:if test="${actionText=='Edit'}">
                ${command.name}
            </c:if>
        </div>
    </div>
    <div class="row">
        <div class="label" >
            <form:label path="name">Password:</form:label>
        </div>
        <div class="value">
            <form:password path="password"/>
        </div>
    </div>
        <div class="row">
        <div class="label" >
            <form:label path="name">Re-Enter Password:</form:label>
        </div>
        <div class="value">
            <form:password path="rePassword"/>
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
        <div class="label">&nbsp;</div>
        <div class="submit">
            <input type="submit"
                   value="<c:if test="${actionText=='Create'}">Create</c:if><c:if test="${actionText=='Edit'}">Save</c:if>"/>
            <input type="submit"
                   name="_cancel"
                   value="Cancel"/>            
        </div>
    </div>
</form:form>
</laf:box>
</body>
</html>