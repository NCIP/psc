<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <tags:includeScriptaculous/>
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
        table.siteTable {
            width: 100%; border: 1px; background-color:#ddd ;
        }
        table.siteTable td, table.siteTable th {
            white-space: nowrap; padding: 1px; height:16pt;
        }
        div.sites {
            overflow: auto;
            width :40em; height: 30em;
            position :relative; margin-left: 11em;
        }
        table.siteTable  thead{ font-weight: bold; color: white; background-color: navy; }
    </style>
    <tags:javascriptLink name="sites/providedSites" />
    <tags:javascriptLink name="resig-templates" />
    <tags:resigTemplate id="new_site_data_row">
        <tr class="site row [#= identifier #]">
            <td title="[#= name #]" class="siteName[#= identifier #]" id="siteName[#= identifier #]" >
                [#= name #]
            </td>
            <td title="[#= identifier #]" class="assignedIdentifier[#= identifier #]" id="assignedIdentifier[#= identifier #]">
                [#= identifier #]
            </td>
        </tr>
    </tags:resigTemplate>
    <tags:resigTemplate id="new_site_control">
            <td id="control [#= identifier #]">
                <input id="Add[#= identifier #]" type="button" name="CreateButton" value="Create" onclick="SC.PS.addNewSite('[#= identifier #]')"/>
            </td>
    </tags:resigTemplate>
</head>
<body>

<laf:box title="${action} site">
    <laf:division>
        <form:form method="post">
            <form:errors path="*"/>
            <div class="row">
                <div class="label">
                    Site Name
                </div>
                <div class="value">
                    <input id="site-name" name="name" type="text" value="${name}" size="30"/>
                </div>
                <div class="label">
                    Assigned Identifier
                </div>
                <div class="value">
                    <input id="assignedIdentifier" name="assignedIdentifier" value="${assignIdentifier}" size="30" />
                </div>
            </div>
            <div class="row">
                <div class="submit">
                    <input type="submit" value="Save"/>
                </div>
            </div>
        </form:form>
        <c:forEach items="${sites}" var="site">
             <input id="existingSiteName${site.name}" type="hidden"/>
             <input id="existingAssignedIdentifier${site.assignedIdentifier}" type="hidden"/>
        </c:forEach>
        <div class="row">
            <div class="sites">
                <table id="providedSitesTable" class="siteTable" cellspacing="0" cellpadding="0" border="1">
                </table>
           </div>
        </div>
    </laf:division>
</laf:box>
</body>
</html>