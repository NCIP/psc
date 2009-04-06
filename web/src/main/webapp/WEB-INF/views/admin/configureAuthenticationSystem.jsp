<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="admin" tagdir="/WEB-INF/tags/admin"%>

<jsp:useBean id="command" scope="request"
             type="edu.northwestern.bioinformatics.studycalendar.web.admin.AuthenticationSystemSelectorCommand"/>
<jsp:useBean id="authenticationSystemKey" scope="request" type="java.lang.String"/>

<html>
<head>
    <tags:stylesheetLink name="admin"/>
    <script type="text/javascript">
        var authSystemField = 'auth-system-selector';
        var authSystemFieldName = 'conf[${authenticationSystemKey}].value';

        function updateSystemOptions() {
            var selectedSystem = $F(authSystemField)
            var params = { }
            params[authSystemFieldName] = selectedSystem
            $("auth-enum-indicator").reveal()
            new Ajax.Request("<c:url value="/pages/admin/configureAuthentication"/>", {
                method: 'get',
                parameters: params,
                onComplete: function() {
                    $("auth-enum-indicator").conceal()
                }
            })
        }

        document.observe("dom:loaded", function() {
            $(authSystemField).observe("change", updateSystemOptions)
        })
    </script>
    <title>Select authentication system</title>
</head>
<body>
<laf:box title="Configure authentication">
    <form:form>
        <laf:division>
            <p class="instructions">
                Select the authentication system you want to use.
            </p>
            <p id="system-select">
                <tags:activityIndicator id="auth-enum-indicator"/>
                <form:select path="conf[${authenticationSystemKey}].value" id="auth-system-selector">
                    <form:options items="${command.directory.entries}" itemLabel="name" itemValue="key"/>
                </form:select>
            </p>
            <ul id="system-descriptions">
                <c:forEach items="${command.directory.entries}" var="entry">
                    <li>
                        <strong>${entry.name}</strong> &mdash; ${entry.behaviorDescription}
                        <c:if test="${entry.default}">(default)</c:if>
                    </li>
                </c:forEach>
            </ul>
            <div id="errors">
                <tags:errors path="*"/>
            </div>
        </laf:division>
        <div id="system-configuration">
            <admin:authenticationSystemOptions/>
        </div>
        <div class="row submit">
            <input type="submit" value="Save"/>
        </div>
    </form:form>
</laf:box>
</body>
</html>