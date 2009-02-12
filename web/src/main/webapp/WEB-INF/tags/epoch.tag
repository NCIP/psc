<%@tag%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@attribute name="last" type="java.lang.Boolean"%>
<%@attribute name="epoch" type="edu.northwestern.bioinformatics.studycalendar.domain.Epoch" required="true" %>
<%@attribute name="selectedStudySegment" type="edu.northwestern.bioinformatics.studycalendar.domain.StudySegment"%>
<div class="epoch${last ? ' last' : ''}" style="width: <tags:epochWidth epoch="${epoch}"/>" id="epoch-${epoch.id}">
    <h4 title="Epoch ${epoch.name} has ${epoch.multipleStudySegments ? fn:length(epoch.studySegments) : 'no'} study segements" id="epoch-${epoch.id}-header"><span id="epoch-${epoch.id}-name">${epoch.name}</span></h4>
    <ul class="studySegments" id="epoch-${epoch.id}-studySegments">
    <c:forEach items="${epoch.studySegments}" var="studySegment">
        <tags:studySegmentItem studySegment="${studySegment}" selectedStudySegment="${selectedStudySegment}"/>
    </c:forEach>
    </ul>
</div>
