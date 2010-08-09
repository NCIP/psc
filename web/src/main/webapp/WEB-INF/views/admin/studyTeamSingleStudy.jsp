<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:useBean id="command" scope="request"
             type="edu.northwestern.bioinformatics.studycalendar.web.admin.SingleStudyStudyTeamMemberCommand"/>
<jsp:useBean id="roles" scope="request"
             type="edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole[]"/>

<html>
<head>
    <title>Update study team memberships for ${command.study.assignedIdentifier}</title>
    <tags:javascriptLink name="admin/provisionable_user"/>
    <tags:javascriptLink name="admin/study_team_single_study"/>
    <c:forEach items="${fn:split('yahoo-dom-event logger-min json-min', ' ')}" var="script">
        <tags:javascriptLink name="yui/2.7.0/${script}"/>
    </c:forEach>

    <script type="text/javascript">
        jQuery(document).ready(function () {
            psc.admin.team.SingleStudy.init("${command.study.assignedIdentifier}",
                ${command.javaScriptProvisionableUsers});
            jQuery('#single-study').submit(function (evt) {
                jQuery('#role-changes').attr('value', psc.admin.team.SingleStudy.serializeRoleChanges());
            });
        });
    </script>
</head>
<body>
<laf:box title="Update study team memberships for ${command.study.assignedIdentifier}">
    <laf:division>
        <form:form id="single-study" method="post">
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
                        <th>${membershipEntry.key.displayName}</th>
                        <c:forEach items="${membershipEntry.value}" var="roleEntry">
                            <c:choose>
                                <c:when test="${roleEntry.value.allStudiesForRole}">
                                    <td class="na">
                                        <abbr title="This user has access to all studies in this role">
                                            All
                                        </abbr>
                                    </td>
                                </c:when>
                                <c:when test="${roleEntry.value.hasRole}">
                                    <td>
                                        <input username="${roleEntry.value.user.username}"
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