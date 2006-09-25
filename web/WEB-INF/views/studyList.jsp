<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Study Menu</title>
</head>
<body>
<h1>Study Menu</h1>

<security:secureOperation element="CreateStudyLink" operation="ACCESS">
<p><a href="<c:url value="/pages/newStudy"/>">Create new study</a></p>
</security:secureOperation>

<h2>Existing studies</h2>
<table>
    <c:forEach items="${studies}" var="study">
        <tr>
            <td><a href="<c:url value="/pages/calendarTemplate?id=${study.id}"/>">${study.name}</a></td>
        </tr>
    </c:forEach>
</table>
</body>
</html>