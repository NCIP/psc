<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="usesLocalPasswords" scope="request" type="java.lang.Boolean"/>
<html>
<head>
    <title>Create the first administrative account</title>
</head>
<body>
<laf:box title="Tips" autopad="true" id="setup-tips">
    <ul class="tips">
        <li>You'll be able to create more System Administrator accounts later, if you like.</li>
        <li>You'll also be able to grant other privileges to the account you create here if
            you need to.
        </li>
    </ul>
</laf:box>
<laf:box title="Create the first user" id="setup-input">
    <laf:division>
        <p class="instructions">
            Every PSC instance needs at least one System Administrator.  The sysadmin performs basic
            configuration of the application.  Since you're filling out this form, the sysadmin's
            probably you.</p>
        <c:choose>
            <c:when test="${usesLocalPasswords}">
                <p class="instructions">
                    Please select a username and enter a secure password.  You'll be able to change
                    the password later, but not the username.
                </p>
            </c:when>
            <c:otherwise>
                <p class="instructions">
                    Please enter the username for a user that can log in using the authentication
                    system you configured on the previous page.
                </p>
            </c:otherwise>
        </c:choose>
    </laf:division>
    <h3>Enter sysadmin info</h3>
    <laf:division>
        <form:form commandName="adminCommand">
            <tags:errors path="*"/>
            <input type="hidden" name="_eventId" value="save"/>
            <div class="row">
                <div class="label"><form:label path="user.name">Username</form:label></div>
                <div class="value"><form:input path="user.name" size="40"/></div>
            </div>
            <div class="row">
                <div class="label"><form:label path="emailAddress">Email address</form:label></div>
                <div class="value"><form:input path="emailAddress" size="40"/></div>
            </div>
            <c:if test="${usesLocalPasswords}">
                <div class="row">
                    <div class="label"><form:label path="password">Password</form:label></div>
                    <div class="value"><form:password path="password" size="40"/></div>
                </div>
                <div class="row">
                    <div class="label"><form:label path="rePassword">Repeat password</form:label></div>
                    <div class="value"><form:password path="rePassword" size="40"/></div>
                </div>
            </c:if>
            <div class="row submit">
                <input type="submit" value="Save"/>
            </div>
        </form:form>
    </laf:division>
</laf:box>

</body>
</html>