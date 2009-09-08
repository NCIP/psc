<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<html>
<head>
    <title>Initial user setup complete</title>
</head>
<body>
<laf:box title="Initial user setup complete" autopad="true">
    <p>
        Initial setup of user for this Patient Study Calendar instance is complete.Create first site through <a href="<c:url value="/pages/admin/manage/newSite"/>">administration section</a> of
        the regular PSC interface. (You'll need to log in with a System Administrator account, of course.)
    </p>
</laf:box>
</body>
</html>