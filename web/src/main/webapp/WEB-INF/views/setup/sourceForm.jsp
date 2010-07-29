<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Create the manually-created activity source</title>
</head>
<body>
<laf:box title="Tips" autopad="true" id="setup-tips">
    <ul class="tips">
        <li>The manually-created activity target source name should be unique to this deployment of
            PSC.  Don't name it something generic like "activities" or "source".  Consider including
            your organization's name, e.g. "Northwestern University Cancer Trial Activities" or
            "ECOG PSC".
        </li>
        <li>You'll have the opportunity later to change the manually-created activity source to
            something else if necessary.
        </li>
    </ul>
</laf:box>
<laf:box title="Create the manually-created activity target source" id="setup-input">
    <laf:division>
        <p class="instructions">
            Study templates in PSC are built out of "activities."  PSC supports importing libraries
            of activities, but you (or your users) will need to create other one-off activities.
            In PSC, a group of activities under a common coding system is called an "activity
            source."  Manually-created one-off activities go into a special source so that they
            can be tracked.
        </p>
        <p class="instructions">
            In order to avoid conflicts when sharing templates with other PSC deployments, every
            PSC deployment needs a uniquely named source for these manually-created activities.
            Set that name here.  You'll be able to switch to a different manually-created activity
            source later.
        </p>
    </laf:division>
    <h3>Enter source info</h3>
    <laf:division>
        <form:form commandName="source">
            <input type="hidden" name="_eventId" value="save"/>
            <div class="row">
                <div class="label"><form:label path="name">Source name</form:label></div>
                <div class="value"><form:input path="name" size="40" id="source-name"/></div>
            </div>
            <div class="row submit">
                <input type="submit" value="Save"/>
            </div>
        </form:form>
    </laf:division>
</laf:box>

</body>
</html>