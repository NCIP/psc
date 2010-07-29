<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="admin" tagdir="/WEB-INF/tags/admin"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:useBean id="authenticationSystemName" scope="request" type="java.lang.String"/>
<jsp:useBean id="command" scope="request"
             type="edu.northwestern.bioinformatics.studycalendar.web.admin.AuthenticationSystemSelectorCommand"/>
<jsp:useBean id="authenticationSystemKey" scope="request" type="java.lang.String"/>
<jsp:useBean id="authenticationSystemValue" scope="request" type="java.lang.String"/>
<c:set var="needsConfiguration" value="${fn:length(command.conf) > 1}"/>

<html>
<head>
    <tags:stylesheetLink name="admin"/>
    <title>Configure selected authentication system</title>
</head>
<body>
<laf:box title="Configure selected authentication system">
    <laf:division>
        <p class="instructions">
            PSC will use the <em>${authenticationSystemName}</em> authentication system.
            To select a different authentication system go 
            <a href="<c:url value="/setup/preAuthenticationSetup"/>" class="control">back</a>.
        </p>
        <c:if test="${not needsConfiguration}">
            <p class="instructions">
                This system does not require any further configuration.  You can continue on.
            </p>
        </c:if>
    </laf:division>
    <form:form commandName="authenticationSystemSetupCommand">
        <input type="hidden" name="_eventId" value="save"/>
        <input name="conf[${authenticationSystemKey}].value" type="hidden" value="${authenticationSystemValue}"/>
        <div id="errors">
            <tags:errors path="*"/>
        </div>
        <div id="system-configuration">
            <admin:authenticationSystemOptions/>
        </div>
        <div class="row submit">
            <input type="submit" value="${needsConfiguration ? 'Save' : 'Continue'}"/>
        </div>
    </form:form>
</laf:box>
</body>
</html>