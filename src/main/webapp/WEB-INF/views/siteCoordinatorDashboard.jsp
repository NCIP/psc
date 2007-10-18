<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head><title>Site Coordinator Dashboard</title></head>
<body>

<laf:box title="Site Coordinator Dashboard">
    <laf:division>
        <form:form method="post">
            <form:errors path="*"/>

            <table cellspacing="0" cellpading="0" border="1" class="siteRoles">
                <tr>
                    <th></th>
                    <c:forEach items="${sites}" var="site">
                        <th>${site.name}</th>
                    </c:forEach>

                </tr>
                <c:forEach items="${command.studyAssignmentGrid}" var="user" varStatus="index">
                    <tr>
                        <th>${user.key.name}</th>

                    </tr>
                </c:forEach>
            </table>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>