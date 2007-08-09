<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<html>
    <head>
        <tags:stylesheetLink name="main"/>
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
            /*h2 {*/
                /*margin-top: 2em;*/
            /*}*/
        </style>
    </head>
    <body>

    <security:secureOperation element="/pages/manageSites" operation="ACCESS">
        <laf:box title="Administration">
            <ul class="menu">
                <tags:restrictedListItem url="/pages/manageSites">Manage sites</tags:restrictedListItem>
                <tags:restrictedListItem url="/pages/listUsers">Manage Users</tags:restrictedListItem>
                <tags:restrictedListItem url="/pages/configure">Configure PSC</tags:restrictedListItem>
            </ul>
        </laf:box>
    </security:secureOperation>

    </body>
</html>