<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="laf" uri="/WEB-INF/tags/laf.tld"%>
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
        the regular PSC interface. (You'll need to log in with a System Administrator account, of course.)
    </p>
</laf:box>
</body>
</html>