<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn" %>

<html>
<head>
    <title>New User</title>
    <tags:includeScriptaculous/>

    <script type="text/javascript">
        <c:if test="${actionText=='Edit'}">
        function registerEditPasswordLink() {
            var aElement = $('changePassword')
            Event.observe(aElement, "click", function(e) {
                Event.stop(e)
                $('passwordField').enable();
                $('rePasswordField').enable();
                $('cancelChangePassword').show()
                $('changePassword').hide();
                $('passwordModified').value = true;
            })
        }

        function registerCancelEditPasswordLink() {
            var aElement = $('cancelChangePassword')
            Event.observe(aElement, "click", function(e) {
                Event.stop(e)
                $('passwordField').clear();
                $('rePasswordField').clear();
                $('passwordField').disable();
                $('rePasswordField').disable();
                $('cancelChangePassword').hide()
                $('changePassword').show();
                $('passwordModified').value = false;
            })
        }

        Event.observe(window, "load", registerEditPasswordLink);
        Event.observe(window, "load", registerCancelEditPasswordLink);
        </c:if>
    </script>
    <style type="text/css">
        div.label {
            width: 50%;
        }

        div.submit {
            text-align: left;
        }

        form {
            width: 40em;
        }

        h2 {
            margin-bottom: 0px;
            font-size: 15px;
        }

        table.siteRoles td, table.siteRoles th {
            text-align: center;
            padding: .5em;
        }

        table.siteRoles td.notSiteSpecific {
            vertical-align: top;
        }

        table.siteRoles th {
            background-color:#ccc
        }

        .password-edit-link {
            color:#0000cc;
            cursor:pointer;
            white-space:nowrap;
        }

    </style>
</head>
<body>
<laf:box title="${actionText} User">
    <laf:division>

        <form:form method="post">
            <form:hidden path="user.id"/>
            <form:hidden path="passwordModified"/> 
            <form:errors path="*"/>
            <c:set var="passwordError" value="false"/>
            <spring:bind path="command.password">
                <c:set var="passwordError" value="${status.errors.errorCount > 0}"/>
            </spring:bind>
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
                    <form:label path="password">Password:</form:label>
                </div>
                <div class="value">
                    <form:password path="password" id="passwordField" disabled="${actionText=='Edit' and not passwordError}"/>

                    <c:if test="${actionText == 'Edit'}">
                        <span id="changePassword" class="password-edit-link" style="<c:if test="${passwordError}">display:none</c:if>">Change Password</span>
                        <span id="cancelChangePassword" class="password-edit-link" style="<c:if test="${not passwordError}">display:none</c:if>">Undo Password Changes</span>
                    </c:if>
                </div>
            </div>
            <div class="row">
                <div class="label" >
                    <form:label path="rePassword">Re-Enter Password:</form:label>
                </div>
                <div class="value">
                    <form:password path="rePassword" id="rePasswordField" disabled="${actionText=='Edit' and not passwordError}"/>
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
                <div class="value">
                    <table cellspacing="0" cellpading="0" border="1" class="siteRoles">
                        <tr>
                            <th></th>
                            <c:forEach items="${roles}" var="role">
                                <th>${role.displayName}</th>
                            </c:forEach>

                        </tr>
                        <c:forEach items="${command.rolesGrid}" var="site" varStatus="index">
                            <tr>
                                <th>${site.key.name}</th>

                                <c:forEach items="${roles}" var="role">
                                    <c:if test="${not role.siteSpecific && index.first}">
                                        <td <c:if test="${not role.siteSpecific && index.first}">rowspan="${fn:length(command.rolesGrid)}"</c:if> class="notSiteSpecific">
                                            <form:checkbox path="rolesGrid[${site.key.id}][${role}].selected"/>
                                        </td>
                                    </c:if>
                                    <c:if test="${role.siteSpecific}">
                                        <td>
                                            <form:checkbox path="rolesGrid[${site.key.id}][${role}].selected"/>
                                        </td>
                                    </c:if>
                                </c:forEach>
                            </tr>
                        </c:forEach>
                    </table>
                </div>
            </div>


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