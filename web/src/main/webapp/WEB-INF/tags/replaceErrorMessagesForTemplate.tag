<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--<%@attribute name="studySegmentList" required="true" type="edu.northwestern.bioinformatics.studycalendar.web.delta.AmendmentView"%>--%>
<%--<%@attribute name="style" required="false"%>--%>

<c:if test="${fn:length(studyWorkflowMessages) > 0}">
    <c:forEach items="${studyWorkflowMessages}" var="msg">
        <h5>${msg.html}</h5>
    </c:forEach>
</c:if>