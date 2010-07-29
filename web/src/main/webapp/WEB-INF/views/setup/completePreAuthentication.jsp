<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<html>
<head>
    <title>Initial user setup complete</title>
</head>
<body>
<laf:box title="Initial user setup complete" autopad="true">
    <p class="instructions">
        In order to finish setting up PSC, you'll need to log in with the credentials you
        just provided.  <a href="<c:url value="/"/>">Log in now.</a>
    </p>
</laf:box>
</body>
</html>