<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Access denied</title>
</head>
<body>
<h1>Access Denied for this user.</h1>
<p><a href="<c:url value="/j_acegi_logout"/>">Login as a different user.</a></p>
</body>
</html>