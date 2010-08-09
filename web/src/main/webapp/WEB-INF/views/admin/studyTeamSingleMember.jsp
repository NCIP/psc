<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:useBean id="command" scope="request"
             type="edu.northwestern.bioinformatics.studycalendar.web.admin.SingleMemberStudyTeamMemberCommand"/>
<jsp:useBean id="roles" scope="request"
             type="edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole[]"/>

<html>
<head>
    <title>Update study team memberships for ${command.user.displayName}</title>
    <tags:javascriptLink name="admin/provisionable_user"/>
    <tags:javascriptLink name="admin/study_team_single_member"/>
    <tags:stylesheetLink name="yui-sam/2.7.0/datatable"/>
    <c:forEach items="${fn:split('yahoo-dom-event element-min datasource-min logger-min json-min connection-min get-min datatable-min', ' ')}" var="script">
        <tags:javascriptLink name="yui/2.7.0/${script}"/>
    </c:forEach>

    <script type="text/javascript">
        jQuery(document).ready(function () {
            psc.admin.team.SingleMember.init(${command.javaScriptProvisionableUser});
            jQuery('#single-member').submit(function (evt) {
                jQuery('#role-changes').attr('value', psc.admin.team.SingleMember.serializeRoleChanges());
            });
        });
    </script>
</head>
<body>
<laf:box title="Update study team memberships for ${command.user.displayName}">
    <laf:division>
        <form:form id="single-member" method="post">
            <form:hidden path="roleChanges" id="role-changes"/>
            <div class="row">
                <input type="submit" value="Save"/>
            </div>
            <table class="grid">
                <tr>
                    <th></th>
                    <c:forEach items="${roles}" var="role">
                        <th>${role.displayName}</th>
                    </c:forEach>
                </tr>
                <c:forEach items="${command.teamMemberships}" var="membershipEntry">
                    <tr>
                        <th>${membershipEntry.key == '__ALL__' ? 'All studies' : membershipEntry.key}</th>
                        <c:forEach items="${membershipEntry.value}" var="roleEntry">
                            <c:choose>
                                <c:when test="${roleEntry.value.hasRole}">
                                    <td>
                                        <input study-identifier="${roleEntry.value.studyIdentifier}"
                                               role="${roleEntry.value.role.csmName}" type="checkbox"
                                               class="study-role-control role-${roleEntry.value.role.csmName}"
                                               ${roleEntry.value.scopeIncluded ? 'checked' : ''}/>
                                    </td>
                                </c:when>
                                <c:otherwise>
                                    <td class="na">
                                        <acronym title="Not applicable: this user doesn't have this role in an appropriate scope">
                                            N/A
                                        </acronym>
                                    </td>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </tr>
                </c:forEach>
            </table>
            <div class="row">
                <input type="submit" value="Save"/>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>