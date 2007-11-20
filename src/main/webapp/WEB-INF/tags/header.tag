<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<laf:header>
    <jsp:attribute name="logoText">Patient Study Calendar</jsp:attribute>
    <jsp:attribute name="logoImageUrl"><c:url value="/images/logo.png"/></jsp:attribute>
    <jsp:attribute name="tagline">Patient Study Calendar</jsp:attribute>
    <jsp:attribute name="taglineImageUrl"><c:url value="/images/tagline.png"/></jsp:attribute>
    <jsp:attribute name="logoutUrl"><c:url value="/j_acegi_logout"/></jsp:attribute>
    <jsp:attribute name="renderSection">
        <laf:sectionTab section="${section}" currentSection="${currentSection}"/>
    </jsp:attribute>
    <jsp:attribute name="renderTask">
        <c:choose>
            <c:when test="${currentSection.displayName == 'Dashboard'}">
                <tags:restrictedTaskItem role="${task.displayName}"><laf:taskLink task="${task}"/></tags:restrictedTaskItem>
            </c:when>
            <c:otherwise>
                <laf:taskLink task="${task}"/>
            </c:otherwise>
        </c:choose>
    </jsp:attribute>
</laf:header>
