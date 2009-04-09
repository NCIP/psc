<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <style type="text/css">
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
                <input id="Add[#= identifier #]" type="button" name="AddButton" value="Add" onclick="SC.PS.addNewSiteSetup('[#= identifier #]')"/>
            </td>
    </tags:resigTemplate>
    <title>Create the first site</title>
</head>
<body>
<laf:box title="Tips" autopad="true" id="setup-tips">
    <ul class="tips">
        <li>You might be more familiar with the "site" concept as an "institution" or an "affiliate"</li>
        <li>If you're not sure what to enter here, consider using just the the name of your organization.  E.g.,
            "Northwestern University" or "Oregon Health Sciences University."
        </li>
        <li>You'll have the opportunity to add more sites later, if necessary.</li>
    </ul>
</laf:box>
<laf:box title="Create the first site" id="setup-input">
    <laf:division>
        <p class="instructions">
            One of the elements of PSC's security model is the site which subject coordinators
            and site coordinators hail from.  Some installations might have more than one site, but every installation
            needs at least one.  Please enter your first site here.  If you need to enter more, you'll have the
            opportunity once this initial setup is complete.
        </p>
    </laf:division>
    <h3>Enter site info</h3>
    <laf:division>
        <form:form commandName="site">
            <input type="hidden" name="_eventId" value="save"/>
            <div class="row">
                <div class="label"><form:label path="name">Site name</form:label></div>
                <div class="value"><form:input path="name" size="40" id="site-name"/></div>
                <div Class="label"><form:label path="assignedIdentifier">Assigned Identifier</form:label></div>
                <div class="value"><form:input path="assignedIdentifier" size="40" id="assigned-Identifier"/></div>
            </div>
            <div class="row submit">
                <input type="submit" value="Save"/>
            </div>
        </form:form>
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