<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<html>
<head>
    <title>Initial setup complete</title>
</head>
<body>
<laf:box title="Initial setup complete" autopad="true">
    <p>
        Initial setup of this Patient Study Calendar instance is complete.  For security reasons, you can't
        repeat this initial setup.  If you need to make any changes to the settings you specified, you can find the
        relevant controls in the <a href="<c:url value="/pages/admin/configure"/>">administration section</a> of
        the regular PSC interface.
    </p>
</laf:box>
</body>
</html>