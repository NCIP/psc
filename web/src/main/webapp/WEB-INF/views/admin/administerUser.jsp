<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:useBean id="command" scope="request"
             type="edu.northwestern.bioinformatics.studycalendar.web.admin.AdministerUserCommand"/>
<jsp:useBean id="startingNewUser" scope="request"
             type="java.lang.Boolean"/>
<jsp:useBean id="roleGroupCells" scope="request"
             type="java.util.Collection"/>

<html>
<head>
    <title>
        <c:choose>
            <c:when test="${command.newUser}">Create New User</c:when>
            <c:otherwise>Provision User ${command.user.displayName}</c:otherwise>
        </c:choose>
    </title>
    <c:forEach items="${fn:split('yahoo-dom-event logger-min json-min', ' ')}" var="script">
        <tags:javascriptLink name="yui/2.7.0/${script}"/>
    </c:forEach>
    <tags:javascriptLink name="resig-templates"/>

    <script type="text/javascript">
        var PROVISIONABLE_SITES = ${command.javaScriptProvisionableSites};

        var PROVISIONABLE_ROLES = ${command.javaScriptProvisionableRoles};

        var PROVISIONABLE_STUDIES = ${command.javaScriptProvisionableStudies};
    </script>

    <tags:javascriptLink name="psc-tools/misc"/>
    <tags:javascriptLink name="admin/provisionable_user"/>
    <tags:javascriptLink name="admin/user_admin"/>
    <tags:javascriptLink name="jquery/jquery.tristate-checkbox"/>

    <tags:resigTemplate id="role_editor_template">
        <div id="role-general">
            <h3>[#= role.name #]</h3>
            <div class="content">
                <p>[#= role.description #]</p>
                [# if (role.scope_description) { #]
                <p>[#= role.scope_description #]</p>
                [# } #]
                <div class="row">
                    [# if (enableRoleControl) { #]
                        <div class="label">
                            <input type="checkbox" id="group-[#= role.key #]" class="role-group-membership" value="[#= role.key #]"/>
                        </div>
                        <div class="value"><label for="group-[#= role.key #]">
                            Grant this user the [#= role.name #] role.
                            [# if (role.scopes) { #]
                            Since this role is scoped, you will also need to specify one or more
                            scopes below.
                            [# } #]</label>
                        </div>
                    [# } else { #]
                        <div class="value"><label for="group-[#= role.key #]">
                            This user has the [#= role.name #] role.
                            [# if (role.scopes) { #]
                            Since this role is scoped, you will also need to specify one or more
                            scopes below.
                            [# } #]</label>
                        </div>
                    [# } #]
                </div>
            </div>
        </div>
        [# if (_(role.scopes || []).include("site")) { #]
        <div>
            <h3>Sites</h3>
            <div id="sites" class="content">
                [# if (enableSitesControl) { #]
                    [# _(sites).each(function (site) { #]
                        <div class="row">
                            <div class="label">
                                <input id="scope-site-[#= site.identifier #]"
                                    site-identifier="[#= site.identifier #]"
                                    class="scope-site [#= site.identifier == '__ALL__' ? 'all' : 'one' #]"
                                    type="checkbox"/>
                            </div>
                            <div class="value"><label for="scope-site-[#= site.identifier #]">[#= site.name #]</label></div>
                        </div>
                    [# }); #]
                [# } else {#]
                    <div class="row">
                        <div class="value">
                            <abbr title="This user has access to all sites in this role">
                                All
                            </abbr>
                        </div>
                    </div>
                [# } #]
            </div>
        </div>
        [# } #]
        [# if (_(role.scopes || []).include("study")) { #]
        <div>
            <h3>Studies</h3>
            <div id="studies" class="content">
                [# _(studies).each(function (study) { #]
                <div class="row">
                    <div class="label">
                        <input id="scope-study-[#= study.identifier #]"
                               study-identifier="[#= study.identifier #]"
                               class="scope-study [#= study.identifier == '__ALL__' ? 'all' : 'one' #]" 
                               type="checkbox"/>
                    </div>
                    <div class="value"><label for="scope-study-[#= study.identifier #]">[#= study.name #]</label></div>
                </div>
                [# }); #]
                [# if (role.study_team_member) { #]
                    <p>A Study Team Administrator will also be able to set study scope for this role.</p>
                [# } #]
            </div>
        </div>
        [# } #]
    </tags:resigTemplate>

    <tags:resigTemplate id="multiple_role_editor_template">
        [# var joinedRoleNames = utils.mapRoleNames(roles).join(', '); #]
        <div id="role-general">
            <h3>[#= joinedRoleNames #]</h3>
            <div class="content" id="multiple-role-content">
                <div style="position: relative;">
                    <c:forEach items="${roleGroupCells}" var="cell">
                        <c:if test="${cell.group != 'SUITE_ROLES'}">
                            <div class="abstract-role-grouping column column${cell.column} row${cell.row}">
                                <h2>${cell.group.displayName}</h2>
                                <c:forEach items="${cell.roles}" var="role">
                                    <div class="row">
                                        <div class="label">
                                            <input class="roles-to-edit" type="checkbox" name="roles_to_edit" value="${role.key}" id="${cell.group.key}_${role.key}"/>
                                        </div>
                                        <div class="value">
                                            <label for="${cell.group.key}_${role.key}">
                                                ${role.displayName}
                                            </label>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:if>
                    </c:forEach>
                    <c:forEach items="${command.provisionableRoleGroups}" var="map">
                        <c:if test="${map.key == 'SUITE_ROLES'}">
                            <div class="abstract-role-grouping column" id="suite-roles-cell">
                                <h2>${map.key.displayName}</h2>
                                <c:forEach items="${map.value}" var="role">
                                    <div class="row">
                                        <div class="label">
                                            <input class="roles-to-edit" type="checkbox" name="roles_to_edit" value="${role.key}"  id="${map.key.key}_${role.key}"/>
                                        </div>
                                        <div class="value">
                                            <label for="${map.key.key}_${role.key}">
                                                ${role.displayName}
                                            </label>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:if>
                    </c:forEach>
                </div>
            </div>
        </div>
        <div id="scope-container"></div>
    </tags:resigTemplate>

    <tags:resigTemplate id="role_and_scope_assignment_template">
        [# var joinedRoleNames = utils.mapRoleNames(roles).join(', '); #]
        [# var scopes = _(roles).map(function(r) {return r.scopes;}).flatten().uniq(); #]

        <div>
            <div class="row">
                [# if (enableRoleControl) { #]
                    <div class="label">
                        <input type="checkbox" id="multiple-group-membership" class="role-group-membership" value="[#= utils.mapRoleKeys(roles) #]"/>
                    </div>
                    <div class="value"><label for="multiple-group-membership">
                        Grant this user the [#= joinedRoleNames  #] roles.
                        [# if (_(roles).any(function(r){return r.scopes})) { #]
                        Since these roles are scoped, you will also need to specify one or more
                        scopes below.
                        [# } #]</label>
                        <label for="multiple-group-membership" id="partial-multiple-group-membership-info" class="partial-membership"></label>
                    </div>
                [# } else { #]
                    <div class="value"><label for="multiple-group-membership">
                        [# if (!_(roles).isEmpty()) { #]
                            This user has the [#= joinedRoleNames  #] role.
                        [# } #]
                        [# if (_(roles).any(function(r){return r.scopes})) { #]
                        Since this role is scoped, you will also need to specify one or more
                        scopes below.
                        [# } #]</label>
                    </div>
                [# } #]
            </div>
        </div>

        [# if (_(scopes || []).include("site")) { #]
        <div>
            <h3>Sites</h3>
            <div id="sites" class="content">
                [# if (enableSitesControl) { #]
                    [# _(sites).each(function (site) { #]
                        <div class="row">
                            <div class="label">
                                <input id="scope-site-[#= site.identifier #]"
                                    site-identifier="[#= site.identifier #]"
                                    class="scope-site [#= site.identifier == '__ALL__' ? 'all' : 'one' #]"
                                    type="checkbox"/>
                            </div>
                            <div class="value">
                                <label for="scope-site-[#= site.identifier #]">[#= site.name #]</label>
                                <label for="scope-site-[#= site.identifier #]" id="partial-scope-site-[#= site.identifier #]-info" class="partial-membership"></label>
                            </div>
                        </div>
                    [# }); #]
                [# } else {#]
                    <div class="row">
                        <div class="value">
                            <abbr title="This user has access to all sites in this role">
                                All
                            </abbr>
                        </div>
                    </div>
                [# } #]
            </div>
        </div>
        [# } #]
        [# if (_(scopes || []).include("study")) { #]
        <div>
            <h3>Studies</h3>
            <div id="studies" class="content">
                [# _(studies).each(function (study) { #]
                <div class="row">
                    <div class="label">
                        <input id="scope-study-[#= study.identifier #]"
                               study-identifier="[#= study.identifier #]"
                               class="scope-study [#= study.identifier == '__ALL__' ? 'all' : 'one' #]"
                               type="checkbox"/>
                    </div>
                    <div class="value">
                        <label for="scope-study-[#= study.identifier #]">[#= study.name #]</label>
                        <label for="scope-study-[#= study.identifier #]" id="partial-scope-study-[#= study.identifier #]-info" class="partial-membership"></label>
                    </div>
                </div>
                [# }); #]
            </div>
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
<laf:box title="Set properties and permissions for ${empty command.newUser ? 'new user' : command.user.displayName}" autopad="true">
    <form:form method="post" id="user-admin">
        <div class="row">
            <c:choose>
                <c:when test="${command.newUser}">
                    <div class="label">
                        <form:label path="user.csmUser.loginName">Username</form:label>
                    </div>
                    <div class="value">
                        <form:input path="user.csmUser.loginName" id="username"/>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="label">Username</div>
                    <div class="value">${command.user.csmUser.loginName}</div>
                </c:otherwise>
            </c:choose>
        </div>
        <c:if test="${not startingNewUser}">
            <div class="row">
                <div class="value"><tags:errors path="*"/></div>
            </div>
        </c:if>
        <div class="row">
            <div class="label">
                <form:label path="user.csmUser.emailId">E-mail address</form:label>
            </div>
            <div class="value">
                <form:input path="user.csmUser.emailId"/>
            </div>
        </div>
        <div class="row">
            <div class="label">
                <form:label path="user.csmUser.firstName">First name</form:label>
            </div>
            <div class="value">
                <form:input path="user.csmUser.firstName"/>
            </div>
        </div>
        <div class="row">
            <div class="label">
                <form:label path="user.csmUser.lastName">Last name</form:label>
            </div>
            <div class="value">
                <form:input path="user.csmUser.lastName"/>
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
            <div class="label">
                <form:label path="user.csmUser.endDate">End date</form:label>
            </div>
            <div class="value">
                <laf:dateInput path="user.csmUser.endDate"/>
                The day this account stops being valid.  Leave blank to have it never expire, or set it in the past to have it expire immediately.
            </div>
        </div>
        <div class="row">
            <div class="value">
                <input type="submit" value="Save"/>
            </div>
        </div>

        <form:hidden path="roleChanges" id="role-changes"/>

        <div id="role-manager">
            <div id="roles">
                <div class="role-tab">
                    <a id="role-multiple-roles" class="role" href="#">Edit Multiple Roles</a>
                </div>
                <c:forEach items="${command.provisionableRoles}" var="role">
                    <div class="role-tab" role-type="${role.pscRole ? 'psc' : 'suite'}">
                        <a id="role-${role.key}" class="role" href="#">${role.displayName}</a>
                        <div class="role-control">
                            <%--<input class="roles-to-edit" type="checkbox" name="roles_to_edit" value="${role.key}"/>--%>
                        </div>
                    </div>
                </c:forEach>
                <div class="role-manager-view-option">
                    <a href="#" id="show-all-toggle">Show Suite Roles</a>
                </div>
            </div>
            <div id="role-editor" class="role-editor">
                <h2>Role memberships</h2>
                <div id="role-editor-pane"></div>
            </div>
        </div>
    </form:form>
</laf:box>
</body>
</html>