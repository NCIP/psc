<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ attribute name="templateActions" required="false" type="java.util.Collection<edu.northwestern.bioinformatics.studycalendar.service.presenter.TemplateAction>" %>

if ($('preview-schedule-button')) {
  $('preview-schedule-button').up('li').hide();
}

if ($('release-revision-button')) {
  $('release-revision-button').up('li').hide();
}

<c:forEach items="${templateActions}" var="action">
    <c:if test="${action.token == 'PREVIEW_SCHEDULE'}">
        if ($('preview-schedule-button')) {
          $('preview-schedule-button').up('li').show();
        }
    </c:if>

    <c:if test="${action.token == 'RELEASE_REVISION'}">
        if ($('release-revision-button')) {
          $('release-revision-button').up('li').show();
        }
    </c:if>
</c:forEach>