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
<laf:box title="${actionText} User">
    <laf:division>

        <form:form method="post">
            <form:hidden path="user.id"/>
            <form:errors path="*"/>
            <div class="row">
                <div class="label" >
                    <form:label path="user.name" >User Name:</form:label>
                </div>
                <div class="value">
                    <c:if test="${actionText=='Create'}">
                        <form:input path="user.name"/>
                    </c:if>
                    <c:if test="${actionText=='Edit'}">
                        ${command.user.name}
                    </c:if>
                </div>
            </div>
            <div class="row">
                <div class="label" >
                    <form:label path="user.plainTextPassword">Password:</form:label>
                </div>
                <div class="value">
                    <form:password path="user.plainTextPassword"/>
                </div>
            </div>
            <div class="row">
                <div class="label" >
                    <form:label path="rePassword">Re-Enter Password:</form:label>
                </div>
                <div class="value">
                    <form:password path="rePassword"/>
                </div>
            </div>
            <div class="row">
                <div class="label" >
                    <form:label path="user.activeFlag">Enable User:</form:label>
                </div>
                <div class="value">
                    <form:checkbox path="user.activeFlag" value=""/>
                </div>
            </div>
            <div class="row">
                <div class="label">
                    <h2>Please select a role</h2>
                </div>
            </div>
            <%--<c:forEach items="${roles}" var="role">--%>
                <!--<div class="row">-->
                    <!--<div class="label">-->
                        <%--<form:label path="userRoles">${user.Userrole.displayName}:</form:label>--%>
                    <!--</div>-->
                    <!--<div class="value">-->
                        <%--<form:checkbox path="userRoles" value="${role}"/>--%>
                    <!--</div>-->
                <!--</div>-->
            <%--</c:forEach>--%>
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
    </laf:division>
</laf:box>
</body>
</html>