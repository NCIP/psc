<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<html>
<title>Administration</title>
    <head>
        <style type="text/css">
            ul.menu {
                margin: 0;
                padding: 0;
            }
            ul.menu li {
                padding: 0.5em;
                list-style-type: none;
                margin: 0.5em;
            }
        </style>
    </head>
    <body>

    <laf:box title="Administration">
        <laf:division>
            <ul class="menu">
                <tags:restrictedListItem url="/pages/admin/manage/sites">Manage sites</tags:restrictedListItem>
                <tags:restrictedListItem url="/pages/admin/users">Manage users</tags:restrictedListItem>
                <tags:restrictedListItem url="/pages/admin/team">Manage study teams</tags:restrictedListItem>
                <tags:restrictedListItem url="/pages/admin/manage/sources">Set manual activity source</tags:restrictedListItem>
                <tags:restrictedListItem url="/pages/admin/configure">Configure PSC</tags:restrictedListItem>
                <tags:restrictedListItem url="/pages/admin/configureAuthentication">Configure authentication</tags:restrictedListItem>
                <tags:restrictedListItem url="/pages/admin/manage/plugins">Manage plugins (OSGi layer)</tags:restrictedListItem>
                <tags:restrictedListItem url="/pages/admin/diagnostics">Grid services configuration</tags:restrictedListItem>
                <tags:restrictedListItem url="/pages/admin/manage/purgeStudy">Purge study</tags:restrictedListItem>
            </ul>
        </laf:division>
    </laf:box>

    </body>
</html>