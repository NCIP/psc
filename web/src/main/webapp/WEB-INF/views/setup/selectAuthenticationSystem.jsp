<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="admin" tagdir="/WEB-INF/tags/admin"%>
<html>
<head>
    <tags:stylesheetLink name="admin"/>
    <title>Select authentication system</title>
</head>
<body>
<laf:box title="Select Authentication System">
    <laf:division>
        <p class="instructions">
            Every PSC instance needs to select authentication system. You'll be able to change the authentication system later. </p>
    </laf:division>
    <h3>Select authentication system</h3>
    <laf:division>
        <form:form commandName="selectAuthenticationSystemCommand">
            <input type="hidden" name="_eventId" value="next"/>
            <form:select path="authenticationSystem" id="auth-system-selector">
                <form:options items="${directory.entries}" itemLabel="name" itemValue="key"/>
            </form:select>
            <ul id="system-descriptions">
                <c:forEach items="${directory.entries}" var="entry">
                    <li>
                        <strong>${entry.name}</strong> &mdash; ${entry.behaviorDescription}
                        <c:if test="${entry.default}">(default)</c:if>
                    </li>
                </c:forEach>
            </ul>
            <div id="errors">
                <tags:errors path="*"/>
            </div>
            <div class="row submit">
                <input type="submit" value="Next"/>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>