<jsp:useBean id="command" scope="request" type="edu.northwestern.bioinformatics.studycalendar.web.admin.ProvisionUserCommand"/>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<html>
<head>
    <title>Administer user ${command.user.loginName}</title>
    <c:forEach items="${fn:split('yahoo-dom-event logger-min json-min', ' ')}" var="script">
        <tags:javascriptLink name="yui/2.7.0/${script}"/>
    </c:forEach>
    <tags:javascriptLink name="resig-templates"/>

    <script type="text/javascript">
        var PROVISIONABLE_SITES = [
            { name: "All sites  (this user will have access in this role for all sites, including new ones as they are created)", identifier: "__ALL__" },
            <c:forEach items="${command.provisionableSites}" var="site" varStatus="status">
            { identifier: '${site.assignedIdentifier}', name: '${fn:replace(site.name, "'", "\\'")}' }
            ${status.last ? '' : ','}
            </c:forEach>
        ];

        var PROVISIONABLE_ROLES = [
            <c:forEach items="${command.provisionableRoles}" var="role" varStatus="status">
            {
                name: '${role.displayName}',
                key: '${role.csmName}',
                description: '${fn:replace(role.description, "'", "\\'")}',
                scopes:
                <c:choose>
                    <c:when test="${role.scoped}">
                        [
                           <c:forEach items="${role.scopes}" var="scope" varStatus="scopeStatus">
                                "<%= pageContext.getAttribute("scope").toString().toLowerCase() %>"
                                ${scopeStatus.last ? '' : ','}
                           </c:forEach>
                        ]
                    </c:when>
                    <c:otherwise>null</c:otherwise>
                </c:choose>
            }
            ${status.last ? '' : ','}
            </c:forEach>
        ];
    </script>

    <tags:javascriptLink name="admin/provisionable_user"/>
    <tags:javascriptLink name="admin/user_admin"/>

    <tags:resigTemplate id="role_editor_template">
        <div id="role-description">
            <h3>[#= role.name #]</h3>
            <p class="content">[#= role.description #]</p>
        </div>
        <div class="row">
            <div class="label"><input type="checkbox" id="group-[#= role.key #]" class="role-group-membership" value="[#= role.key #]"/></div>
            <div class="value">
                <label for="group-[#= role.key #]">
                    Grant this user the [#= role.name #] role.
                    [# if (role.scopes) { #]
                    Since this role is scoped, you will also need to specify one or more scopes below.
                    [# } #]
                </label>
            </div>
        </div>
        [# if (_(role.scopes || []).include("site")) { #]
        <div>
            <h3>Sites</h3>
            <table id="sites" class="content">
                [# _(PROVISIONABLE_SITES).each(function (site) { #]
                <tr>
                    <td><input id="scope-site-[#= site.identifier #]" site-identifier="[#= site.identifier #]" class="scope-site" type="checkbox"/></td>
                    <td><label for="scope-site-[#= site.identifier #]">[#= site.name #]</label></td>
                </tr>
                [# }); #]
            </table>
        </div>
        [# } #]
    </tags:resigTemplate>

    <tags:sassLink name="one-user"/>

    <script type="text/javascript">
        jQuery(document).ready(function () {
            psc.admin.UserAdmin.init(${command.javaScriptProvisionableUser});
            jQuery('#user-admin').submit(function (evt) {
                jQuery('#role-changes').attr('value', psc.admin.UserAdmin.serializeRoleChanges());
            })
        });
    </script>
</head>
<body>
<laf:box title="Set properties and permissions for ${command.user.loginName}">
    <form:form method="post" id="user-admin">
        <tags:errors path="*"/>
        <div class="row">
            <c:choose>
                <c:when test="${command.newUser}">
                    <div class="label">
                        <form:label path="user.loginName">Username</form:label>
                    </div>
                    <div class="value">
                        <form:input path="user.loginName"/>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="label">Username</div>
                    <div class="value">${command.user.loginName}</div>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="row">
            <div class="label">
                <form:label path="user.emailId">E-mail address</form:label>
            </div>
            <div class="value">
                <form:input path="user.emailId"/>
            </div>
        </div>
        <div class="row">
            <div class="label">
                <form:label path="user.firstName">First name</form:label>
            </div>
            <div class="value">
                <form:input path="user.firstName"/>
            </div>
        </div>
        <div class="row">
            <div class="label">
                <form:label path="user.lastName">Last name</form:label>
            </div>
            <div class="value">
                <form:input path="user.lastName"/>
            </div>
        </div>
        <c:if test="${command.usesLocalPasswords}">
            <div class="row">
                <div class="label">
                    <form:label path="password">Password</form:label>
                </div>
                <div class="value">
                    <form:password path="password"/>
                    <c:if test="${not command.newUser}">
                        If you don't want to change the user's password, leave these fields blank.
                    </c:if>
                </div>
            </div>
            <div class="row">
                <div class="label">
                    <form:label path="rePassword">Repeat password</form:label>
                </div>
                <div class="value">
                    <form:password path="rePassword"/>
                </div>
            </div>
        </c:if>
        <div class="row">
            <div class="value">
                <input type="submit" value="Save"/>
            </div>
        </div>

        <form:hidden path="roleChanges" id="role-changes"/>

        <div id="role-manager">
            <div id="roles">
                <c:forEach items="${command.provisionableRoles}" var="role">
                    <div class="role-tab">
                        <a id="role-${role.csmName}" class="role" href="#">${role.displayName}</a>
                        <div class="role-control">
                            <!-- TODO: use this -->
                            <input class="roles-to-edit" type="checkbox" name="roles_to_edit" value="${role.csmName}"/>
                        </div>
                    </div>
                </c:forEach>
            </div>
            <div id="role-editor">
                <h2>Role memberships</h2>
                <div id="role-editor-pane"></div>
            </div>
        </div>
    </form:form>
</laf:box>
</body>
</html>