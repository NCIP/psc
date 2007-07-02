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
            width: 85%;
        }
        div.submit {
            text-align: right;
        }
        form {
            width: 20em;
        }
    </style>
</head>
<body>
<h1>New User</h1>


<form:form method="post">
    <form:errors path="*"/>
    <div class="row">
        <div class="label">
            <form:label path="name">User Name</form:label>
        </div>
        <div class="value">
            <form:input path="name"/>
        </div>
    </div>
    <c:forEach items="${roles}" var="role">
        <div class="row">
            <div class="label">
                <form:label path="userRoles">${role.displayName}</form:label>
            </div>
            <div class="value">
                    <form:checkbox path="userRoles" value="${role}"/>
            </div>
        </div>
    </c:forEach>
    <div class="row">
        <div class="submit">
            <input type="submit" value="Create"/>
        </div>
    </div>
</form:form>
</body>
</html>