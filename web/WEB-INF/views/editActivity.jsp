<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
<head>
    <title>${action} Activity</title>
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
<h1>${action} Activity</h1>

<form action="<c:url value="/pages/newActivity"/>" method="post">
    <div class="row">
        <div class="label">
            <form:label path="name">Activity name</form:label>
        </div>
        <div class="value">
            <form:input path="name"/>
        </div>
    </div>
    <div class="row">
        <div class="label">
            <form:label path="description">Activity description</form:label>
        </div>
        <div class="value">
            <form:input path="descrtiption"/>
        </div>
    </div>
    <div class="row">
        <div class="value submit">
            <input type="submit" value="Create"/>
        </div>
    </div>
</form>

<h1>Activity Types</h1>
<table>
    <c:forEach items="${activityTypes}" var="type">
        <tr>
            <td>${type.name}</td>
        </tr>
    </c:forEach>
</table>


</body>
</html>