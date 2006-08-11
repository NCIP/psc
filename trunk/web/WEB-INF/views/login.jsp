<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
<head>
    <title>Account Login</title>
    <tags:javascriptLink name="scriptaculous/scriptaculous"/>
    <style type="text/css">
        div.label {
            width: 35%;
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
<h1>Please enter your userid and password</h1>
<c:url value="/pages/login" var="formAction"/>
<form:form action="${formAction}" method="post">
    <div class="row">
        <div class="label">
            <form:label path="userId">User Id</form:label>
        </div>
        <div class="value">
            <form:input path="userId"/>
        </div>
    </div>
    <div class="row">
        <div class="label">
            <form:label path="password">Password</form:label>
        </div>
        <div class="value">
            <form:password path="password"/>
        </div>
    </div>
    <div class="row">
        <div class="submit">
            <input type="submit" value="Login"/>
        </div>
    </div>
</form:form>

</body>
</html>
