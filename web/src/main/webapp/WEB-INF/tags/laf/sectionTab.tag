<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@attribute name="section" type="gov.nih.nci.cabig.ctms.web.chrome.Section" required="true"%>
<%@attribute name="currentSection" type="gov.nih.nci.cabig.ctms.web.chrome.Section"%>
<li class="${section == currentSection ? 'selected' : ''}"><div>
    <a href="<c:url value="${section.mainUrl}"/>">${section.displayName}</a>
</div></li>
