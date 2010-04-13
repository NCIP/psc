<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Create the first source</title>
</head>
<body>
<laf:box title="Tips" autopad="true" id="setup-tips">
    <ul class="tips">
        <li>Default manually activity target source name should be unique to the institution.</li>
        <li>You'll have the opportunity later to change default source to other source if necessary.</li>
    </ul>
</laf:box>
<laf:box title="Create the default manually activity target source" id="setup-input">
    <laf:division>
        <p class="instructions">
            PSC installation required one manual activity target source. Please enter your default manual activity source here. If you need to change manual activity target source, you'll have the
            opportunity once this initial setup is complete.
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