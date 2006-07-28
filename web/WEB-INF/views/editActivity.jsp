<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <title>${action} Activity</title>
    <tags:javascriptLink name="scriptaculous/scriptaculous"/>
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
<h1>${action} Activity</h1>

<form action="<c:url value="/pages/newActivity"/>" method="post">
    <div class="row">
        <div class="label">
            <label for="activity-name">Activity name</label>
        </div>
        <div class="value">
            <input type="text" id="activity-name" name="activityName"/>
        </div>
    </div>
    <div class="row">
        <div class="label">
            <label for="activity-description">Activity description</label>
        </div>
        <div class="value">
            <input type="text" id="activity-description" name="activityDescription"/>
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