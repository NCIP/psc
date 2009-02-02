<%@attribute name="task" required="true" type="gov.nih.nci.cabig.ctms.web.chrome.Task" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<a href="<c:url value="${task.url}"/>">${task.displayName}</a>