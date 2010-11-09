<%@tag %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@attribute name="epoch" type="edu.northwestern.bioinformatics.studycalendar.domain.Epoch" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>

<c:forEach items="${epoch.studySegments}" var="studySegment">
  <c:if test="${not empty studySegment.periods}">
    <div class="row">
        <label>${studySegment.qualifiedName}</label>
    </div>
    <c:forEach items="${studySegment.periods}" var="period" varStatus="status">
        <div class="row ${commons:parity(status.count)} tab">
            <Input type= radio id="${period.id}" name="periodName" value="false" onclick="getSelectedPeriod(${period.id})"/>
            <label>${period.displayNameWithActivities}</label>
        </div>
    </c:forEach>
  </c:if>
</c:forEach>

