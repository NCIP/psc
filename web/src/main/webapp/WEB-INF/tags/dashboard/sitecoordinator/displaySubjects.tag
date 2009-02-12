<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@attribute name="study" type="edu.northwestern.bioinformatics.studycalendar.domain.Study" required="true" %>
<%@attribute name="site" type="edu.northwestern.bioinformatics.studycalendar.domain.Site" required="true" %>
<%@attribute name="subjects" type="java.util.Collection" required="true" %>

<c:forEach items="${subjects}" var="subject">
    <li><input type="checkbox" name="subjects" value="${subject.id}"  onclick="deselectOrSelectAllCheckbox('${study.id}_${site.id}', this.checked)" />&nbsp;<span class="subject"> ${subject.fullName}</span></li>
</c:forEach>