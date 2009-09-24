<%@tag%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@attribute name="failed" type="java.lang.Boolean"%>
<%@attribute name="ajax" type="java.lang.Boolean"%>

<form method="post" id="login" action="<c:url value="/auth/login_form_security_check"/>">
    <p class="error" id="loginInputError"></p
    <c:if test="${not empty param.login_error}">
        <p class="error"> Incorrect username and/or password.  Please try again. </p>
    </c:if>
    <div class="row">
        <div class="label">
            Username
        </div>
        <div class="value">
            <input type="text" name="j_username"
                    value="${sessionScope['ACEGI_SECURITY_LAST_USERNAME']}"
                    id="username"/>
        </div>
    </div>
    <div class="row">
        <div class="label">
            Password
        </div>
        <div class="value">
            <input type="password" name="j_password" id="password"/>
        </div>
    </div>
    <div class="row">
        <div class="submit">
            <c:if test="${ajax}">
                <tags:activityIndicator id="login-indicator"/>
                <input type="button" value="Cancel" id="login-cancel-button"/>
            </c:if>
            <input type="submit" value="Log in"/>
        </div>
    </div>
</form>
