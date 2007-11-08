<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@attribute name="study" type="edu.northwestern.bioinformatics.studycalendar.domain.Study" required="true" %>
<%@attribute name="site" type="edu.northwestern.bioinformatics.studycalendar.domain.Site" required="true" %>
<%@attribute name="participants" type="java.util.Collection" required="true" %>

<c:forEach items="${participants}" var="participant">
    <li class="levelThree"><input type="checkbox" name="participants" value="${participant.id}"/>&nbsp;${participant.fullName}</li>
</c:forEach>