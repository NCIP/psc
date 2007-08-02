<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>View activities</title>
</head>
<body>
<h1>View activity</h1>
<p>
    Activity: ${activity.name}
</p>
<p>
    Activity Description: ${activity.description}
</p>
<p>
    Activity Type: ${activity.type.name}
</p>
</body>
</html>