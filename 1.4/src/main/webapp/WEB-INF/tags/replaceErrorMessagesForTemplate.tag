<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%--<%@attribute name="studySegmentList" required="true" type="edu.northwestern.bioinformatics.studycalendar.web.delta.AmendmentView"%>--%>
<%--<%@attribute name="style" required="false"%>--%>


<c:forEach items="${epochs}" var="epoch">
    <c:if test="${epoch.name == '[Unnamed epoch]'}">
        <h5 id="setEpochName">Please set name for epoch '${epoch.name}'</h5>        
    </c:if>
    <c:forEach items="${epoch.studySegments}" var="studySegment">
        <c:if test="${studySegment.name == '[Unnamed epoch]'}">
            <h5 id="setStudySegmentName">Please set name for segment '${studySegment.name}'</h5>
        </c:if>
        <c:if test="${empty studySegment.periods}">
            <h5 id="missingPeriods">Segment '${studySegment.name}' has no periods. Please add a period or delete a segment</h5>
        </c:if>
        <c:forEach items="${studySegment.periods}" var="listOfPeriods">
            <c:if test="${empty listOfPeriods.plannedActivities}">
                <h5 id="missingActivities">Segment '${studySegment.name}' has periods with no activities</h5>
            </c:if>
        </c:forEach>
    </c:forEach>
</c:forEach>    
