<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="admin" tagdir="/WEB-INF/tags/admin"%>
<html>
<head>
    <tags:stylesheetLink name="admin"/>
    <script type="text/javascript">
        var authSystemField = 'conf[${authenticationSystemKey}].value';
        var customClassCache = '${command.customAuthenticationSystemClass}';

        function selectCustomClass() {
            if ($F(authSystemField) == '') {
                $('custom-classname').show()
                $('customAuthenticationSystemClass').value = customClassCache;
            } else {
                customClassCache = $F('customAuthenticationSystemClass');
                $('customAuthenticationSystemClass').value = '';
                $('custom-classname').hide()
            }
        }

        function updateSystemOptions() {
            var selectedSystem;
            if ($F(authSystemField) == '') {
                selectedSystem = $F('customAuthenticationSystemClass')
            } else {
                selectedSystem = $F(authSystemField)
            }
            var params = { }
            params[authSystemField] = selectedSystem
            $("auth-enum-indicator").reveal()
            new Ajax.Request("<c:url value="/pages/admin/configureAuthentication"/>", {
                parameters: params,
                onComplete: function() {
                    $("auth-enum-indicator").conceal()
                }
            })
        }

        document.observe("dom:loaded", function() {
            selectCustomClass()
            $(authSystemField).observe("change", selectCustomClass)
            $(authSystemField).observe("change", updateSystemOptions)
            $('custom-classname').observe("change", updateSystemOptions)
        })
    </script>
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
                <form:select path="conf[${authenticationSystemKey}].value">
                    <form:options items="${knownAuthenticationSystems}" itemLabel="displayName"/>
                    <option value="" <c:if test="${isCustomAuthenticationSystem}">selected="selected"</c:if>>custom</option>
                </form:select>
                <span id="custom-classname">
                    AuthenticationSystem implementation class:
                    <form:textarea path="customAuthenticationSystemClass" cols="60" rows="2" cssStyle="vertical-align: top"/>
                </span>
            </p>
            <ul id="system-descriptions">
                <c:forEach items="${knownAuthenticationSystems}" var="known">
                    <li><strong>${known.displayName}</strong> &mdash; ${known.description}</li>
                </c:forEach>
                <li><strong>custom</strong> &mdash; uses a custom implementation of the
                    AuthenticationSystem interface.
                    To use this option, you need to ensure that the custom implementation is on your
                    servlet container's classpath, along with any of its dependencies.
                </li>
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