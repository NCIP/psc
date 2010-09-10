<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ attribute name="templateActions" required="false" %>

<c:forEach items="${templateActions}" var="action">
    <c:choose>
        <c:when test="${action == '[PREVIEW_SCHEDULE]'}">
            if ($('preview-schedule-button')) {
              $('preview-schedule-button').up('li').show();
            }
        </c:when>
        <c:otherwise>
            if ($('preview-schedule-button')) {
              $('preview-schedule-button').up('li').hide();
            }
        </c:otherwise>
    </c:choose>

    <c:choose>
        <c:when test="${action == '[RELEASE_REVISION]'}">
            if ($('release-revision-button')) {
              $('release-revision-button').up('li').show();
            }
        </c:when>
        <c:otherwise>
            if ($('release-revision-button')) {
              $('release-revision-button').up('li').hide();
            }
        </c:otherwise>
    </c:choose>
</c:forEach>