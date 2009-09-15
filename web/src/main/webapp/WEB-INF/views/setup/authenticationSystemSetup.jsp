<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="admin" tagdir="/WEB-INF/tags/admin"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <tags:stylesheetLink name="admin"/>
    <style type="text/css">
        span.textLabel {
            font-weight: bold;
            font-style: italic;
        }
    </style>
    <title>Configure authentication system's option</title>
</head>
<body>
<laf:box title="Configure authentication">
    <laf:division>
        <p class="instructions">
            Selected authentication system is <span class="textLabel">${authenticationSystemName}</span>
            .To select other authentication system go
            <input type="button" value="Back" onclick="window.location.href= SC.relativeUri('setup/preAuthenticationSetup')">
        </p>
    </laf:division>
    <laf:division>
        <form:form commandName="authenticationSystemSetupCommand">
            <input type="hidden" name="_eventId" value="save"/>
            <input name="conf[${authenticationSystemKey}].value" type="hidden" value="${authenticationSystemValue}"/>
            <div id="errors">
                <tags:errors path="*"/>
            </div>
            <div id="system-configuration">
                <c:if test="${fn:length(command.conf) < 2}">
                    <h3>Configuration options for the selected system</h3>
                    <p class="description">No other options required for <span class="textLabel">
                    ${authenticationSystemName}</span> authentication system. 
                </c:if>
                <admin:authenticationSystemOptions/>
            </div>
            <div class="row submit">
                <input type="submit" value="Save"/>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>