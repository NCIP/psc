<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--<%@attribute name="studySegmentList" required="true" type="edu.northwestern.bioinformatics.studycalendar.web.delta.AmendmentView"%>--%>
<%--<%@attribute name="style" required="false"%>--%>

<c:if test="${not empty studyWorkflowMessages}">
    <c:forEach items="${studyWorkflowMessages}" var="msg">
        <c:choose>
            <c:when test="${fn:containsIgnoreCase(msg.text, 'assigned identifier')}">
                <h5>Template ${msg.html}</h5>
            </c:when>
            <c:otherwise>
                <h5>${msg.html}</h5>
            </c:otherwise>
        </c:choose>
    </c:forEach>
</c:if>