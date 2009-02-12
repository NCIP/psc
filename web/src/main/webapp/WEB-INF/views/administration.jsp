<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="laf" uri="/WEB-INF/tags/laf.tld"%>
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

    <security:secureOperation element="/pages/admin/manage/sites">
        <laf:box title="Administration">
            <laf:division>
                <ul class="menu">
                    <tags:restrictedListItem url="/pages/admin/manage/sites">Manage sites</tags:restrictedListItem>
                    <tags:restrictedListItem url="/pages/admin/manage/listUsers">Manage users</tags:restrictedListItem>
                    <tags:restrictedListItem url="/pages/admin/configure">Configure PSC</tags:restrictedListItem>
                    <tags:restrictedListItem url="/pages/admin/configureAuthentication">Configure authentication</tags:restrictedListItem>
                    <tags:restrictedListItem url="/pages/admin/diagnostics">Grid services configuration</tags:restrictedListItem>

                </ul>
            </laf:division>
        </laf:box>
    </security:secureOperation>

    </body>
</html>