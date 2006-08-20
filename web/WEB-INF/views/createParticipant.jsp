<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <title>${action} Study</title>
    <tags:includeScriptaculous/>
    <style type="text/css">
        div.label {
            width: 35%;
        }
        div.submit {
            text-align: right;
        }
        form {
            width: 20em;
        }
    </style>
</head>
<body>
<h1>${action} Participant</h1>

<form action="<c:url value="/pages/createParticipant"/>" method="post">
    <div class="row">
        <div class="label">
            <label for="first-name">FirstName</label>
        </div>
        <div class="value">
            <input type="text" id="first-name" name="firstName"/>
        </div>
    </div>
    <div class="row">
        <div class="label">
            <label for="last-name">LastName</label>
        </div>
        <div class="value">
            <input type="text" id="last-name" name="lastName"/>
        </div>
    </div>
    <div class="row">
        <div class="label">
            <label for="date-of-birth">dateOfBirth</label>
        </div>
        <div class="value">
            <input type="text" id="date-of-birth" name="dateOfBirth"/>
        </div>
    </div>
    <div class="row">
        <div class="label">
            <label for="gender">gender</label>
        </div>
        <div class="value">
            <input type="text" id="gender" name="gender"/>
        </div>
    </div>
    <div class="row">
        <div class="label">
            <label for="social-security-number">socialSecurityNumber</label>
        </div>
        <div class="value">
            <input type="text" id="social-security-number" name="socialSecurityNumber"/>
        </div>
    </div>
    <div class="row">
        <div class="value submit">
            <input type="submit" value="Create"/>
        </div>
    </div>
</form>

</body>
</html>