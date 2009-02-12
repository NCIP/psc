<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<html>
<head>
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
                <div class="value"><form:input path="name" size="40"/></div>
                <div Class="label"><form:label path="assignedIdentifier">Assigned Identifier</form:label></div>
                <div class="value"><form:input path="assignedIdentifier" size="40"/></div>
            </div>
            <div class="row submit">
                <input type="submit" value="Save"/>
            </div>
        </form:form>
    </laf:division>
</laf:box>

</body>
</html>