<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <tags:stylesheetLink name="provided-sites"/>
    <tags:javascriptLink name="sites/provided-sites" />
    <tags:javascriptLink name="resig-templates" />
    <tags:resigTemplate id="new_site_data_row">
        <tr class="site row [#= identifier #]">
            <td title="[#= name #]" id="site-name-[#= identifier #]" >
                [#= name #]
            </td>
            <td title="[#= identifier #]" id="assigned-identifier-[#= identifier #]">
                [#= identifier #]
            </td>
            <td title="[#= provider #]" id="provider-[#= identifier #]">
                [#= provider #]
            </td>
        </tr>
    </tags:resigTemplate>
    <tags:resigTemplate id="new_site_control">
            <td id="control [#= identifier #]">
                <input id="add-[#= identifier #]" type="button" name="AddButton" value="Add" onclick="SC.PS.addNewSiteSetup('[#= identifier #]')"/>
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
            An important concept in PSC's model of a clinical trial are the sites which participate
            in the trial.  Some installations of PSC might have more than one site, but every
            installation needs at least one.  Please enter your first site here.  If you need to
            enter more, you'll have the opportunity once this initial setup is complete.
        </p>
    </laf:division>
    <h3>Enter site info</h3>
    <laf:division>
        <form:form commandName="site">
            <input type="hidden" name="_eventId" value="save"/>
            <div class="row">
                <div class="label"><form:label path="name">Site name</form:label></div>
                <div class="value"><form:input path="name" size="40" id="site-name"/></div>
            </div>
            <div class="row">
                <div Class="label"><form:label path="assignedIdentifier">Assigned Identifier</form:label></div>
                <div class="value"><form:input path="assignedIdentifier" size="40" id="assigned-identifier"/></div>
            </div>
            <div class="row submit">
                <input type="submit" value="Save"/>
            </div>
            <tags:activityIndicator id="provided-site-search-indicator"/>
        </form:form>
        <div class="site-response"></div>
    </laf:division>
</laf:box>

</body>
</html>