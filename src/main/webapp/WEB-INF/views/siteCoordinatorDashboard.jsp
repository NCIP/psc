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

            <c:url value="/pages/dashboard/siteCoordinatorSchedule" var="action"/>
            <c:forEach items="${studies}" var="study">
                <a href="${action}?study=${study.id}">${study.name}</a>
            </c:forEach>
             <br/>
            <table cellspacing="0" cellpading="0" border="1" class="siteRoles">
                <tr>
                    <th></th>
                    <c:forEach items="${sites}" var="site">
                        <th>${site.name}</th>
                    </c:forEach>

                </tr>
                <c:forEach items="${command.studyAssignmentGrid}" var="user">
                    <tr>
                        <th>${user.key.name}</th>

                        <c:forEach items="${sites}" var="site">
                            <c:if test="${command.studyAssignmentGrid[user.key][site].siteAccessAllowed}">
                                <td>
                                    <form:checkbox path="studyAssignmentGrid[${user.key.id}][${site.id}].selected"/>
                                </td>
                            </c:if>
                            <c:if test="${not command.studyAssignmentGrid[user.key][site].siteAccessAllowed}">
                                <td style="background-color:#999">&nbsp;</td>
                            </c:if>
                        </c:forEach>
                    </tr>
                </c:forEach>
            </table>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>