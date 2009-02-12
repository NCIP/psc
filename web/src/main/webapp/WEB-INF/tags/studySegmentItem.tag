<%@tag%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="studySegment" required="true" type="edu.northwestern.bioinformatics.studycalendar.domain.StudySegment"%>
<%@attribute name="selectedStudySegment" required="false" type="edu.northwestern.bioinformatics.studycalendar.domain.StudySegment"%>
<li id="studySegment-${studySegment.id}-item" class="studySegment <c:if test="${studySegment == selectedStudySegment}">selected</c:if>">
    <a href="#" class="studySegment" id="studySegment-${studySegment.id}" title="${studySegment.qualifiedName}">${studySegment.name}</a>
</li>
