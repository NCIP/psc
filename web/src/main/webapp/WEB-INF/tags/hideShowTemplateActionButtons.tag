<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ attribute name="templateActions" required="false" type="java.util.Collection" %>

if ($('release-revision-button')) {
  $('release-revision-button').up('li').hide();
}

<c:forEach items="${templateActions}" var="action">
    <c:if test="${action.token == 'RELEASE_REVISION'}">
        if ($('release-revision-button')) {
          $('release-revision-button').up('li').show();
        }
    </c:if>
</c:forEach>